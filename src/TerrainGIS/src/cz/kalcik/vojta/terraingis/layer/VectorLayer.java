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
    private static final float[] DASHED_PARAMS = {10, 5};
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
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteIO mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData data = new VectorLayerData(new ArrayList<Coordinate>());
    protected AttributeHeader mAttributeHeader;
    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, Paint paint, String name, int srid,
                       SpatiaLiteIO spatialite)
    {
        this.mType = type;
        if(paint == null)
        {
            paint = new Paint();
        }
        
        this.mPaint = paint;        
        super.data.name = name;
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
        result.childData = data;
        return result;
    }

    /**
     * @param data the data to set
     */
    public void setData(AbstractLayerData inData)
    {
        super.setData(inData);
        data = (VectorLayerData)super.data.childData;
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
        return (data.mRecordedPoints.size() != 0);
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
        data.mRecordedPoints.add(coordinate);
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
        data.mRecordedPoints.addAll(points);
    }
    
    /**
     * end recorded object
     */
    public void endObject()
    {
        mSpatialite.insertObject(createGeometry(), super.data.name, mGeometryColumn,
                SpatiaLiteIO.EPSG_SPHERICAL_MERCATOR, mSrid,
                mAttributeHeader.getInsertSQLColumns(true),
                mAttributeHeader.getInsertSQLArgs(true), null);
        updateEnvelope();
        data.mRecordedPoints.clear();
    }
    
    public void importObjects(Iterator<ShapeFileRecord> iterGeometries) throws Exception
    {
        Stmt stmt = mSpatialite.prepareInsert(super.data.name, mGeometryColumn, mSrid, mSrid,
                mAttributeHeader.getInsertSQLColumns(true),
                mAttributeHeader.getInsertSQLArgs(true));
        
        while(iterGeometries.hasNext())
        {
            ShapeFileRecord record = iterGeometries.next();
            AttributeRecord values = new AttributeRecord(mAttributeHeader, record.getAttributes());
            values.trimValues();
            mSpatialite.insertObject(stmt, record.getGeometry(), values);
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
    protected Iterator<Geometry> getObjects(Envelope envelope)
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
     * create paint with dashed line from paint
     * @param paint
     * @return
     */
    protected void setDashedPath(Paint paint)
    {
        paint.setPathEffect(new DashPathEffect(DASHED_PARAMS, 0));
    }
    
    // private methods ===========================================================
    
    /**
     * onvert points to geometry
     * @return
     */
    private Geometry createGeometry()
    {
        return createGeometry(data.mRecordedPoints, mType);
    }
}