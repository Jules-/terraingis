/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.terraingis.R;
import android.content.Context;

/**
 * @author jules
 *
 */
public class ListBackgroundColors
{
    private int mColor1;
    private int mColor2;
    private boolean mFirstColor = true;
    
    public ListBackgroundColors(Context context)
    {
        mColor1 = context.getResources().getColor(R.color.background_list1);
        mColor2 = context.getResources().getColor(R.color.background_list2);        
    }
    
    /**
     * @return next color
     */
    public int getNextColor()
    {
        int result = mFirstColor ? mColor1 : mColor2;

        mFirstColor = !mFirstColor;
        return result;
    }
}
