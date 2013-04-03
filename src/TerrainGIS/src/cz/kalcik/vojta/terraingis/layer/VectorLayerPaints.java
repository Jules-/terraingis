/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

/**
 * @author jules
 *
 */
public class VectorLayerPaints
{
    // constants ===================================================================
    enum PaintType {DEFAULT, SELECTED, SELECTED_SELECTED_NODE, NOT_SAVED};
    
    private static final float POINT_RADIUS_DP = 6;
    private static final float POINT_RADIUS_SELECTED_DP = 8;
    private static final float LINE_WIDTH = 2;
    private static final float LINE_WIDTH_SELECTED = 3;
    private static final float HUE_STEP = 80;
    private static final float VALUE_STEP = 0.2f;
    private static final float MIN_VALUE = 0.3f;
    private static final float[] DEFAULT_HSV = {0, 1, 1};
    private static final int SELECTED_COLOR = Color.YELLOW;
    private static final int SELECTED_DEFAULT_NODE_COLOR = Color.BLACK;
    private static final float[] DASHED_PARAMS = {10, 5};
    private final static int TRANCPARENCE_NOTSAVED = 64;
    // static atributes ============================================================
    private static float[] mCurrentHSV = DEFAULT_HSV.clone();
    
    // public static methods =======================================================
    
    /**
     * paint for point
     * @param paintType
     * @return
     */
    public static Paint getPoint(PaintType paintType)
    {
        Paint result = commonAttrs(paintType);
        
        return result;
    }
    
    /**
     * paint for line
     * @param paintType
     * @return
     */
    public static Paint getLine(PaintType paintType)
    {
        Paint result = commonAttrs(paintType);
        result.setStyle(Paint.Style.STROKE);
        // stroke width
        if(paintType == PaintType.SELECTED)
        {
            result.setStrokeWidth(LINE_WIDTH_SELECTED);
        }
        else
        {
            result.setStrokeWidth(LINE_WIDTH);
        }
        // dashed
        if(paintType == PaintType.NOT_SAVED)
        {
            result.setPathEffect(new DashPathEffect(DASHED_PARAMS, 0));
        }        
        
        return result;
    }
    
    /**
     * paint for polygon
     * @param paintType
     * @return
     */
    public static Paint getPolygon(PaintType paintType)
    {
        Paint result = commonAttrs(paintType);
        result.setStyle(Paint.Style.FILL);
        
        // transparency
        if(paintType == PaintType.NOT_SAVED)
        {
            result.setAlpha(TRANCPARENCE_NOTSAVED);
        }
        
        return result;
    }
    
    /**
     * set default colors
     */
    public static void resetColors()
    {
        mCurrentHSV = DEFAULT_HSV.clone();
    }
    
    /**
     * @param paintType
     * @return radius for point circle
     */
    public static float getPointRadius(PaintType paintType)
    {
        if(paintType == PaintType.SELECTED)
        {
            return ConvertUnits.dp2px(POINT_RADIUS_SELECTED_DP);
        }
        else
        {
            return ConvertUnits.dp2px(POINT_RADIUS_DP);
        }
    }
    // private static methods ======================================================
    /**
     * common settings of paint
     * @param paint
     * @return
     */
    private static Paint commonAttrs(PaintType paintType)
    {
        Paint result = new Paint();
        result.setAntiAlias(true);
        if(paintType == PaintType.SELECTED)
        {
            result.setColor(SELECTED_COLOR);
        }
        else if(paintType == PaintType.SELECTED_SELECTED_NODE)
        {
            result.setColor(SELECTED_DEFAULT_NODE_COLOR);
        }
        else
        {
            result.setColor(getColor());
        }
        
        return result;
    }
    
    /**
     * change color
     * @return
     */
    private static int getColor()
    {
        int result = Color.HSVToColor(mCurrentHSV);
        
        mCurrentHSV[0] += HUE_STEP;
        if(mCurrentHSV[0] > 359f)
        {
            mCurrentHSV[0] -= 360;
            mCurrentHSV[2] -= VALUE_STEP;
            if(mCurrentHSV[2] < MIN_VALUE)
            {
                mCurrentHSV[2] = 1;
            }
        }
        
        return result;
    }
}
