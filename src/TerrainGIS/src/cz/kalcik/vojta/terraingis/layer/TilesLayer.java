package cz.kalcik.vojta.terraingis.layer;

import microsoft.mappoint.TileSystem;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.TileLooper;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.components.Navigator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class TilesLayer extends AbstractLayer
{
    /** Current tile source */
    protected final MapTileProviderBase mTileProvider;
    
    /** A drawable loading tile **/
    private BitmapDrawable mLoadingTile = null;
    private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
    private int mLoadingLineColor = Color.rgb(200, 192, 192);

    /* to avoid allocations during draw */
    private final Rect mTileRect = new Rect();
    
    private int mWorldSize_2;
    
    private int mOvershootTileCache = 0;
    
    protected final ResourceProxy mResourceProxy;
    protected final float mScale;
    
    private final Context aContext;
    private double scale;
    private Rect currentRect = new Rect();
    private Envelope currentRealRect = new Envelope();
    private Navigator navigator = Navigator.getInstance();
    
    /**
     * constructor
     * @param aTileProvider
     * @param aContext
     */
    public TilesLayer(final MapTileProviderBase aTileProvider, final Context aContext)
    {       
        this.aContext = aContext;
        
        mResourceProxy = new DefaultResourceProxyImpl(aContext);
        mScale = mResourceProxy.getDisplayMetricsDensity();
        
        if (aTileProvider == null)
        {
            throw new IllegalArgumentException("You must pass a valid tile provider to the tiles overlay.");
        }
        
        this.mTileProvider = aTileProvider;
        
        mName = aTileProvider.getTileSource().name();
        mEnvelope = new Envelope(-LayerManager.SPHERICAL_MERCATOR_DIST,
                                LayerManager.SPHERICAL_MERCATOR_DIST,
                                -LayerManager.SPHERICAL_MERCATOR_DIST,
                                LayerManager.SPHERICAL_MERCATOR_DIST);
        mSrid = LayerManager.EPSG_SPHERICAL_MERCATOR;
    }
       

    @Override
    public void draw(Canvas canvas, Envelope rect)
    {
        int zoomLevel = Navigator.mpxToZoomLevel(navigator.getZoom());
        zoomLevel = Math.max(Math.min(zoomLevel, mTileProvider.getMaximumZoomLevel()), mTileProvider.getMinimumZoomLevel());
        double tilesZoom = Navigator.zoomLevelToMpx(zoomLevel);
        
        scale = navigator.getZoom() / tilesZoom;
               
        mWorldSize_2 = TileSystem.MapSize(zoomLevel) >> 1;
        
        navigator.getPxRectangle(currentRealRect);
        
        rectRealToTiles(currentRealRect, currentRect);

        currentRect.offset(mWorldSize_2, mWorldSize_2);
        
        drawTiles(canvas, zoomLevel, TileSystem.getTileSize(), currentRect);      
    }
    
    @Override
    public void detach()
    {
        mTileProvider.detach();
    }
    
    // protected methods ============================================================================
    
    protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile,
                                     final Rect tileRect)
    {
        tileRect.offset(-mWorldSize_2, -mWorldSize_2);

        rectTilesToReal(tileRect, currentRealRect);
        
        mDrawer.drawCanvasDraweblePx(c, currentMapTile, currentRealRect);
    }
    
    // private methods ==============================================================================
    
    private void drawTiles(final Canvas c, final int zoomLevel, final int tileSizePx,
                           final Rect viewPort)
    {
        mTileLooper.loop(c, zoomLevel, tileSizePx, viewPort);
    }
    
    private final TileLooper mTileLooper = new TileLooper()
    {
        @Override
        public void initialiseLoop(final int pZoomLevel, final int pTileSizePx)
        {
            // make sure the cache is big enough for all the tiles
            final int numNeeded = (mLowerRight.y - mUpperLeft.y + 1) * (mLowerRight.x - mUpperLeft.x + 1);
            mTileProvider.ensureCapacity(numNeeded + mOvershootTileCache);
        }
        
        @Override
        public void handleTile(final Canvas pCanvas, final int pTileSizePx, final MapTile pTile, final int pX, final int pY)
        {
            Drawable currentMapTile = mTileProvider.getMapTile(pTile);
            if (currentMapTile == null)
            {
                currentMapTile = getLoadingTile();
            }

            if (currentMapTile != null)
            {
                mTileRect.set(pX * pTileSizePx, pY * pTileSizePx, pX * pTileSizePx + pTileSizePx, pY
                        * pTileSizePx + pTileSizePx);
                onTileReadyToDraw(pCanvas, currentMapTile, mTileRect);
            }
        }
        @Override
        public void finaliseLoop() {
        }
    };
    
    private Drawable getLoadingTile()
    {
        if (mLoadingTile == null && mLoadingBackgroundColor != Color.TRANSPARENT)
        {
            try
            {
                final int tileSize = mTileProvider.getTileSource() != null ? mTileProvider
                        .getTileSource().getTileSizePixels() : 256;
                final Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize,
                        Bitmap.Config.RGB_565);
                final Canvas canvas = new Canvas(bitmap);
                final Paint paint = new Paint();
                canvas.drawColor(mLoadingBackgroundColor);
                paint.setColor(mLoadingLineColor);
                paint.setStrokeWidth(0);
                final int lineSize = tileSize / 16;
                for (int a = 0; a < tileSize; a += lineSize)
                {
                    canvas.drawLine(0, a, tileSize, a, paint);
                    canvas.drawLine(a, 0, a, tileSize, paint);
                }

                mLoadingTile = new BitmapDrawable(aContext.getResources(), bitmap);
            }
            catch (final OutOfMemoryError e)
            {
                Log.e("TerrainGIS", "OutOfMemoryError getting loading tile");
                System.gc();
            }
        }
        return mLoadingTile;
    }
    
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
}