package cz.kalcik.vojta.terraingis.view;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
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
        public Coordinate position = new Coordinate(0, 0);
        // zoom only for save and restore (value in Navigator)
        public double zoom;
    }
    
    // attributes =========================================================================
    private LayerManager layerManager = LayerManager.getInstance();
    private Navigator navigator = Navigator.getInstance();
    private Settings settings = Settings.getInstance();
    private Drawer mDrawer = Drawer.getInstance();
    private GestureDetector gestureDetector;
    private MainActivity mainActivity;
    private MapFragment mMapFragment;
    
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
    /**
     * constructor
     * @param context
     * @param attrs
     */
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        locationIcon = context.getResources().getDrawable(settings.getLocationIcon());
        this.setWillNotDraw(false);
        
        gestureDetector =  new GestureDetector(context, new MySimpleOnGestureListener());
    }

    /**
     * zoom to envelope
     * @param zoomingEnvelope
     */
    public void zoomToEnvelopeM(Envelope zoomingEnvelope)
    {
        navigator.zoomToEnvelopeM(zoomingEnvelope);
        invalidate();
    }
    
    // getter setter =======================================================================
    
    public MapViewData getData()
    {
        Coordinate position = navigator.getPositionM();
        data.position.x = position.x;
        data.position.y = position.y;
        data.zoom = navigator.getZoom();
        return data;
    }
    
    public void setData(Serializable data)
    {
        this.data = (MapViewData)data;
        
        Coordinate position = new Coordinate(this.data.position.x, this.data.position.y);
        navigator.setPositionM(position);
        navigator.setZoom(this.data.zoom);
    }
    
    /**
     * set zoom of canvas
     * @param zoom
     */
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
     * set height of ActionBar
     * @param heightActionBar
     */
    public void setMainActivity(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        mMapFragment = this.mainActivity.getMapFragment();
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
        
        mDrawer.draw(canvas, getWidth(), getHeight());
        
        drawLocation(canvas);
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
    
    private synchronized void drawLocation(Canvas canvas)
    {
        Coordinate location = mMapFragment.getLocation();
        if(location != null)
        {
            mDrawer.drawIconM(canvas, location, locationIcon);
        }
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
            else if(touchPoint.x <= mainActivity.getActionBarHeight() && mainActivity.isHiddenLayersFragment())
            {
                mainActivity.showLayersFragment();
            }
            
            return true;
        }        
    }
}