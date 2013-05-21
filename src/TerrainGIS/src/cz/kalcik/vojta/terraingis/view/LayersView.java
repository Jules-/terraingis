/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.ConvertUnits;


public class LayersView extends DragSortListView
{
    // constants ==========================================================================
    private final float MEASURE_MAX_OFF_PATH = (float)0.25;
    private final float MEASURE_PATH_MIN_DISTANCE = (float)0.3;
    private final float SWIPE_THRESHOLD_VELOCITY = 100;
    
    // attributes =========================================================================
    private GestureDetector mGestureDetector;
    private MainActivity mMainActivity;
    private int mMySelectedPosition = -1;
    
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
        setOnItemClickListener(itemClickHandler);
    }

    /**
     * remove selection
     */
    public void deselect()
    {
        setMySelectedPosition(-1);
    }
    // getter, setter ======================================================================
    /**
     * @return the mMySelectedPosition
     */
    public int getMySelectedPosition()
    {
        return mMySelectedPosition;
    }
    
    /**
     * set MySelectedPosition
     * @param position
     */
    public void setMySelectedPosition(int position)
    {
        mMainActivity.getLayersFragment().removeSelectedObject();
        mMainActivity.getAttributesFragment().clearSelection();
        
        mMySelectedPosition = position;
    }
    // on methods ==========================================================================
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {     
        boolean value = super.onTouchEvent(e);
        
        mGestureDetector.onTouchEvent(e);
        
        return value;
    }
    
    // handler ============================================================================
    OnItemClickListener itemClickHandler = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            setMySelectedPosition(position);
            invalidateViews();
            mMainActivity.getMapFragment().setMapTools();
        }
    };
    
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
               Math.abs(velocityX) > ConvertUnits.dp2px(SWIPE_THRESHOLD_VELOCITY))
            {
                mMainActivity.hidePanel();
                return true;
            }
            
            return value;
        }     
    }    
}