package cz.kalcik.vojta.terraingis.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.mobeta.android.dslv.DragSortListView;

import cz.kalcik.vojta.terraingis.MainActivity;


public class LayersView extends DragSortListView
{
    // constants ==========================================================================
    private final float MEASURE_MAX_OFF_PATH = (float)0.25;
    private final float MEASURE_PATH_MIN_DISTANCE = (float)0.3;
    private final float SWIPE_THRESHOLD_VELOCITY = 100;
    
    // attributes =========================================================================
    private GestureDetector mGestureDetector;
    private MainActivity mMainActivity;
    
    // public methods ======================================================================
    
    /**
     * constructor
     * @param context
     * @param attrs
     */
    public LayersView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        
        mGestureDetector =  new GestureDetector(context, new MySimpleOnGestureListener());
        mMainActivity = (MainActivity)getContext();
    }
    
    // on methods ==========================================================================
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {     
        boolean value = super.onTouchEvent(e);
        
        mGestureDetector.onTouchEvent(e);
        
        return value;
    }
    
    
    // classes =============================================================================
    
    /**
     * gesture listener
     * @author jules
     *
     */
    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {     
            boolean value = super.onFling(e1, e2, velocityX, velocityY);
            
            if (Math.abs(e1.getY() - e2.getY()) > getWidth()*MEASURE_MAX_OFF_PATH)
            {
                return value;
            }
            
            // right to left swipe
            if(e1.getX() - e2.getX() > getWidth()*MEASURE_PATH_MIN_DISTANCE &&
               Math.abs(velocityX) > mMainActivity.dp2px(SWIPE_THRESHOLD_VELOCITY))
            {
                mMainActivity.hideLayersFragment();
                return true;
            }
            
            return value;
        }     
    }    
}