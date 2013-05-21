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

import android.graphics.Point;

public class SphericalMercatorTiles
{
    // constants ==================================================================
    public static final int TILE_SIZE = 256;
    
    /**
     * return size of map by level of zoom
     * @param zoomLevel
     * @return
     */
    public static int getMapSize(int zoomLevel)
    {
        return TILE_SIZE << zoomLevel;
    }
    
    /**
     * return tile by point in pixels
     * @param x
     * @param y
     * @param output
     * @return
     */
    public static Point getTileFromPx(int x, int y, Point output)
    {
        if(output == null)
        {
            output = new Point();
        }

        output.x = x / TILE_SIZE;
        output.y = y / TILE_SIZE;
        return output;
    }
}
