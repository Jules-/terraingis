package cz.kalcik.vojta.terraingis.view;

import org.osmdroid.tileprovider.MapTileProviderBase;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * main class for viewing map
 * @author jules
 *
 */
public class MapView extends SurfaceView
{
    private LayerManager layerManager = new LayerManager();
    private final String LOG_TAG = "TerrainGIS";
    
    // drawing
    private PointF touchPoint = new PointF();
    
    // public methods ======================================================================
    
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        this.setWillNotDraw(false);
    }
    
    // layers
    public void appendTileLayer(MapTileProviderBase tileProvider, Context context)
    {
        layerManager.appendTileLayer(tileProvider, context);
    }
    
    // attributes
    
    public void setZoomLevel(int zoomLevel)
    {
        layerManager.setZoomLevel(zoomLevel);
        changeScroll();
    }
    
    public void setLatLonPosition(double lon, double lat)
    {
        layerManager.setLatLonPosition(lon, lat);
        changeScroll();
    }
    
    public void setProjection(Projection projection)
    {
        layerManager.setProjection(projection);
    }
    
    // on methods ==========================================================================
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        layerManager.redraw(canvas, getScreenRect(null));
    }
    
    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        
        changeScroll();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {      
        float x = e.getX();
        float y = e.getY();
        
        int action = e.getAction() & MotionEvent.ACTION_MASK;
        
        if(action == MotionEvent.ACTION_DOWN)
        {
            touchPoint.set(x, y);
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            int diffX = (int)Math.round(x - touchPoint.x);
            int diffY = (int)Math.round(y - touchPoint.y);
            
            if(Math.abs(diffX) > 1 || Math.abs(diffY) > 1)
            {
                layerManager.offsetPx(-diffX, diffY);
                
                changeScroll();
                
                touchPoint.set(x, y);
            }
        }

        return true;
    }
    // private methods =====================================================================
    
    /**
     * Gets the current bounds of the screen in <I>screen coordinates</I>.
     */
    private Rect getScreenRect(final Rect reuse)
    {
        final Rect out = reuse == null ? new Rect() : reuse;
        out.set(getScrollX(), getScrollY(), getScrollX() + getWidth(), getScrollY() + getHeight());
        return out;
    }
    
    private void changeScroll()
    {
        Point position = layerManager.getPositionPx(); 
        scrollTo(position.x - getWidth()/2, -position.y - getHeight()/2);
        invalidate();
    }
}