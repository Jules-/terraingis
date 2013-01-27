/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import android.content.Context;

/**
 * @author jules
 *
 */
public class ConvertUnits
{
    private static float density = 0;
    
    /**
     * @param density the density to set
     */
    public static void setDensity(float densityValue)
    {
        density = densityValue;
    }

    /**
     * convert value in dp to px
     * @param dp
     * @return
     */
    public static int dp2px(float dp)
    {
        assert density == 0 : "Density is not set!"; 
        
        return (int) (dp * density + 0.5f);
    }
    
    /**
     * convert value in px to dp
     * @param px
     * @return dp
     */
    public static float px2dp(int px)
    {
        assert density == 0 : "Density is not set!"; 
        
        return px/density;
    }    
}
