package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.geom.Rectangle2D;
import android.graphics.Canvas;

/**
 * parent of all layers
 * @author jules
 *
 */
public abstract class AbstractLayer
{
    protected String name;
    
    public abstract void draw(final Canvas canvas, Rectangle2D.Double rect);
    public abstract void detach();
    
    @Override
    public String toString()
    {
        return name;
    }
}