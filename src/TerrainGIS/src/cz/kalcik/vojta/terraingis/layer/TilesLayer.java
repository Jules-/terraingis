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
package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.components.SphericalMercatorTiles;
import cz.kalcik.vojta.terraingis.components.TileCache;
import cz.kalcik.vojta.terraingis.components.TileDownloader;
import cz.kalcik.vojta.terraingis.components.TileCache.Tile;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class TilesLayer extends AbstractLayer
{
    // constants ===============================================================================
    private static int MIN_ZOOM_LEVEL = 0;
    private static int MAX_ZOOM_LEVEL = 18;
    
    // attributes ==============================================================================
    private Envelope screenRectPx = new Envelope();
    private Rect screenRectTilesPx = new Rect();
    private double scale;
    private int mWorldSize_2;
    private Point mUpperLeft = new Point();
    private Point mLowerRight = new Point();
    private TileCache mTileCache = TileCache.getInstance();
    private Rect mTileRect = new Rect();
    private TileDownloader mDownloader;
    private MainActivity mMainActivity;
    
    // public methods ==========================================================================
    public TilesLayer(MainActivity mainActivity)
    {
        super.data.name = "OSM Mapnik";
        mEnvelope = LayerManager.MAX_ENVELOPE;
        mSrid = SpatiaLiteIO.EPSG_SPHERICAL_MERCATOR;
        mMainActivity = mainActivity;
    }    
    
    @Override
    public void draw(Canvas canvas, Envelope rect, boolean drawVertices) throws Exception,
            ParseException
    {
        int zoomLevel = Navigator.mpxToZoomLevel(mNavigator.getZoom());
        zoomLevel = Math.max(Math.min(zoomLevel, MAX_ZOOM_LEVEL), MIN_ZOOM_LEVEL);
        double tilesZoom = Navigator.zoomLevelToMpx(zoomLevel);
        scale = mNavigator.getZoom() / tilesZoom;
        
        mWorldSize_2 = SphericalMercatorTiles.getMapSize(zoomLevel) / 2; 
        
        mNavigator.getPxRectangle(screenRectPx);
        rectRealToTiles(screenRectPx, screenRectTilesPx);
        screenRectTilesPx.offset(mWorldSize_2, mWorldSize_2);
        
        SphericalMercatorTiles.getTileFromPx(screenRectTilesPx.left, screenRectTilesPx.top, mUpperLeft);
        mUpperLeft.offset(-1, -1);
        SphericalMercatorTiles.getTileFromPx(screenRectTilesPx.right, screenRectTilesPx.bottom, mLowerRight);
        
        drawTiles(canvas, zoomLevel);
    }

    @Override
    public void detach()
    {
        // TODO Auto-generated method stub
        
    }
    
    // private methods ==========================================================================
    private void rectRealToTiles(Envelope input, Rect output)
    {
        output.set(coordRealToTiles(input.getMinX()),
                   coordRealToTiles(-(input.getMaxY())),
                   coordRealToTiles(input.getMaxX()),
                   coordRealToTiles(-input.getMinY()));        
    }
    
    private void rectTilesToReal(Rect input, Envelope output)
    {
        output.init(coordTilesToReal(input.left),
                    coordTilesToReal(input.right),
                    coordTilesToReal(-input.bottom),
                    coordTilesToReal(-input.top));         
    }
    
    private int coordRealToTiles(double coord)
    {
        return (int)Math.round(coord * scale);
    }
    
    private double coordTilesToReal(double coord)
    {
        return coord / scale;
    }
    
    private void drawTiles(Canvas canvas, int zoom)
    {
        int tileSize = SphericalMercatorTiles.TILE_SIZE;
        int countTiles = (mWorldSize_2 * 2) / tileSize;
        
        int minX = Math.max(Math.min(mUpperLeft.x, countTiles-1), 0);
        int maxX = Math.max(Math.min(mLowerRight.x, countTiles-1), 0);
        int minY = Math.max(Math.min(mUpperLeft.y, countTiles-1), 0);
        int maxY = Math.max(Math.min(mLowerRight.y, countTiles-1), 0);
        
        ArrayList<Tile> tiles = mTileCache.getTiles(minX, maxX,
                minY, maxY, zoom);
        if(mMainActivity.isNetworkAvailable() && mTileCache.hasIncompletedRequest() &&
                (mDownloader == null || !mDownloader.isAlive()))
        {
            Log.d("TerrainGIS", "start downloader");
            mDownloader = new TileDownloader();
            mDownloader.start();
        }        
        
        for(Tile tile: tiles)
        {
            mTileRect.set(tile.x * tileSize, tile.y * tileSize,
                    tile.x * tileSize + tileSize, tile.y * tileSize + tileSize);
            mTileRect.offset(-mWorldSize_2, -mWorldSize_2);
            
            rectTilesToReal(mTileRect, screenRectPx);
            
            Drawer.drawCanvasDraweblePx(canvas, tile.imageTile, screenRectPx);
        }
    }
}