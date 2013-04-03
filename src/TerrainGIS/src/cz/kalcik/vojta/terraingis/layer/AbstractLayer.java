package cz.kalcik.vojta.terraingis.layer;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.components.Navigator;

import android.graphics.Canvas;

/**
 * parent of all layers
 * @author jules
 *
 */
public abstract class AbstractLayer
{
    // attributes ==============================================================
    public static class AbstractLayerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public boolean visible;
        public String name;
        public Serializable childData;

        public AbstractLayerData(boolean mVisible)
        {
            this.visible = mVisible;
        }
    }
    
    protected Envelope mEnvelope;
    protected int mSrid;
    protected Drawer mDrawer = Drawer.getInstance();
    protected LayerManager mLayerManager = LayerManager.getInstance();
    protected AbstractLayerData data = new AbstractLayerData(true);
    protected Navigator mNavigator = Navigator.getInstance();

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
        return data.visible;
    }
    
    /**
     * @return the data
     */
    public AbstractLayerData getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(AbstractLayerData data)
    {
        this.data = data;
    }    
    // public methods =========================================================
    @Override
    public String toString()
    {
        return data.name;
    }
    
    /**
     * Toggle visibility
     */
    public void toggleVisibility()
    {
        data.visible = !data.visible;
    }
}