package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Envelope;

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
        this.srid = srid;
        this.mSpatialite = spatialite;
        mEnvelope = spatialite.getEnvelopeLayer(name);
        mColumnGeom = mSpatialite.getColumnGeom(name);
    }
    
    // public methods =========================================================
    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Envelope rect)
    {

    }
    
    @Override
    public void detach()
    {
        // TODO Auto-generated method stub
        
    }
    // private methods ========================================================
    
}