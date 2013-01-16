package cz.kalcik.vojta.terraingis.view;

import java.io.Serializable;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
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
    private int WHITE = Color.rgb(255, 255, 255);

    // data =========================================================================
    public static class MapViewData implements Serializable
    {
        private static final long serialVersionUID = -3597152260915322234L;
        
        // position only for save and restore (value in Navigator)
        public Point2D.Double position = new Point2D.Double(0, 0);
        // zoom only for save and restore (value in Navigator)
        public double zoom;
    }
    
    // attributes =========================================================================
    private LayerManager layerManager = LayerManager.getInstance();
    private Navigator navigator = Navigator.getInstance();
    private Settings settings = Settings.getInstance();
    private Context context;
    private GestureDetector gestureDetector;
    private MainActivity mainActivity;
    
    private boolean runLocation = false;
    private Point2D.Double locationM = new Point2D.Double(0,0); // location from GPS or Wi-Fi
    private boolean freezLocation = false;
    private boolean locationValid = false;
    private MapViewData data = new MapViewData();
    private Drawable locationIcon;

    // touch attributes
    enum TouchStatus {IDLE, TOUCH, PINCH};
    private PointF touchPoint = new PointF();
    private PointF touchDiff = new PointF(0, 0);
    private PinchDistance pinchDistance = new PinchDistance();
    private TouchStatus touchStatus = TouchStatus.IDLE;
    private float scale;
    private PointF pivot = new PointF();
    
    // static attributes
    
    // public methods ======================================================================
    
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        this.context = context;
        locationIcon = context.getResources().getDrawable(settings.getLocationIcon());
        this.setWillNotDraw(false);
        
        gestureDetector =  new GestureDetector(context, new MySimpleOnGestureListener());
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
    
    // getter setter =======================================================================
    
    public MapViewData getData()
    {
        Point2D.Double position = navigator.getPositionM();
        data.position.setLocation(position.x, position.y);
        data.zoom = navigator.getZoom();
        return data;
    }
    
    public void setData(Serializable data)
    {
        this.data = (MapViewData)data;
        
        Point2D.Double position = new Point2D.Double(this.data.position.x, this.data.position.y);
        navigator.setPositionM(position);
        navigator.setZoom(this.data.zoom);
    }
    
    /**
     * set if location is valid
     * @param value
     */
    public void setLocationValid(boolean value)
    {
        locationValid = value;
    }
    
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
     * set height of ActionBar
     * @param heightActionBar
     */
    public void setMainActivity(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
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
        
        if(touchStatus == TouchStatus.TOUCH)
        {            
            canvas.translate(touchDiff.x, touchDiff.y);
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
        if(gestureDetector.onTouchEvent(e))
        {
            return true;
        }
        
        int action = e.getAction() & MotionEvent.ACTION_MASK;
        
        // touch down
        if(action == MotionEvent.ACTION_DOWN)
        {
            touchStatus = TouchStatus.TOUCH;
            
            touchPoint.set(e.getX(), e.getY());
            touchDiff.set(0, 0);
        }
        // pinch down
        else if(action == MotionEvent.ACTION_POINTER_DOWN)
        {
            changePositionByDiff();
            
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
                
                float diffX = x - touchPoint.x;
                float diffY = y - touchPoint.y;
                
                if(Math.abs(diffX - touchDiff.x) > 1 || Math.abs(diffY - touchDiff.y) > 1)
                {
                    touchDiff.set(diffX, diffY);
                    
                    invalidate();
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
            
            changePositionByDiff();
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
     * change position by diff of touch point
     */
    private void changePositionByDiff()
    {
        if(Math.abs(touchDiff.x) >= 0.5 || Math.abs(touchDiff.y) >= 0.5)
        {
            navigator.offsetSurfacePx(-touchDiff.x, -touchDiff.y);
            touchDiff.set(0, 0);
            
            invalidate();
        }
    }
    
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
        navigator.drawIconM(canvas, locationM, locationIcon);
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
    
    /**
     * detector for gestures
     * @author jules
     *
     */
    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent arg0) 
        {
            if(touchPoint.y <= mainActivity.getActionBarHeight())
            {
                mainActivity.showActionBar();
            }
            
            return true;
        }        
    }
}