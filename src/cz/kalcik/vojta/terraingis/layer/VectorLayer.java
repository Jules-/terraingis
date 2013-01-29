package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * class for vector layer
 * @author jules
 *
 */
public abstract class VectorLayer extends AbstractLayer
{
    // enum ===================================================================
    public enum VectorLayerType {POINT, LINE, POLYGON};
    
    // attributes ==============================================================
    private boolean mHaveIndex;

    protected Paint mPaint;
    protected VectorLayerType mType;
    protected SpatiaLiteManager mSpatialite;
    protected String mColumnGeom;
    
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
        mEnvelope = mSpatialite.getEnvelopeLayer(name);
        mColumnGeom = mSpatialite.getColumnGeom(name);
        mHaveIndex = mSpatialite.indexEnabled(name);
    }
    
    // public methods =========================================================    
    @Override
    public void detach()
    {
        // TODO Auto-generated method stub
        
    }
    // protected methods ========================================================
    protected ArrayList<Geometry> getObjects(Envelope envelope)
    {
        return mSpatialite.getObjects(envelope, mName, mColumnGeom, mSrid,
                                      mLayerManager.getSrid(), mHaveIndex);
    }
}