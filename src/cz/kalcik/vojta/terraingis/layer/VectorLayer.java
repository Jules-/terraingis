package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import cz.kalcik.vojta.geom.Rectangle2D;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * class for vector layer
 * @author jules
 *
 */
public class VectorLayer implements ILayer
{
    // enum ===================================================================
    public enum LayerType {POINT, LINE, POLYGON};
    
    // attributes ==============================================================
    private ArrayList<VectorObject> objects = new ArrayList<VectorObject>();
    Rectangle2D.Double currentRect = new Rectangle2D.Double();
    Paint paint;
    AttributesTable attributes;
    LayerType type;
    
    // constructors ============================================================

    /**
     * constructor
     * @param type
     */
    public VectorLayer(LayerType type)
    {
        this(type, null);
    }
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(LayerType type, Paint paint)
    {
        this.type = type;
        if(paint == null)
        {
            paint = new Paint();
        }
        
        this.paint = paint;
    }
    
    // public methods =========================================================
    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Rect screenRect)
    {
        LayerManager layerManager = LayerManager.getInstance();
        layerManager.pxToM(screenRect, currentRect);
        currentRect.y = -currentRect.y-currentRect.height; 
        
        for(VectorObject object: objects)
        {
            object.draw(canvas, currentRect, paint);
        }
    }
    
    /**
     * add object
     * @param object
     */
    public void addObject(VectorObject object)
    {
        objects.add(object);
    }
}