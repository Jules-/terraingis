/**
 * 
 */
package cz.kalcik.vojta.terraingis.view;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.AttributesFragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * @author jules
 *
 */
public class VScroll extends ScrollView
{
    // attributes ===========================================================
    private float mx, my;
    private HorizontalScrollView mHScroll;
    private GestureDetector mGestureDetector;
    private AttributesFragment mTableFragment;
    

    // public methods =======================================================
    public VScroll(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void initView(AttributesFragment tableFragment)
    {
        mHScroll = (HorizontalScrollView)findViewById(R.id.hScroll);
        mGestureDetector = new GestureDetector(getContext(), new MySimpleOnGestureListener());
        mTableFragment = tableFragment;
    }
    
    // on methods ===========================================================
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(mGestureDetector.onTouchEvent(event))
        {
            return true;
        }
        
        float curX, curY;

        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN)
        {
            mx = event.getX();
            my = event.getY();
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            curX = event.getX();
            curY = event.getY();
            scrollBy((int) (mx - curX), (int) (my - curY));
            mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            mx = curX;
            my = curY;
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            curX = event.getX();
            curY = event.getY();
            scrollBy((int) (mx - curX), (int) (my - curY));
            mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
        }

        return true;
    }
    
    // classes =========================================================================================
    /**
     * detector for gestures
     * @author jules
     *
     */
    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent event) 
        {
            mTableFragment.selectedRowIsTouched();
            mTableFragment.setBackgroundColors();
            
            return true;
        }        
    }
}
