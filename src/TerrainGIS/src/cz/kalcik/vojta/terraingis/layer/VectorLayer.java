package cz.kalcik.vojta.terraingis.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import jsqlite.Exception;
import jsqlite.Stmt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.ShapeFileRecord;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteAttributesIterator;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;

import android.graphics.Paint;

/**
 * class for vector layer
 * @author jules
 *
 */
public abstract class VectorLayer extends AbstractLayer
{
    // constants ==============================================================
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    
    // attributes ==============================================================
    protected static class VectorLayerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public String selectedRowid;
        public Coordinate clickedPoint;
       
        // temporery edited object values
        public String tempEditedRowid;
        public int tempEditedSelectedVertexIndex;
        public ArrayList<Coordinate> tempEditedVertices;
    }
    
    private boolean mHasIndex;

    protected Paint mPaint;
    protected Paint mSelectedPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData mVectorLayerData = new VectorLayerData();
    protected AttributeHeader mAttributeHeader;
    protected MapFragment mMapFragment;
    
    protected EditedObject mEditedObject = new EditedObject();
    protected int mCountObjects;    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     * @throws Exception 
     */
    public VectorLayer(VectorLayerType type, String name, int srid,
                       SpatiaLiteIO spatialite, MapFragment mapFragment)
                               throws Exception
    {
        mMapFragment = mapFragment;
        mType = type;
        setPaints();     
        data.name = name;
        this.mSrid = srid;
        this.mSpatialite = spatialite;
        mGeometryColumn = mSpatialite.getColumnGeom(name);
        mHasIndex = mSpatialite.indexEnabled(name);
        mAttributeHeader = mSpatialite.getAttributeTable(name);
        updateLayerAttributes();
    }

    // abstract methods =======================================================
    abstract protected int getMinCountPoints();
    
    // public methods =========================================================    
    @Override
    public void detach()
    {

    }
    
    /**
     * add lon lat point to edited object
     * @param coordinate
     * @throws ParseException 
     * @throws Exception 
     */
    public void addPointToEdited(Coordinate coordinate, int srid, boolean addToEnd)
            throws Exception, ParseException
    {
        mEditedObject.addPoint(coordinate, srid, addToEnd);
    }

    /**
     * add lon lat points to edited objects
     * @param points
     * @throws ParseException 
     * @throws Exception 
     */
    public void addPointsToEdited(ArrayList<Coordinate> points, int srid)
            throws Exception, ParseException
    {
        mEditedObject.addPoints(points, srid);
    }
    
    /**
     * insert edited object
     * @param attributes
     * @throws Exception 
     */
    public void insertEditedObject(AttributeRecord attributes)
            throws Exception
    {
        insertObject(attributes, mEditedObject.getGeometry());
        mEditedObject.clear();
    }
    
    /**
     * update edited object
     * @throws NumberFormatException
     * @throws Exception
     */
    public void updateEditedObject() throws NumberFormatException, Exception
    {
        Geometry geometry = createGeometry(mEditedObject.getVertices(), mType);
        mSpatialite.updateObject(data.name, mGeometryColumn,
                Integer.parseInt(mEditedObject.getRowid()), geometry,
                mSrid, mLayerManager.getSrid());
        mEditedObject.clear();
        
        updateLayerAttributes();
    }

    /**
     * @return true if object is not saved
     */
    public boolean isEditedObjectNew()
    {
        return mEditedObject.isNew();
    }
    
    /**
     * import objects to layer
     * @param iterGeometries
     * @throws Exception
     */
    public void importObjects(Iterator<ShapeFileRecord> iterGeometries) throws Exception
    {
        boolean usePK = true;
        Stmt stmt = mSpatialite.prepareInsert(super.data.name, mGeometryColumn, mSrid, mSrid,
                mAttributeHeader, usePK);
        
        while(iterGeometries.hasNext())
        {
            ShapeFileRecord record = iterGeometries.next();
            AttributeRecord values = new AttributeRecord(mAttributeHeader, record.getAttributes());
            values.trimValues();
            mSpatialite.insertObject(stmt, record.getGeometry(), values, usePK);
        }
        
        stmt.close();
        updateLayerAttributes();
    }
    
    /**
     * remove layer from db
     * @throws Exception 
     */
    public void remove() throws Exception
    {
        mSpatialite.removeLayer(super.data.name, mGeometryColumn, mHasIndex);
    }
    
    /**
     * set ROWID of clicked object
     * @param point
     * @param bufferDistance
     * @throws ParseException 
     * @throws Exception 
     */
    public void clickSelectionObject(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        mVectorLayerData.clickedPoint = point;
        
        ArrayList<String> rowidObjects = getNearestObjectsToPoint(envelope, point);
        
        String rowid = null;
        if(rowidObjects.size() > 0)
        {
            rowid = rowidObjects.get(0);
        }
        
        mVectorLayerData.selectedRowid = rowid;
    }
    
    /**
     * open editing object
     * @param envelope
     * @param point
     * @throws Exception
     * @throws ParseException
     */
    public void clickEditingObject(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        ArrayList<String> rowidObjects = getNearestObjectsToPoint(envelope, point);
        
        String rowid = rowidObjects.size() == 0 ? null : rowidObjects.get(0);
        mEditedObject.openObject(rowid);
    }

    
    /**
     * check click on vertex of polygon
     * @param envelope
     * @param point
     * @throws Exception
     * @throws ParseException
     */
    public void checkClickEditedVertex(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        mEditedObject.findeSelectedVertex(envelope, point);
    }
    
    /**
     * select object by rowid
     * @param rowid
     * @throws Exception
     * @throws ParseException
     */
    public void selectObject(String rowid) throws Exception, ParseException
    {
        mVectorLayerData.clickedPoint = null;
        mVectorLayerData.selectedRowid = rowid;
    }
    
    /**
     * remove selected rowid
     * @throws Exception 
     * @throws ParseException 
     */
    public void removeSelectionOfObject() throws Exception, ParseException
    {
        mVectorLayerData.selectedRowid = null;
    }
    
    public void cancelNotSavedEditedChanges()
    {
        mEditedObject.clear();
    }
    
    /**
     * check if point is near selected point
     * @param point
     * @return
     */
    public boolean isNearSelectedVertex(Coordinate point)
    {
        return mEditedObject.isNearSelectedVertex(point);
    }
    
    /**
     * set new position of selected point
     * position is not cloned
     * @param position
     */
    public void setPositionSelectedVertex(Coordinate position)
    {
        mEditedObject.setPositionSelectedVertex(position); 
    }
    
    /**
     * remove object from db
     * @param rowid
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public void removeObject(String rowid) throws NumberFormatException, Exception
    {
        mSpatialite.removeObject(data.name, Integer.parseInt(rowid));
        updateLayerAttributes();
    }
    
    /**
     * find clicked vertex coordinates
     * @param envelope
     * @param point
     * @return vertex coordinates or null
     * @throws Exception
     * @throws ParseException
     */
    public Coordinate getClickPoint(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        if(mEditedObject.isOpened())
        {
            Coordinate vertex = findNearestVertex(mEditedObject.getVertices(), point);
            
            if(vertex != null)
            {
                return vertex;
            }
        }

        ArrayList<String> rowidObjects = getNearestObjectsToPoint(envelope, point);
        
        String rowid = null;
        
        for(String currentRowid: rowidObjects)
        {
            if(!currentRowid.equals(mEditedObject.getRowid()))
            {
                rowid = currentRowid;
                break;
            }
        }
        
        if(rowid == null)
        {
            return null;
        }


        if(mType == VectorLayerType.POINT)
        {
            Geometry geom = getObject(rowid);
            return geom.getCoordinate();
        }

        ArrayList<Coordinate> points = getPointsOfRowidObject(rowid);
        return findNearestVertex(points, point);
    }
    
    /**
     * remove vertex or edited object by selection
     * @throws NumberFormatException
     * @throws Exception
     */
    public void removeSelectedEdited() throws NumberFormatException, Exception
    {
        mEditedObject.removeSelected();
    }
    // getter, setter =========================================================
    /**
     * @return typ of vyctor layer
     */
    public VectorLayerType getType()
    {
        return mType;
    }
    
    
    /**
     * @return the mColumnGeom
     */
    public String getGeometryColumn()
    {
        return mGeometryColumn;
    }

    
    /**
     * @return the data
     */
    public AbstractLayerData getData()
    {
        AbstractLayerData result = super.getData();
        
        // set mVectorLayerData
        mVectorLayerData.tempEditedRowid = mEditedObject.getRowid();
        mVectorLayerData.tempEditedSelectedVertexIndex = mEditedObject.getSelectedVertexIndex();
        mVectorLayerData.tempEditedVertices = mEditedObject.getVertices();
        
        result.childData = mVectorLayerData;
        return result;
    }

    /**
     * @param mVectorLayerData the data to set
     */
    public void setData(AbstractLayerData inData)
    {
        super.setData(inData);
        mVectorLayerData = (VectorLayerData)data.childData;
        
        // set edited object
        mEditedObject.setRowid(mVectorLayerData.tempEditedRowid);
        mEditedObject.setSelectedVertexIndex(mVectorLayerData.tempEditedSelectedVertexIndex);
        mEditedObject.setVertices(mVectorLayerData.tempEditedVertices);
    }
    
    
    /**
     * @return the mAttributeHeader
     */
    public AttributeHeader getAttributeHeader()
    {
        return mAttributeHeader;
    }

    /**
     * @return the mCountObjects
     */
    public int getCountObjects()
    {
        return mCountObjects;
    }    
    
    /**
     * @return color of layer
     */
    public int getColor()
    {
        return mPaint.getColor();
    }
    
    /**
     * @return rowid of selected object
     */
    public String getSelectedRowid()
    {
        return mVectorLayerData.selectedRowid;
    }
    
    /**
     * @return true if recorded object has enough points
     */
    public boolean hasEditedObjectEnoughPoints()
    {
        return mEditedObject.hasEnoughPoints();
    }
    
    /**
     * @return true if is selected point for move
     */
    public boolean hasEditedObjectSelectedVertex()
    {
        return mEditedObject.hasSelectedVertex();
    }
    
    /**
     * @return iterator of attributes
     * @throws Exception
     */
    public SpatialiteAttributesIterator getAttributes() throws Exception
    {
        return mSpatialite.getAttributes(data.name, mAttributeHeader);
    }
    
    /**
     * @param rowid
     * @return return object in SRS of main map
     * @throws Exception
     * @throws ParseException
     */
    public Geometry getObject(String rowid) throws Exception, ParseException
    {
        return mSpatialite.getObject(data.name, mGeometryColumn,
                mLayerManager.getSrid(), Integer.parseInt(rowid));        
    }
    
    /**
     * @return true if layer has opened edited object
     */
    public boolean hasOpenedEditedObject()
    {
        return mEditedObject.isOpened();
    }
    
    /**
     * @return color of layer
     */
    public int getLayerColor()
    {
        return mPaint.getColor();
    }
    // public static ============================================================

    /**
     * convert points to geometry
     * @param points
     * @param type
     * @param geometryFactory
     * @return
     */
    public static Geometry createGeometry(ArrayList<Coordinate> points, VectorLayerType type)
    {
        Geometry result = null;
        
        // for polygon add first point
        if(type == VectorLayerType.POLYGON)
        {
            points.add(points.get(0));
        }
        
        CoordinateArraySequence coordinates =
                new CoordinateArraySequence(points.toArray(new Coordinate[points.size()]));
        
        if(type == VectorLayerType.POINT)
        {
            if(points.size() < PointsLayer.MIN_POINTS)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            result = new Point(coordinates, GEOMETRY_FACTORY);
        }
        else if(type == VectorLayerType.LINE)
        {
            if(points.size() < LinesLayer.MIN_POINTS)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            result = new LineString(coordinates, GEOMETRY_FACTORY);
        }
        else if(type == VectorLayerType.POLYGON)
        {
            if(points.size() < PolygonsLayer.MIN_POINTS + 1)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            LinearRing ring = new LinearRing(coordinates, GEOMETRY_FACTORY);
            result = new Polygon(ring, null, GEOMETRY_FACTORY);
        }
        
        return result;
    }
    
    // protected methods ========================================================
    protected SpatialiteGeomIterator getObjects(Envelope envelope)
            throws Exception, ParseException
    {
        return mSpatialite.getObjectsInEnvelope(envelope.intersection(LayerManager.MAX_ENVELOPE), super.data.name, mGeometryColumn, mSrid,
                                      mLayerManager.getSrid(), mHasIndex);
    }
    
    /**
     * update envelope of Layer
     * @throws Exception 
     */
    protected void updateLayerAttributes() throws Exception
    {
        mEnvelope = mSpatialite.getEnvelopeLayer(super.data.name, mGeometryColumn, mHasIndex);
        mCountObjects = mSpatialite.countObjects(data.name);
    }
    
    /**
     * check if object is selected
     * @param geomIterator
     * @return
     */
    protected boolean isSelectedObject(SpatialiteGeomIterator geomIterator)
    {
        return geomIterator.getLastROWID().equals(mVectorLayerData.selectedRowid);
    }
   
    /**
     * check if object is recorded
     * @param geomIterator
     * @return
     */
    protected boolean isEditedObject(SpatialiteGeomIterator geomIterator)
    {
        return geomIterator.getLastROWID().equals(mEditedObject.getRowid());
    }
    // private methods ===========================================================   
    /**
     * set paints by type
     */
    private void setPaints()
    {
        if(mType == VectorLayerType.POINT)
        {
            mPaint = VectorLayerPaints.getPoint(PaintType.DEFAULT);
            mSelectedPaint = VectorLayerPaints.getPoint(PaintType.SELECTED);
        }
        else if(mType == VectorLayerType.LINE)
        {
            mPaint = VectorLayerPaints.getLine(PaintType.DEFAULT);
            mNotSavedPaint = VectorLayerPaints.getLine(PaintType.NOT_SAVED);
            mSelectedPaint = VectorLayerPaints.getLine(PaintType.SELECTED);
        }
        else if(mType == VectorLayerType.POLYGON)
        {
            mPaint = VectorLayerPaints.getPolygon(PaintType.DEFAULT);
            mNotSavedPaint = VectorLayerPaints.getPolygon(PaintType.NOT_SAVED);
            mSelectedPaint = VectorLayerPaints.getPolygon(PaintType.SELECTED);
        }           
    }
    
    /**
     * find index of nearest point
     * @param points
     * @param point
     * @return index of nearest point or -1 if any
     */
    private int findIndexNearestPoint(ArrayList<Coordinate> points, Coordinate point)
    {
        double bufferDistance = mNavigator.getBufferDistance();
        
        if(point == null)
        {
            return -1;
        }
        
        int size = points.size();
        double minDistance = bufferDistance;
        int minIndex = -1;
        
        for(int i=0; i < size; i++)
        {
            Double distance = points.get(i).distance(point);
            if(distance < minDistance)
            {
                minDistance = distance;
                minIndex = i;
            }
        }
        
        return minIndex;
    }
    
    /**
     * @param points
     * @param point
     * @return nearest vertex
     */
    private Coordinate findNearestVertex(ArrayList<Coordinate> points, Coordinate point)
    {
        int index = findIndexNearestPoint(points, point);
        if(index != -1)
        {
            return points.get(index);
        }
        
        return null;
    }
    
    /**
     * insert object to DB
     * @param attributes
     * @param geometry
     * @throws Exception 
     */
    private void insertObject(AttributeRecord attributes, Geometry geometry)
            throws Exception
    {
        mSpatialite.insertObject(geometry, data.name, mGeometryColumn,
                mLayerManager.getSrid(), mSrid,
                mAttributeHeader, attributes, false);
        
        updateLayerAttributes();
    }    
    
    /**
     * @param envelope
     * @param point
     * @return nearest object to point if is in buffer
     * @throws Exception
     * @throws ParseException
     */
    private ArrayList<String> getNearestObjectsToPoint(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        double bufferDistance = mNavigator.getBufferDistance();

        return mSpatialite.getRowidNearCoordinate(envelope, data.name,
                mGeometryColumn, mSrid, mLayerManager.getSrid(), mHasIndex, point, bufferDistance);
    }
    
    /**
     * @param rowid
     * @return arraylist of points of object with rowid
     * @throws Exception
     * @throws ParseException
     */
    private ArrayList<Coordinate> getPointsOfRowidObject(String rowid)
            throws Exception, ParseException
    {
        Geometry object = getObject(rowid);
        
        if(object == null)
        {
            throw new RuntimeException("There is not object with rowid!");
        }
        
        ArrayList<Coordinate> result = new ArrayList<Coordinate>(Arrays.asList(object.getCoordinates()));
        
        if(mType == VectorLayerType.POLYGON)
        {
            result.remove(result.size()-1);
        }
        
        return result;
    }
    
    // classes =========================================================================================
    protected class EditedObject implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        private ArrayList<Coordinate> vertices = new ArrayList<Coordinate>();
        private String rowid = null;

        private int selectedVertexIndex = -1;
        
        /**
         * add lon lat point to edited object
         * @param coordinate
         * @throws ParseException 
         * @throws Exception 
         */
        public void addPoint(Coordinate coordinate, int srid, boolean addToEnd)
                throws Exception, ParseException
        {
            int layerManagerSrid = mLayerManager.getSrid();

            if(layerManagerSrid != srid)
            {
                coordinate = mSpatialite.transformSRS(coordinate, srid, layerManagerSrid);
            }
            
            if(selectedVertexIndex >= 0 && !addToEnd)
            {
                vertices.add(selectedVertexIndex, coordinate);
                selectedVertexIndex++;
            }
            else
            {
                vertices.add(coordinate);
            }
        }
        
        /**
         * add lon lat points to edited objects
         * @param points
         * @throws ParseException 
         * @throws Exception 
         */
        public void addPoints(ArrayList<Coordinate> points, int srid)
                throws Exception, ParseException
        {
            int layerManagerSrid = mLayerManager.getSrid();

            if(layerManagerSrid != srid)
            {
                points = mSpatialite.transformSRS(points, srid, layerManagerSrid);
            }
            points.addAll(points);
        }
        
        /**
         * open oject for editing
         * @param rowid
         * @throws Exception
         * @throws ParseException
         */
        public void openObject(String rowid) throws Exception, ParseException
        {
            if(rowid == null)
            {
                clear();
            }
            else
            {
                try
                {
                    vertices = getPointsOfRowidObject(rowid);
                    this.rowid = rowid;
                    
                    if(mType == VectorLayerType.POINT)
                    {
                        selectedVertexIndex = 0;
                    }
                }
                catch (RuntimeException e)
                {
                    clear();
                    return;
                }
            }
        }
        
        /**
         * clear edited object
         */
        public void clear()
        {
            vertices.clear();
            rowid = null;
            selectedVertexIndex = -1;
        }
        
        /**
         * remove vertex or object
         * @throws Exception 
         * @throws NumberFormatException 
         */
        public void removeSelected() throws NumberFormatException, Exception
        {
            // remove vertex
            if(selectedVertexIndex >= 0 && mType != VectorLayerType.POINT)
            {
                vertices.remove(selectedVertexIndex);
                if(selectedVertexIndex >= vertices.size())
                {
                    selectedVertexIndex = vertices.size()-1;
                }
            }
            // remove object
            else
            {
                if(rowid != null)
                {
                    removeObject(rowid);
                }
                
                clear();
            }
        }

        /**
         * check if point is near selected point
         * @param point
         * @return
         */
        public boolean isNearSelectedVertex(Coordinate point)
        {
            if(selectedVertexIndex >= 0)
            {
                Double distance = vertices.get(selectedVertexIndex).distance(point);
                double bufferDistance = mNavigator.getBufferDistance();
                
                return distance < bufferDistance;
            }
            
            return false;
        }
        
        /**
         * set new position of selected vertex
         * position is not cloned
         * @param position
         */
        public void setPositionSelectedVertex(Coordinate position)
        {
            if(selectedVertexIndex >= 0)
            {
                vertices.set(selectedVertexIndex, position);
            }        
        }
       
        /**
         * check click on vertex of object
         * @param envelope
         * @param point
         * @throws Exception
         * @throws ParseException
         */
        public void findeSelectedVertex(Envelope envelope, Coordinate point)
                throws Exception, ParseException
        {
            selectedVertexIndex = 
                        findIndexNearestPoint(vertices, point);
        }

        /**
         * @return geometry created from vertices
         */
        public Geometry getGeometry()
        {
            return createGeometry(vertices, mType);
        }
        // getter, setter ----------------------------------
        /**
         * @return points of object
         */
        public ArrayList<Coordinate> getVertices()
        {
            return vertices;
        }
        
        /**
         * @return the rowid
         */
        public String getRowid()
        {
            return rowid;
        }

        /**
         * check if edited ebject is new or not
         * @return
         */
        public boolean isNew()
        {
            return rowid == null;
        }
        
        /**
         * @return true if edited object has loaded points
         */
        public boolean isOpened()
        {
            return vertices.size() > 0;
        }
        
        /**
         * @return true if object has enough points
         */
        public boolean hasEnoughPoints()
        {
            return vertices.size() >= getMinCountPoints();
        }
        
        /**
         * @return true if object has selected vertex
         */
        public boolean hasSelectedVertex()
        {
            return selectedVertexIndex >= 0;
        }

        /**
         * @return the selectedVertexIndex
         */
        public int getSelectedVertexIndex()
        {
            return selectedVertexIndex;
        }

        /**
         * @param vertices the vertices to set
         */
        public void setVertices(ArrayList<Coordinate> vertices)
        {
            this.vertices = vertices;
        }

        /**
         * @param rowid the rowid to set
         */
        public void setRowid(String rowid)
        {
            this.rowid = rowid;
        }

        /**
         * @param selectedVertexIndex the selectedVertexIndex to set
         */
        public void setSelectedVertexIndex(int selectedVertexIndex)
        {
            this.selectedVertexIndex = selectedVertexIndex;
        }
    }
}