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
