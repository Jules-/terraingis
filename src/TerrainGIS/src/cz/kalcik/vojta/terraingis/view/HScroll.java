/**
 * 
 */
package cz.kalcik.vojta.terraingis.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * @author jules
 *
 */
public class HScroll extends HorizontalScrollView
{
    public HScroll(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        return false;
    }
}
