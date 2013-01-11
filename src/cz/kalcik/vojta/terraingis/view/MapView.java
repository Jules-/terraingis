package cz.kalcik.vojta.terraingis.view;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
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
    private int WHITE = Color.rgb(255, 255, 255);
    
    // attributes =========================================================================
    private LayerManager layerManager = LayerManager.getInstance();
    private Navigator navigator = Navigator.getInstance();
    private Settings settings;
    
    private boolean runLocation = false;
    private Point2D.Double locationM = new Point2D.Double(0,0); // location from GPS or Wi-Fi
    private boolean freezLocation = false;
    private boolean locationValid = false;
    
    // touch attributes
    enum TouchStatus {IDLE, TOUCH, PINCH};
    private PointF touchPoint = new PointF();
    private PinchDistance pinchDistance = new PinchDistance();
    private TouchStatus touchStatus = TouchStatus.IDLE;
    private float scale;
    private PointF pivot = new PointF();
    
    // static attributes
    
    // public methods ======================================================================
    
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        this.setWillNotDraw(false);
        settings = new Settings(context);
    }
    
    // layers
    /**
     * add tiles layer to layerManager
     * @param tileProvider
     * @param context
     */
    public void addTilesLayer(MapTileProviderBase tileProvider, Context context)
    {
        layerManager.addTilesLayer(tileProvider, context);
        
        Handler mTileRequestCompleteHandler = new SimpleInvalidationHandler(this);
        tileProvider.setTileRequestCompleteHandler(mTileRequestCompleteHandler);
    }
    
    /**
     * add layer to layer manager
     * @param layer
     */
    public void addLayer(AbstractLayer layer)
    {
        layerManager.addLayer(layer);
    }
    
    // attributes    
    public void setZoom(double zoom)
    {
        navigator.setZoom(zoom);
        invalidate();
    }
    
    /**
     * set center of view
     * @param lon
     * @param lat
     */
    public synchronized void setLonLatPosition(double lon, double lat)
    {
        navigator.setLonLatPosition(lon, lat);
        invalidate();
    }
    
    /**
     * set location
     * @param location
     */
    public synchronized void setLonLatLocation(Point2D.Double location)
    {
        if(freezLocation)
        {
            navigator.setLonLatPosition(location);
        }
        layerManager.lonLatToM(location, locationM);
        locationValid = true;
        invalidate();
    }

    /**
     * set projection of layers
     * @param projection
     */
    public void setProjection(Projection projection)
    {
        layerManager.setProjection(projection);
    }
    
    /**
     * start location service
     */
    public void startLocation()
    {
        runLocation = true;
    }

    /**
     * stop location service
     */
    public void stopLocation()
    {
        runLocation = false;
        setLocationValid(false);
        invalidate();
    }
    
    /**
     * set if location is valid
     * @param value
     */
    public void setLocationValid(boolean value)
    {
        locationValid = value;
    }
    
    /**
     * change position by location 
     * @return true if success
     */
    public boolean showLocation()
    {
        if(locationValid)
        {
            navigator.setPositionM(locationM.x, locationM.y);
            
            invalidate();
        }

        return locationValid;
    }
    // on methods ==========================================================================
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        canvas.drawColor(WHITE);
        
        if(touchStatus == TouchStatus.PINCH)
        {            
            canvas.scale(scale, scale, pivot.x, pivot.y);
        }
        
        navigator.draw(canvas, getWidth(), getHeight());
        
        if(locationValid)
        {
            drawLocation(canvas);
        }
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
                    navigator.offsetSurfacePx(-diffX, -diffY);
                    
                    invalidate();
                    
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
    
    @Override
    public void onDetachedFromWindow()
    {
        layerManager.detach();
    }
    
    // private methods =====================================================================

    /**
     * change zoom by scale
     */
    private void changeZoomByScale()
    {
        if(scale != 1)
        {
            navigator.zoomByScale(scale, pivot);
            
            invalidate();
        }
    }
    
    private void drawLocation(Canvas canvas)
    {
        navigator.drawIconM(canvas, locationM, settings.getLocationIcon());
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