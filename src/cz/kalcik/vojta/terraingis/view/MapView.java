package cz.kalcik.vojta.terraingis.view;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class MapView extends SurfaceView
{
    private LayerManager layerManager = new LayerManager(); 
    
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        this.setWillNotDraw(false);
        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic(context, tileSource);
        
        layerManager.appendTileLayer(tileProvider, context);
    }
    
    // on methods ==========================================================================
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        layerManager.redraw(canvas, getScreenRect(null));
    }
    
    // private methods =====================================================================
    
    /**
     * Gets the current bounds of the screen in <I>screen coordinates</I>.
     */
    private Rect getScreenRect(final Rect reuse)
    {
        final Rect out = reuse == null ? new Rect() : reuse;
        out.set(getScrollX(), getScrollY(), getWidth(), getHeight());
        return out;
    }
}