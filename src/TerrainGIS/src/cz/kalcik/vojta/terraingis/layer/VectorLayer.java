package cz.kalcik.vojta.terraingis.layer;

import java.io.Serializable;
import java.util.ArrayList;
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

import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.io.ShapeFileRecord;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;

import android.graphics.DashPathEffect;
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
        
        public ArrayList<Coordinate> mRecordedPoints;

        public VectorLayerData(ArrayList<Coordinate> mRecordedPoints)
        {
            this.mRecordedPoints = mRecordedPoints;
        }
    }
    
    private boolean mHasIndex;

    protected Paint mPaint;
    protected Paint mSelectedPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData childData = new VectorLayerData(new ArrayList<Coordinate>());
    protected AttributeHeader mAttributeHeader;
    protected String mSelectedRowid;
    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, String name, int srid,
                       SpatiaLiteIO spatialite)
    {
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
        return (childData.mRecordedPoints.size() != 0);
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
        childData.mRecordedPoints.add(coordinate);
    }

    /**
     * add lon lat points to recorded objects
     * @param points
     */
    public void addPoints(ArrayList<Coordinate> points, int srid)
    {
        int layerManagerSrid = mLayerManager.getSrid();
        if(layerManagerSrid != srid)
        {
            points = mSpatialite.transformSRS(points, srid, layerManagerSrid);
        }
        childData.mRecordedPoints.addAll(points);
    }
    
    /**
     * end recorded object
     */
    public void endObject(AttributeRecord record)
    {
        mSpatialite.insertObject(createGeometry(), super.data.name, mGeometryColumn,
                SpatiaLiteIO.EPSG_SPHERICAL_MERCATOR, mSrid,
                mAttributeHeader, record, false);
        updateEnvelope();
        childData.mRecordedPoints.clear();
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
    public void clickedObject(Envelope envelope, Coordinate point, double bufferDistance)
    {
        mSelectedRowid = mSpatialite.getRowidNearCoordinate(envelope, data.name,
                mGeometryColumn, mSrid, mLayerManager.getSrid(), mHasIndex, point, bufferDistance);
    }
    
    /**
     * remove selected rowid
     */
    public void removeSelectionOfObject()
    {
        mSelectedRowid = null;
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
        return geomIterator.getLastROWID().equals(mSelectedRowid);
    }
    
    /**
     * return paint by selection of object
     * @param geomIterator
     * @return
     */
    protected Paint selectObjectPaint(SpatialiteGeomIterator geomIterator)
    {
        if(geomIterator.getLastROWID().equals(mSelectedRowid))
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
    private Geometry createGeometry()
    {
        return createGeometry(childData.mRecordedPoints, mType);
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
}