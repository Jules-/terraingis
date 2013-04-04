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
    public static class VectorLayerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public ArrayList<Coordinate> recordedPoints;
        public String selectedRowid;
        public ArrayList<Coordinate> selectedObjectPoints; // in srid of map
        public int selectedNodeIndex;

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
    protected Paint mSelectedPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData childData = new VectorLayerData(new ArrayList<Coordinate>(),
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

    // public methods =========================================================    
    @Override
    public void detach()
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * @return if layer has opened recorded object
     */
    public boolean haveOpenedRecordObject()
    {
        return (childData.recordedPoints.size() != 0);
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
        childData.recordedPoints.add(coordinate);
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
        childData.recordedPoints.addAll(points);
    }
    
    /**
     * insert recorded object
     * @param attributes
     */
    public void insertRecordedObject(AttributeRecord attributes)
    {
        insertObject(attributes, createRecordedGeometry());
        childData.recordedPoints.clear(); 
    }

    /**
     * insert edited object
     * @param attributes
     */
    public void insertEditedObject(AttributeRecord attributes)
    {
        insertObject(attributes, createGeometry(childData.selectedObjectPoints, mType));
        childData.selectedObjectPoints.clear(); 
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
        double bufferDistance = mNavigator.pxToM(ConvertUnits.dp2px(Settings.DP_RADIUS_CLICK));
        String rowid = mSpatialite.getRowidNearCoordinate(envelope, data.name,
                mGeometryColumn, mSrid, mLayerManager.getSrid(), mHasIndex, point, bufferDistance);
        changeSelectionOfObject(rowid);
        
        if(childData.selectedRowid != null)
        {
            Geometry object = mSpatialite.getObject(data.name, mGeometryColumn,
                    mLayerManager.getSrid(), Integer.parseInt(childData.selectedRowid));
            childData.selectedObjectPoints = new ArrayList<Coordinate>(Arrays.asList(object.getCoordinates()));
            if(mType != VectorLayerType.POINT)
            {
                checkSelectedNode(point, bufferDistance);
            }
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
            childData.selectedObjectPoints.clear();
            childData.selectedObjectPoints.add(point);
        }
        else
        {
            if(childData.selectedNodeIndex > 0)
            {
                childData.selectedObjectPoints.add(childData.selectedNodeIndex, point);
                childData.selectedNodeIndex++;
            }
            else
            {
                childData.selectedObjectPoints.add(point);
            }
        }
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
            if(points.size() == 0)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            result = new Point(coordinates, GEOMETRY_FACTORY);
        }
        else if(type == VectorLayerType.LINE)
        {
            if(points.size() < 2)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            result = new LineString(coordinates, GEOMETRY_FACTORY);
        }
        else if(type == VectorLayerType.POLYGON)
        {
            if(points.size() < 4)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            LinearRing ring = new LinearRing(coordinates, GEOMETRY_FACTORY);
            result = new Polygon(ring, null, GEOMETRY_FACTORY);
        }
        
        return result;
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
        result.childData = childData;
        return result;
    }

    /**
     * @param childData the data to set
     */
    public void setData(AbstractLayerData inData)
    {
        super.setData(inData);
        childData = (VectorLayerData)data.childData;
    }
    
    
    /**
     * @return the mAttributeHeader
     */
    public AttributeHeader getAttributeHeader()
    {
        return mAttributeHeader;
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
        return geomIterator.getLastROWID().equals(childData.selectedRowid);
    }
    
    /**
     * return paint by selection of object
     * @param geomIterator
     * @return
     */
    protected Paint selectObjectPaint(SpatialiteGeomIterator geomIterator)
    {
        if(geomIterator.getLastROWID().equals(childData.selectedRowid))
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
        return createGeometry(childData.recordedPoints, mType);
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
        int size = childData.selectedObjectPoints.size();
        double minDistance = bufferDistance;
        int minIndex = -1;
        
        for(int i=0; i < size; i++)
        {
            Double distance = childData.selectedObjectPoints.get(i).distance(point);
            if(distance < minDistance)
            {
                minDistance = distance;
                minIndex = i;
            }
        }
        
        childData.selectedNodeIndex = minIndex;
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
        if(!childData.selectedObjectPoints.isEmpty())
        {
            
            if(childData.selectedRowid == null)
            {
                mMapFragment.endObject(this, InsertObjectType.EDITING);
            }
            else if(childData.selectedRowid != null)
            {
                Geometry geometry = createGeometry(childData.selectedObjectPoints, mType);
                
                try
                {
                    mSpatialite.updateObject(data.name, mGeometryColumn,
                            Integer.parseInt(childData.selectedRowid), geometry,
                            mSrid, mLayerManager.getSrid());
                }
                catch (Exception e)
                {
                    Toast.makeText(mMapFragment.getActivity(), R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
                
                childData.selectedObjectPoints.clear();
            }
        }
        
        childData.selectedRowid = rowid;
    }
}