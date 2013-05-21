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

import cz.kalcik.vojta.terraingis.exception.TerrainGISException;

/**
 * @author jules
 *
 */
public class ConvertUnits
{
    private static float density = -1;
    
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
        if(density <= 0)
        {
            throw new TerrainGISException("Density is not set!"); 
        }
        
        return (int) (dp * density + 0.5f);
    }
    
    /**
     * convert value in px to dp
     * @param px
     * @return dp
     */
    public static float px2dp(int px)
    {
        if(density <= 0)
        {
            throw new TerrainGISException("Density is not set!"); 
        }
        
        return px/density;
    }    
}
