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

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.dialogs.InsertAttributesDialog.InsertObjectType;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.ShapeFileRecord;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
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
    
    // enum ===================================================================
    public enum VectorLayerType
    {
        POINT, LINE, POLYGON;
        
        public String getSpatialiteType()
        {
            if(this == POINT)
            {
                return "POINT";
            }
            else if(this == LINE)
            {
                return "LINESTRING";
            }
            else if(this == POLYGON)
            {
                return "POLYGON";
            }
            
            return null;
        }
    };
    
    // attributes ==============================================================
    protected static class VectorLayerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public ArrayList<Coordinate> recordedPoints;
        public String selectedRowid;
        public ArrayList<Coordinate> selectedObjectPoints; // in srid of map
        public int selectedNodeIndex;
        public Coordinate clickedPoint;

        public VectorLayerData(ArrayList<Coordinate> recordedPoints,
                ArrayList<Coordinate> selectedObjectPoints, int selectedNodeIndex)
        {
            this.recordedPoints = recordedPoints;
            this.selectedObjectPoints = selectedObjectPoints;
            this.selectedNodeIndex = selectedNodeIndex;
        }
    }
    
    private boolean mHasIndex;

    protected Paint mPaint;
    protected Paint mPaintPoint;
    protected Paint mSelectedPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData vectorLayerData = new VectorLayerData(new ArrayList<Coordinate>(),
            new ArrayList<Coordinate>(), -1);
    protected AttributeHeader mAttributeHeader;
    protected MapFragment mMapFragment;
    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, String name, int srid,
                       SpatiaLiteIO spatialite, MapFragment mapFragment)
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
        updateEnvelope();
    }

    // abstract methods =======================================================
    abstract protected int getMinCountPoints();
    
    // public methods =========================================================    
    @Override
    public void detach()
    {

    }
    
    /**
     * @return if layer has opened recorded object
     */
    public boolean hasOpenedRecordObject()
    {
        return (vectorLayerData.recordedPoints.size() != 0);
    }
    
    /**
     * add lon lat point to recorded object
     * @param coordinate
     */
    public void addPoint(Coordinate coordinate, int srid)
    {
        int layerManagerSrid = mLayerManager.getSrid();
        if(layerManagerSrid != srid)
        {
            coordinate = mSpatialite.transformSRS(coordinate, srid, layerManagerSrid);
        }
        vectorLayerData.recordedPoints.add(coordinate);
    }

    /**
     * add lon lat points to recorded objects
     * @param points
     */
    public void addPointsRecording(ArrayList<Coordinate> points, int srid)
    {
        int layerManagerSrid = mLayerManager.getSrid();
        if(layerManagerSrid != srid)
        {
            points = mSpatialite.transformSRS(points, srid, layerManagerSrid);
        }
        vectorLayerData.recordedPoints.addAll(points);
    }
    
    /**
     * insert recorded object
     * @param attributes
     */
    public void insertRecordedObject(AttributeRecord attributes)
    {
        insertObject(attributes, createRecordedGeometry());
        vectorLayerData.recordedPoints.clear(); 
    }

    /**
     * insert edited object
     * @param attributes
     */
    public void insertEditedObject(AttributeRecord attributes)
    {
        insertObject(attributes, createGeometry(vectorLayerData.selectedObjectPoints, mType));
        vectorLayerData.selectedObjectPoints.clear(); 
    }
    
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
        updateEnvelope();
    }
    
    /**
     * remove layer from db
     */
    public void remove()
    {
        mSpatialite.removeLayer(super.data.name, mGeometryColumn, mHasIndex);
    }
    
    /**
     * set ROWID of clicked object
     * @param point
     * @param bufferDistance
     */
    public void clickedObject(Envelope envelope, Coordinate point)
    {
        vectorLayerData.clickedPoint = point;
        double bufferDistance = mNavigator.getBufferDistance();
        String rowid = mSpatialite.getRowidNearCoordinate(envelope, data.name,
                mGeometryColumn, mSrid, mLayerManager.getSrid(), mHasIndex, point, bufferDistance);
        changeSelectionOfObject(rowid);
        
        if(vectorLayerData.selectedRowid != null)
        {
            loadSelectedPoints();
        }
    }
    
    /**
     * remove selected rowid
     */
    public void removeSelectionOfObject()
    {
        changeSelectionOfObject(null);
    }
    
    /**
     * insert point to selected layer by edit
     * @param point
     */
    public void addPointEdit(Coordinate point)
    {
        if(mType == VectorLayerType.POINT)
        {
            removeSelectionOfObject();
            vectorLayerData.selectedObjectPoints.clear();
            vectorLayerData.selectedObjectPoints.add(point);
        }
        else
        {
            if(vectorLayerData.selectedNodeIndex >= 0)
            {
                vectorLayerData.selectedObjectPoints.add(vectorLayerData.selectedNodeIndex, point);
                vectorLayerData.selectedNodeIndex++;
            }
            else
            {
                vectorLayerData.selectedObjectPoints.add(point);
            }
        }
    }
    
    /**
     * remove selected object
     */
    public void removeSelected()
    {
        if(vectorLayerData.selectedNodeIndex >= 0)
        {
            vectorLayerData.selectedObjectPoints.remove(vectorLayerData.selectedNodeIndex);
            if(vectorLayerData.selectedNodeIndex >= vectorLayerData.selectedObjectPoints.size())
            {
                vectorLayerData.selectedNodeIndex = vectorLayerData.selectedObjectPoints.size()-1;
            }
        }
        else
        {
            if(vectorLayerData.selectedRowid != null)
            {
                try
                {
                    mSpatialite.removeObject(data.name, Integer.parseInt(vectorLayerData.selectedRowid));
                }
                catch (Exception e)
                {
                    Toast.makeText(mMapFragment.getActivity(), R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
            }
            
            vectorLayerData.selectedObjectPoints.clear();
        }
    }
    
    /**
     * cancel not saved changes
     */
    public void cancelNotSavedChanges()
    {
        if(vectorLayerData.selectedRowid != null)
        {
            loadSelectedPoints();
        }
        else
        {
            vectorLayerData.selectedObjectPoints.clear();
        }
    }
    
    /**
     * check if point is near selected point
     * @param point
     * @return
     */
    public boolean isNearSelectedPoint(Coordinate point)
    {
        if(vectorLayerData.selectedNodeIndex >= 0)
        {
            Double distance = vectorLayerData.selectedObjectPoints.get(
                    vectorLayerData.selectedNodeIndex).distance(point);
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
        if(vectorLayerData.selectedNodeIndex >= 0)
        {
            vectorLayerData.selectedObjectPoints.set(
                    vectorLayerData.selectedNodeIndex, position);
        }        
    }
    
    /**
     * @return true if recorded object has enough points
     */
    public boolean hasRecordedObjectEnoughPoints()
    {
        return vectorLayerData.recordedPoints.size() >= getMinCountPoints();
    }

    /**
     * @return true if selected object has enough points
     */
    public boolean hasSelectedObjectEnoughPoints()
    {
        return vectorLayerData.selectedObjectPoints.size() >= getMinCountPoints();
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
    public String getGeometrColumn()
    {
        return mGeometryColumn;
    }

    
    /**
     * @return the data
     */
    public AbstractLayerData getData()
    {
        AbstractLayerData result = super.getData();
        result.childData = vectorLayerData;
        return result;
    }

    /**
     * @param vectorLayerData the data to set
     */
    public void setData(AbstractLayerData inData)
    {
        super.setData(inData);
        vectorLayerData = (VectorLayerData)data.childData;
    }
    
    
    /**
     * @return the mAttributeHeader
     */
    public AttributeHeader getAttributeHeader()
    {
        return mAttributeHeader;
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
    {
        return mSpatialite.getObjects(envelope, super.data.name, mGeometryColumn, mSrid,
                                      mLayerManager.getSrid(), mHasIndex);
    }
    
    /**
     * update envelope of Layer
     */
    protected void updateEnvelope()
    {
        mEnvelope = mSpatialite.getEnvelopeLayer(super.data.name, mGeometryColumn, mHasIndex);
    }
    
    /**
     * check if object is selected
     * @param geomIterator
     * @return
     */
    protected boolean isSelectedObject(SpatialiteGeomIterator geomIterator)
    {
        return geomIterator.getLastROWID().equals(vectorLayerData.selectedRowid);
    }
    
    /**
     * return paint by selection of object
     * @param geomIterator
     * @return
     */
    protected Paint selectObjectPaint(SpatialiteGeomIterator geomIterator)
    {
        if(geomIterator.getLastROWID().equals(vectorLayerData.selectedRowid))
        {
            return mSelectedPaint;
        }
        else
        {
            return mPaint;
        }
    }
    // private methods ===========================================================
    
    /**
     * onvert points to geometry
     * @return
     */
    private Geometry createRecordedGeometry()
    {
        return createGeometry(vectorLayerData.recordedPoints, mType);
    }
    
    /**
     * set paints by type
     */
    private void setPaints()
    {
        mPaintPoint = VectorLayerPaints.getPoint(PaintType.SELECTED_SELECTED_NODE);
        
        if(mType == VectorLayerType.POINT)
        {
            mPaint = VectorLayerPaints.getPoint(PaintType.DEFAULT);
            mSelectedPaint = VectorLayerPaints.getPoint(PaintType.SELECTED);
        }
        else if(mType == VectorLayerType.LINE)
        {
            mPaint = VectorLayerPaints.getLine(PaintType.DEFAULT);
            mSelectedPaint = VectorLayerPaints.getLine(PaintType.SELECTED);
            mNotSavedPaint = VectorLayerPaints.getLine(PaintType.NOT_SAVED);
        }
        else if(mType == VectorLayerType.POLYGON)
        {
            mPaint = VectorLayerPaints.getPolygon(PaintType.DEFAULT);
            mSelectedPaint = VectorLayerPaints.getPolygon(PaintType.SELECTED);
            mNotSavedPaint = VectorLayerPaints.getPolygon(PaintType.NOT_SAVED);
        }           
    }
    
    /**
     * check if is selected node and set his index 
     * @param point
     * @param bufferDistance
     */
    private void checkSelectedNode(Coordinate point, Double bufferDistance)
    {
        if(mType == VectorLayerType.POINT)
        {
            vectorLayerData.selectedNodeIndex = 0;
            return;
        }
        
        int size = vectorLayerData.selectedObjectPoints.size();
        double minDistance = bufferDistance;
        int minIndex = -1;
        
        for(int i=0; i < size; i++)
        {
            Double distance = vectorLayerData.selectedObjectPoints.get(i).distance(point);
            if(distance < minDistance)
            {
                minDistance = distance;
                minIndex = i;
            }
        }
        
        vectorLayerData.selectedNodeIndex = minIndex;
    }
    
    /**
     * insert object to DB
     * @param attributes
     * @param geometry
     */
    private void insertObject(AttributeRecord attributes, Geometry geometry)
    {
        mSpatialite.insertObject(geometry, data.name, mGeometryColumn,
                mLayerManager.getSrid(), mSrid,
                mAttributeHeader, attributes, false);
        
        updateEnvelope();
    }
    
    /**
     * set selection to new rowid
     * @param rowid
     * @throws Exception 
     * @throws NumberFormatException 
     */
    private void changeSelectionOfObject(String rowid)
    {
        if(!vectorLayerData.selectedObjectPoints.isEmpty())
        {
            
            if(vectorLayerData.selectedRowid == null)
            {
                if(hasSelectedObjectEnoughPoints())
                {
                    mMapFragment.endObject(this, InsertObjectType.EDITING);
                }
                else
                {
                    vectorLayerData.selectedObjectPoints.clear();
                }
            }
            else if(vectorLayerData.selectedRowid != null)
            {                
                try
                {
                    if(hasSelectedObjectEnoughPoints())
                    {
                        Geometry geometry = createGeometry(vectorLayerData.selectedObjectPoints, mType);
                        mSpatialite.updateObject(data.name, mGeometryColumn,
                                Integer.parseInt(vectorLayerData.selectedRowid), geometry,
                                mSrid, mLayerManager.getSrid());
                    }
                    else
                    {
                        mSpatialite.removeObject(data.name, Integer.parseInt(vectorLayerData.selectedRowid));
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(mMapFragment.getActivity(), R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
                
                vectorLayerData.selectedObjectPoints.clear();
            }
        }
        
        vectorLayerData.selectedNodeIndex = -1;
        vectorLayerData.selectedRowid = rowid;
    }
    

    /**
     * load points of selected object
     * rowid can not be null
     */
    private void loadSelectedPoints()
    {
        double bufferDistance = mNavigator.getBufferDistance();
        Geometry object = mSpatialite.getObject(data.name, mGeometryColumn,
                mLayerManager.getSrid(), Integer.parseInt(vectorLayerData.selectedRowid));
        vectorLayerData.selectedObjectPoints = new ArrayList<Coordinate>(Arrays.asList(object.getCoordinates()));
        
        if(mType == VectorLayerType.POLYGON)
        {
            vectorLayerData.selectedObjectPoints.remove(
                    vectorLayerData.selectedObjectPoints.size()-1);
        }
        
        checkSelectedNode(vectorLayerData.clickedPoint, bufferDistance);
    }
}