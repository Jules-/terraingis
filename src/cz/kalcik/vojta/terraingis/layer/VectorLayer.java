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
public class VectorLayer extends AbstractLayer
{
    // enum ===================================================================
    public enum VectorLayerType {POINT, LINE, POLYGON};
    
    // attributes ==============================================================
    private ArrayList<VectorObject> objects = new ArrayList<VectorObject>();
    Rectangle2D.Double currentRect = new Rectangle2D.Double();
    Paint paint;
    AttributesTable attributes;
    VectorLayerType type;
    
    // constructors ============================================================

    /**
     * constructor
     * @param type
     */
    public VectorLayer(VectorLayerType type)
    {
        this(type, null);
    }
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, Paint paint)
    {
        this.type = type;
        if(paint == null)
        {
            paint = new Paint();
        }
        
        this.paint = paint;
        
        setStyleByType();
    }
    
    // public methods =========================================================
    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Rectangle2D.Double rect)
    {
        for(VectorObject object: objects)
        {
            object.draw(canvas, rect, paint);
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
    
    // private methods ========================================================
    private void setStyleByType()
    {
        if(type == VectorLayerType.POLYGON)
        {
            paint.setStyle(Paint.Style.FILL);
        }
        else if(type == VectorLayerType.LINE)
        {
            paint.setStyle(Paint.Style.STROKE);
        }
    }
}