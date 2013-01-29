package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.components.Drawer;

import android.R.bool;
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
    protected int mSrid;
    protected Drawer mDrawer = Drawer.getInstance();
    protected LayerManager mLayerManager = LayerManager.getInstance();
    protected boolean mVisible = true;
    
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
        return mSrid;
    }
    
    /**
     * Is layer visible?
     * @return
     */
    public boolean isVisible()
    {
        return mVisible;
    }
    
    // public methods =========================================================
    @Override
    public String toString()
    {
        return mName;
    }
    
    /**
     * Toggle visibility
     */
    public void toggleVisibility()
    {
        mVisible = !mVisible;
    }
}