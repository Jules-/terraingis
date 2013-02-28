package cz.kalcik.vojta.terraingis.layer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;

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
    
    // enum ===================================================================
    public enum VectorLayerType {POINT, LINE, POLYGON};
    
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
    
    private boolean mHaveIndex;

    protected Paint mPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteManager mSpatialite;
    protected String mGeometryColumn;
    protected VectorLayerData data = new VectorLayerData(new ArrayList<Coordinate>());

    protected GeometryFactory mGeometryFactory = new GeometryFactory();
    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, Paint paint, String name, int srid,
                       SpatiaLiteManager spatialite)
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
        mHaveIndex = mSpatialite.indexEnabled(name);
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
        Coordinate newPoint =  mSpatialite.transformSRS(coordinate,
                srid,
                mLayerManager.getSrid());
        data.mRecordedPoints.add(newPoint);
    }

    /**
     * add lon lat points to recorded objects
     * @param points
     */
    public void addPoints(ArrayList<Coordinate> points)
    {
        ArrayList<Coordinate> newPoints =  mSpatialite.transformSRS(points,
                SpatiaLiteManager.EPSG_LONLAT,
                mLayerManager.getSrid());
        data.mRecordedPoints.addAll(newPoints);
    }
    
    /**
     * end recorded object
     */
    public void endObject()
    {
        Geometry object = null;
        
        // for polygon add first point
        if(mType == VectorLayerType.POLYGON)
        {
            data.mRecordedPoints.add(data.mRecordedPoints.get(0));
        }
        
        CoordinateArraySequence coordinates =
                new CoordinateArraySequence(data.mRecordedPoints.toArray(new Coordinate[data.mRecordedPoints.size()]));
        
        if(mType == VectorLayerType.POINT)
        {
            if(data.mRecordedPoints.size() == 0)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            object = new Point(coordinates, mGeometryFactory);
        }
        else if(mType == VectorLayerType.LINE)
        {
            if(data.mRecordedPoints.size() < 2)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            object = new LineString(coordinates, mGeometryFactory);
        }
        else if(mType == VectorLayerType.POLYGON)
        {
            if(data.mRecordedPoints.size() < 4)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            LinearRing ring = new LinearRing(coordinates, mGeometryFactory);
            object = new Polygon(ring, null, mGeometryFactory);
        }
        
        mSpatialite.inserGeometry(object, super.data.name, mGeometryColumn,
                SpatiaLiteManager.EPSG_SPHERICAL_MERCATOR, mSrid);
        updateEnvelope();
        data.mRecordedPoints.clear();
    }
    
    // protected methods ========================================================
    protected Iterator<Geometry> getObjects(Envelope envelope)
    {
        return mSpatialite.getObjects(envelope, super.data.name, mGeometryColumn, mSrid,
                                      mLayerManager.getSrid(), mHaveIndex);
    }
    
    /**
     * update envelope of Layer
     */
    protected void updateEnvelope()
    {
        mEnvelope = mSpatialite.getEnvelopeLayer(super.data.name);
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
}