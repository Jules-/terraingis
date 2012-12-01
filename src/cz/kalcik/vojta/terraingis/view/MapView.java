package cz.kalcik.vojta.terraingis.view;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * main class for viewing map
 * @author jules
 *
 */
public class MapView extends SurfaceView
{
    // constants ==========================================================================
    private final String LOG_TAG = "TerrainGIS";
    
    // attributes =========================================================================
    private LayerManager layerManager = new LayerManager();
    
    // touch attributes
    enum TouchStatus {IDLE, TOUCH, PINCH};
    private PointF touchPoint = new PointF();
    private PinchDistance pinchDistance = new PinchDistance();
    private TouchStatus touchStatus = TouchStatus.IDLE;
    private float scale;
    private PointF pivot = new PointF();
    
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
        
        Handler mTileRequestCompleteHandler = new SimpleInvalidationHandler(this);
        tileProvider.setTileRequestCompleteHandler(mTileRequestCompleteHandler);
    }
    
    // attributes    
    public void setZoom(float zoom)
    {
        layerManager.setZoom(zoom);
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
        
        if(touchStatus == TouchStatus.PINCH)
        {            
            canvas.scale(scale, scale, pivot.x, pivot.y);
        }
        
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
        int action = e.getAction() & MotionEvent.ACTION_MASK;
        
        // touch down
        if(action == MotionEvent.ACTION_DOWN)
        {
            touchStatus = TouchStatus.TOUCH;
            
            touchPoint.set(e.getX(), e.getY());
        }
        // pinch down
        else if(action == MotionEvent.ACTION_POINTER_DOWN)
        {
            touchStatus = TouchStatus.PINCH;
            
            pinchDistance.setStart(e.getX(0), e.getY(0), e.getX(1), e.getY(1));
            pivot = pinchDistance.getMiddle();
        }
        // moving fingers
        else if(action == MotionEvent.ACTION_MOVE)
        {
            // moving map
            if(touchStatus == TouchStatus.TOUCH)
            {
                float x = e.getX();
                float y = e.getY();
                
                int diffX = (int)Math.round(x - touchPoint.x);
                int diffY = (int)Math.round(y - touchPoint.y);
                
                if(Math.abs(diffX) > 1 || Math.abs(diffY) > 1)
                {
                    layerManager.offsetPx(-diffX, diffY);
                    
                    changeScroll();
                    
                    touchPoint.set(x, y);
                }
            }
            //zooming map
            else if(touchStatus == TouchStatus.PINCH)
            {
                pinchDistance.set(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                scale = pinchDistance.getRateDistance();
                
                invalidate();
            }
        }
        // pinch up
        else if(action == MotionEvent.ACTION_POINTER_UP)
        {
            touchStatus = TouchStatus.IDLE;
                
            changeZoomByScale();
        }
        // touch up
        else if(action == MotionEvent.ACTION_UP)
        {
            touchStatus = TouchStatus.IDLE;
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

    /**
     * change zoom by scale
     */
    private void changeZoomByScale()
    {
        if(scale != 1)
        {
            Point center = getCenter();
            
            float pivotCenterDistanceX = (center.x - pivot.x);
            float pivotCenterDistanceY = (center.y - pivot.y);
            
            Point2D.Double pivotLatLon = layerManager.pxToLatLon(pivot, null);
            
            layerManager.setZoom(layerManager.getZoom()/scale);
            
            PointF localPivot = layerManager.latLonToPx(pivotLatLon, (PointF)null);
            localPivot.set(localPivot.x + pivotCenterDistanceX,
                           -(localPivot.y + pivotCenterDistanceY));
            layerManager.setPositionPx(localPivot);
            
            changeScroll();
        }
    }
    
    private Point getCenter()
    {
        return new Point(getScrollX() + getWidth()/2, getScrollY() + getHeight()/2);
    }
    
    // classes =============================================================================
    /**
     * class compute distance for zoom
     * @author jules
     *
     */
    private class PinchDistance
    {
        private float distance;
        private float startDistance;
        private PointF middle = new PointF();

        public PointF getMiddle()
        {
            return middle;
        }
        
        public void set(float distanceX, float distanceY)
        {            
            distance = FloatMath.sqrt(distanceX * distanceX + distanceY * distanceY);
        }
        
        public void setStart(float x0, float y0, float x1, float y1)
        {
            middle.set(getScrollX() + (x0+x1)/2, getScrollY() + (y0+y1)/2);
            set(x1 - x0, y1 - y0);
            startDistance = distance;
        }
        
        public float getRateDistance()
        {
            return distance/startDistance;
        }
    }
}