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
    protected String name;
    
    public abstract void draw(final Canvas canvas, Envelope rect);
    public abstract void detach();
    
    @Override
    public String toString()
    {
        return name;
    }
}