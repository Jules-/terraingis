package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.exception.TerrainGISException;

import android.graphics.Canvas;
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
    private boolean mHaveIndex;

    protected Paint mPaint;
    protected Paint mNotSavedPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteManager mSpatialite;
    protected String mColumnGeom;
    protected ArrayList<Coordinate> mRecordedPoints = new ArrayList<Coordinate>();
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
        this.mName = name;
        this.mSrid = srid;
        this.mSpatialite = spatialite;
        mColumnGeom = mSpatialite.getColumnGeom(name);
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
        return (mRecordedPoints.size() != 0);
    }
    
    /**
     * add point to recorded object
     * @param coordinate
     */
    public void addPoint(Coordinate coordinate)
    {
        Coordinate newPoint =  mSpatialite.transformSRS(coordinate,
                                                        LayerManager.EPSG_LONLAT,
                                                        LayerManager.EPSG_SPHERICAL_MERCATOR);
        mRecordedPoints.add(newPoint);
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
            mRecordedPoints.add(mRecordedPoints.get(0));
        }
        
        CoordinateArraySequence coordinates =
                new CoordinateArraySequence(mRecordedPoints.toArray(new Coordinate[mRecordedPoints.size()]));
        
        if(mType == VectorLayerType.POINT)
        {
            if(mRecordedPoints.size() == 0)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            object = new Point(coordinates, mGeometryFactory);
        }
        else if(mType == VectorLayerType.LINE)
        {
            if(mRecordedPoints.size() < 2)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            object = new LineString(coordinates, mGeometryFactory);
        }
        else if(mType == VectorLayerType.POLYGON)
        {
            if(mRecordedPoints.size() < 4)
            {
                throw new CreateObjectException("Few points for object.");
            }
            
            LinearRing ring = new LinearRing(coordinates, mGeometryFactory);
            object = new Polygon(ring, null, mGeometryFactory);
        }
        
        mSpatialite.inserGeometry(object, mName, mColumnGeom,
                                  LayerManager.EPSG_SPHERICAL_MERCATOR, mSrid);
        updateEnvelope();
        mRecordedPoints.clear();
    }
    
    // protected methods ========================================================
    protected Iterator<Geometry> getObjects(Envelope envelope)
    {
        return mSpatialite.getObjects(envelope, mName, mColumnGeom, mSrid,
                                      mLayerManager.getSrid(), mHaveIndex);
    }
    
    /**
     * update envelope of Layer
     */
    protected void updateEnvelope()
    {
        mEnvelope = mSpatialite.getEnvelopeLayer(mName);
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