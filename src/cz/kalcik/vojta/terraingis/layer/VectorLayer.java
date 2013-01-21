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
public abstract class VectorLayer extends AbstractLayer
{
    // enum ===================================================================
    public enum VectorLayerType {POINT, LINE, POLYGON};
    
    // attributes ==============================================================
    Rectangle2D.Double currentRect = new Rectangle2D.Double();
    Paint paint;
    VectorLayerType type;
    int srid;
    
    // constructors ============================================================
    
    /**
     * constructor
     * @param type
     * @param paint
     */
    public VectorLayer(VectorLayerType type, Paint paint, String name, int srid)
    {
        this.type = type;
        if(paint == null)
        {
            paint = new Paint();
        }
        
        this.paint = paint;        
        this.name = name;
        this.srid = srid;
    }
    
    // public methods =========================================================
    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Rectangle2D.Double rect)
    {

    }
    
    @Override
    public void detach()
    {
        // TODO Auto-generated method stub
        
    }
    
    // private methods ========================================================

}