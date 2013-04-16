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

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.dialogs.InsertAttributesDialog.InsertObjectType;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.ShapeFileRecord;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteAttributesIterator;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.widget.Toast;

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
        
        public ArrayList<Coordinate> recordedPoints;
        public String recordedRowid = null;
        public int recordedVertexIndex = -1;
        public String selectedRowid;
        public ArrayList<Coordinate> selectedObjectPoints; // in srid of map
        public int selectedVertexIndex;
        public Coordinate clickedPoint;
        public boolean selectVertex = false;

        public VectorLayerData(ArrayList<Coordinate> recordedPoints,
                ArrayList<Coordinate> selectedObjectPoints, int selectedNodeIndex)
        {
            this.recordedPoints = recordedPoints;
            this.selectedObjectPoints = selectedObjectPoints;
            this.selectedVertexIndex = selectedNodeIndex;
        }
    }
    
    private boolean mHasIndex;

    protected Paint mPaint;
    protected Paint mSelectedPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData mVectorLayerData = new VectorLayerData(new ArrayList<Coordinate>(),
            new ArrayList<Coordinate>(), -1);
    protected AttributeHeader mAttributeHeader;
    protected MapFragment mMapFragment;
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
     * add lon lat point to recorded object
     * @param coordinate
     * @throws ParseException 
     * @throws Exception 
     */
    public void addPoint(Coordinate coordinate, int srid)
            throws Exception, ParseException
    {
        int layerManagerSrid = mLayerManager.getSrid();

        if(layerManagerSrid != srid)
        {
            coordinate = mSpatialite.transformSRS(coordinate, srid, layerManagerSrid);
        }
        mVectorLayerData.recordedPoints.add(coordinate);
    }

    /**
     * add lon lat points to recorded objects
     * @param points
     * @throws ParseException 
     * @throws Exception 
     */
    public void addPointsRecording(ArrayList<Coordinate> points, int srid)
            throws Exception, ParseException
    {
        int layerManagerSrid = mLayerManager.getSrid();

        if(layerManagerSrid != srid)
        {
            points = mSpatialite.transformSRS(points, srid, layerManagerSrid);
        }
        mVectorLayerData.recordedPoints.addAll(points);
    }
    
    /**
     * insert recorded object
     * @param attributes
     * @throws Exception 
     */
    public void insertRecordedObject(AttributeRecord attributes)
            throws Exception
    {
        insertObject(attributes, createRecordedGeometry());
        
        clearRecorded();
    }
    
    /**
     * update recorded object
     * @throws NumberFormatException
     * @throws Exception
     */
    public void updateRecordedObject() throws NumberFormatException, Exception
    {
        Geometry geometry = createGeometry(mVectorLayerData.recordedPoints, mType);
        mSpatialite.updateObject(data.name, mGeometryColumn,
                Integer.parseInt(mVectorLayerData.recordedRowid), geometry,
                mSrid, mLayerManager.getSrid());
        clearRecorded();
        
        updateLayerAttributes();
    }

    /**
     * @return true if object is not saved
     */
    public boolean isRecordedObjectNew()
    {
        return (mVectorLayerData.recordedRowid == null);
    }
    
    /**
     * insert edited object
     * @param attributes
     * @throws Exception 
     */
    public void insertEditedObject(AttributeRecord attributes)
            throws Exception
    {
        insertObject(attributes, createGeometry(mVectorLayerData.selectedObjectPoints, mType));
        clearSelected();
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
    public void clickSelectionObject(Envelope envelope, Coordinate point, boolean selectVertex)
            throws Exception, ParseException
    {
        mVectorLayerData.clickedPoint = point;
        mVectorLayerData.selectVertex = selectVertex;
        
        String rowid = getNearestObjectToPoint(envelope, point);
        
        changeSelectionOfObject(rowid);
    }
    
    /**
     * open recording object
     * @param envelope
     * @param point
     * @throws Exception
     * @throws ParseException
     */
    public void clickRecordingObject(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        mVectorLayerData.recordedRowid = getNearestObjectToPoint(envelope, point);
        
        if(mVectorLayerData.recordedRowid != null)
        {
            try
            {
                mVectorLayerData.recordedPoints =
                        getPointsOfRowidObject(mVectorLayerData.recordedRowid);
            }
            catch (RuntimeException e)
            {
                clearRecorded();
                return;
            }
        }
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
        mVectorLayerData.selectVertex = false;
        
        changeSelectionOfObject(rowid);
    }
    
    /**
     * remove selected rowid
     * @throws Exception 
     * @throws ParseException 
     */
    public void removeSelectionOfObject() throws Exception, ParseException
    {
        changeSelectionOfObject(null);
    }
    
    /**
     * insert point to selected layer by edit
     * @param point
     * @throws Exception 
     * @throws ParseException 
     */
    public void addPointEdit(Coordinate point)
            throws Exception, ParseException
    {
        if(mType == VectorLayerType.POINT)
        {
            removeSelectionOfObject();
            clearSelected();
            mVectorLayerData.selectedObjectPoints.add(point);
        }
        else
        {
            if(mVectorLayerData.selectedVertexIndex >= 0)
            {
                mVectorLayerData.selectedObjectPoints.add(mVectorLayerData.selectedVertexIndex, point);
                mVectorLayerData.selectedVertexIndex++;
            }
            else
            {
                mVectorLayerData.selectedObjectPoints.add(point);
            }
        }
    }
    
    /**
     * remove selected object
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public void removeSelected() throws Exception
    {
        if(mVectorLayerData.selectedVertexIndex >= 0 && mType != VectorLayerType.POINT)
        {
            mVectorLayerData.selectedObjectPoints.remove(mVectorLayerData.selectedVertexIndex);
            if(mVectorLayerData.selectedVertexIndex >= mVectorLayerData.selectedObjectPoints.size())
            {
                mVectorLayerData.selectedVertexIndex = mVectorLayerData.selectedObjectPoints.size()-1;
            }
        }
        else
        {
            if(mVectorLayerData.selectedRowid != null)
            {
                removeObject(mVectorLayerData.selectedRowid);
            }
            
            clearSelected();
        }
    }
    
    /**
     * cancel not saved changes
     * @throws ParseException 
     * @throws Exception 
     */
    public void cancelNotSavedEditedChanges()
            throws Exception, ParseException
    {
        if(mVectorLayerData.selectedRowid != null)
        {
            loadSelectedPoints();
        }
        else
        {
            clearSelected();
        }
    }
    
    public void cancelNotSavedRecordedChanges()
    {
        clearRecorded();
    }
    
    /**
     * check if point is near selected point
     * @param point
     * @return
     */
    public boolean isNearSelectedPoint(Coordinate point)
    {
        if(mVectorLayerData.selectedVertexIndex >= 0)
        {
            Double distance = mVectorLayerData.selectedObjectPoints.get(
                    mVectorLayerData.selectedVertexIndex).distance(point);
            double bufferDistance = mNavigator.getBufferDistance();
            
            return distance < bufferDistance;
        }
        
        return false;
    }
    
    /**
     * set new position of selected point
     * position is not cloned
     * @param position
     */
    public void setPositionSelectedPoint(Coordinate position)
    {
        if(mVectorLayerData.selectedVertexIndex >= 0)
        {
            mVectorLayerData.selectedObjectPoints.set(
                    mVectorLayerData.selectedVertexIndex, position);
        }        
    }
    
    /**
     * check click on vertex of polygon
     * @param envelope
     * @param point
     * @throws Exception
     * @throws ParseException
     */
    public void checkClickRecordedVertex(Envelope envelope, Coordinate point)
            throws Exception, ParseException
    {
        if(mType == VectorLayerType.POINT)
        {
            clearRecorded();
            clickRecordingObject(envelope, point);
            if(mVectorLayerData.recordedRowid != null)
            {
                mVectorLayerData.recordedVertexIndex = 0;
            }
        }
        else
        {
            mVectorLayerData.recordedVertexIndex = 
                    findIndexNearestPoint(mVectorLayerData.recordedPoints, point);
        }
    }
    
    /**
     * move vertex to new position
     * @param point
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public void moveRecordedVertex(Coordinate point) throws NumberFormatException, Exception
    {
        if(mVectorLayerData.recordedVertexIndex != -1)
        {
            mVectorLayerData.recordedPoints.set(mVectorLayerData.recordedVertexIndex,
                    (Coordinate) point.clone());
            
            if(mType == VectorLayerType.POINT)
            {
                updateRecordedObject();
            }
        }
        
        mVectorLayerData.recordedVertexIndex = -1;
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
    public boolean hasRecordedObjectEnoughPoints()
    {
        return mVectorLayerData.recordedPoints.size() >= getMinCountPoints();
    }

    /**
     * @return true if selected object has enough points
     */
    public boolean hasSelectedObjectEnoughPoints()
    {
        return mVectorLayerData.selectedObjectPoints.size() >= getMinCountPoints();
    }
    
    /**
     * @return true if is selected point for move
     */
    public boolean hasRecordedMovableVertex()
    {
        return mVectorLayerData.recordedVertexIndex != -1;
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
     * @return true if layer has opened recorded object
     */
    public boolean hasOpenedRecordObject()
    {
        return (mVectorLayerData.recordedPoints.size() != 0);
    }
    
    
    /**
     * @return if layer has selected object
     */
    public boolean hasSelectedObject()
    {
        return (mVectorLayerData.selectedObjectPoints.size() != 0);
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
        return mSpatialite.getObjectsInEnvelope(envelope, super.data.name, mGeometryColumn, mSrid,
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
    protected boolean isRecordedObject(SpatialiteGeomIterator geomIterator)
    {
        return geomIterator.getLastROWID().equals(mVectorLayerData.recordedRowid);
    }
    // private methods ===========================================================
    
    /**
     * onvert points to geometry
     * @return
     */
    private Geometry createRecordedGeometry()
    {
        return createGeometry(mVectorLayerData.recordedPoints, mType);
    }
    
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
     * check if is selected node and set his index 
     * @param point
     * @param bufferDistance
     */
    private void checkSelectedVertex(Coordinate point)
    {
        if(mType == VectorLayerType.POINT)
        {
            mVectorLayerData.selectedVertexIndex = 0;
            return;
        }
        
        mVectorLayerData.selectedVertexIndex =
                findIndexNearestPoint(mVectorLayerData.selectedObjectPoints, point);
    }
    
    /**
     * find index of nearest point
     * @param points
     * @param point
     * @return
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
     * set selection to new rowid
     * @param rowid
     * @throws Exception 
     * @throws ParseException 
     * @throws NumberFormatException 
     */
    private void changeSelectionOfObject(String rowid)
            throws Exception, ParseException
    {
        if(!mVectorLayerData.selectedObjectPoints.isEmpty())
        {
            
            if(mVectorLayerData.selectedRowid == null)
            {
                if(hasSelectedObjectEnoughPoints())
                {
                    mMapFragment.endNewObject(this, InsertObjectType.EDITING);
                }
                else
                {
                    clearSelected();
                }
            }
            else
            {                
                if(hasSelectedObjectEnoughPoints())
                {
                    Geometry geometry = createGeometry(mVectorLayerData.selectedObjectPoints, mType);
                    mSpatialite.updateObject(data.name, mGeometryColumn,
                            Integer.parseInt(mVectorLayerData.selectedRowid), geometry,
                            mSrid, mLayerManager.getSrid());
                }
                else
                {
                    removeObject(mVectorLayerData.selectedRowid);
                }
                
                clearSelected();
            }
        }
        
        mVectorLayerData.selectedVertexIndex = -1;
        mVectorLayerData.selectedRowid = rowid;
        
        if(mVectorLayerData.selectedRowid != null)
        {
            loadSelectedPoints();
        }
    }
    

    /**
     * load points of selected object
     * rowid can not be null
     * @throws ParseException 
     * @throws Exception
     */
    private void loadSelectedPoints()
            throws Exception, ParseException
    {
        try
        {
            mVectorLayerData.selectedObjectPoints = getPointsOfRowidObject(mVectorLayerData.selectedRowid);
        }
        catch (RuntimeException e)
        {
            clearSelected();
            return;
        }

        // selection of vertex
        if(mVectorLayerData.selectVertex)
        {
            checkSelectedVertex(mVectorLayerData.clickedPoint);
        }
        else
        {
            mVectorLayerData.selectedVertexIndex = -1;
        }
    }
    
    /**
     * @param envelope
     * @param point
     * @return nearest object to point if is in buffer
     * @throws Exception
     * @throws ParseException
     */
    private String getNearestObjectToPoint(Envelope envelope, Coordinate point)
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
    
    /**
     * clear recorded points
     */
    private void clearRecorded()
    {
        mVectorLayerData.recordedPoints.clear();
        mVectorLayerData.recordedRowid = null;
        mVectorLayerData.recordedVertexIndex = -1;
    }
    
    /**
     * clear selected points
     */
    private void clearSelected()
    {
        mVectorLayerData.selectedObjectPoints.clear();
        mVectorLayerData.selectedRowid = null;
        mVectorLayerData.selectedVertexIndex = -1;
    }
}