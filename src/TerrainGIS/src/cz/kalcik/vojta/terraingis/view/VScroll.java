/**
 * 
 */
package cz.kalcik.vojta.terraingis.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * @author jules
 *
 */
public class VScroll extends ScrollView
{
    public VScroll(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        return false;
    }
}
