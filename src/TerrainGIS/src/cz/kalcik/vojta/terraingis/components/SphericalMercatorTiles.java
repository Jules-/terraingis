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
