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
    
    /**
     * reset background color
     */
    public void reset()
    {
        mFirstColor = true;
    }
}
