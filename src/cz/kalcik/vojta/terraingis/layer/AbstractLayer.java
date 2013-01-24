package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Envelope;

import android.graphics.Canvas;

/**
 * parent of all layers
 * @author jules
 *
 */
public abstract class AbstractLayer
{
    // attributes ==============================================================
    protected String mName;
    protected Envelope mEnvelope;
    protected int srid;
    
    // abstract methods ========================================================
    public abstract void draw(final Canvas canvas, Envelope rect);
    public abstract void detach();
    
    // getter, setter ==========================================================
    public Envelope getEnvelope()
    {
        return mEnvelope;
    }

    public int getSrid()
    {
        return srid;
    }
    
    // public methods =========================================================
    @Override
    public String toString()
    {
        return mName;
    }
}