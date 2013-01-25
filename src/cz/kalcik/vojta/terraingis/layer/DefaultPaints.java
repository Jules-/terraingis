/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Paint;

/**
 * @author jules
 *
 */
public class DefaultPaints
{
    // constants ===================================================================
    private static final float LINE_WIDTH = 2;
    
    // public static methods =======================================================
    public static Paint getLines()
    {
        Paint result = new Paint();
        result.setAntiAlias(true);
        result.setStrokeWidth(LINE_WIDTH);
        
        return result;
    }
}
