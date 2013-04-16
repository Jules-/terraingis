package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import jsqlite.Exception;
import microsoft.mappoint.TileSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.components.TileCache;
import cz.kalcik.vojta.terraingis.components.TileDownloader;
import cz.kalcik.vojta.terraingis.components.TileCache.Tile;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class TilesLayer extends AbstractLayer
{
    // constants ===============================================================================
    private static int MIN_ZOOM_LEVEL = 0;
    private static int MAX_ZOOM_LEVEL = 18;
    
    // attributes ==============================================================================
    private Envelope currentRealRect = new Envelope();
    private Rect currentRect = new Rect();
    private double scale;
    private int mWorldSize_2;
    private Point mUpperLeft = new Point();
    private Point mLowerRight = new Point();
    private TileCache mTileCache = TileCache.getInstance();
    private Rect mTileRect = new Rect();
    private TileDownloader mDownloader;
    
    // public methods ==========================================================================
    public TilesLayer()
    {
        super.data.name = "OSM Mapnik";
        mEnvelope = new Envelope(-LayerManager.SPHERICAL_MERCATOR_DIST,
                LayerManager.SPHERICAL_MERCATOR_DIST,
                -LayerManager.SPHERICAL_MERCATOR_DIST,
                LayerManager.SPHERICAL_MERCATOR_DIST);
        mSrid = SpatiaLiteIO.EPSG_SPHERICAL_MERCATOR;
    }    
    
    @Override
    public void draw(Canvas canvas, Envelope rect) throws Exception,
            ParseException
    {
        int zoomLevel = Navigator.mpxToZoomLevel(mNavigator.getZoom());
        zoomLevel = Math.max(Math.min(zoomLevel, MAX_ZOOM_LEVEL), MIN_ZOOM_LEVEL);
        double tilesZoom = Navigator.zoomLevelToMpx(zoomLevel);
        scale = mNavigator.getZoom() / tilesZoom;
        
        mWorldSize_2 = TileSystem.MapSize(zoomLevel) >> 1;
        
        mNavigator.getPxRectangle(currentRealRect);
        rectRealToTiles(currentRealRect, currentRect);
        currentRect.offset(mWorldSize_2, mWorldSize_2);
        
        TileSystem.PixelXYToTileXY(currentRect.left, currentRect.top, mUpperLeft);
        mUpperLeft.offset(-1, -1);
        TileSystem.PixelXYToTileXY(currentRect.right, currentRect.bottom, mLowerRight);
        
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
        ArrayList<Tile> tiles = mTileCache.getTiles(mUpperLeft.x, mLowerRight.x,
                mUpperLeft.y, mLowerRight.y, zoom);
        if(mTileCache.hasIncompletedRequest() &&
                (mDownloader == null || !mDownloader.isAlive()))
        {
            Log.d("TerrainGIS", "start downloader");
            mDownloader = new TileDownloader();
            mDownloader.start();
        }        
        
        int tileSize = TileSystem.getTileSize();
        for(Tile tile: tiles)
        {
            mTileRect.set(tile.x * tileSize, tile.y * tileSize,
                    tile.x * tileSize + tileSize, tile.y * tileSize + tileSize);
            mTileRect.offset(-mWorldSize_2, -mWorldSize_2);
            
            rectTilesToReal(mTileRect, currentRealRect);
            
            mDrawer.drawCanvasDraweblePx(canvas, tile.imageTile, currentRealRect);
        }
    }
}