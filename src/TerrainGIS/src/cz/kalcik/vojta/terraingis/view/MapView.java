package cz.kalcik.vojta.terraingis.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.MainActivity.ActivityMode;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayerType;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * main class for viewing map
 * @author jules
 *
 */
public class MapView extends SurfaceView
{
    // constants ==========================================================================
    private int INVALIDATE_TIME = 500;

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
    private Navigator mNavigator = Navigator.getInstance();
    private Settings settings = Settings.getInstance();
    private GestureDetector gestureDetector;
    private MainActivity mMainActivity;
    private MapFragment mMapFragment;
    
    private MapViewData data = new MapViewData();
    private Drawable locationIcon;
    private Drawable locationAddPointIcon;
    private Timer mInvalidateTimer;

    // touch attributes
    enum TouchStatus {IDLE, TOUCH, PINCH};
    private PointF mTouchPoint = new PointF();
    private PointF mTouchDiff = new PointF(0, 0);
    private PinchDistance mPinchDistance = new PinchDistance();
    private TouchStatus mTouchStatus = TouchStatus.IDLE;
    private float mScale;
    private PointF mPivot = new PointF();
    private boolean mMovingSelectedPoint = false;
    
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
        locationAddPointIcon = context.getResources().getDrawable(settings.getLocationAddPointIcon());
        this.setWillNotDraw(false);
        
        gestureDetector =  new GestureDetector(context, new MySimpleOnGestureListener());
    }

    /**
     * zoom to envelope
     * @param zoomingEnvelope
     */
    public void zoomToEnvelopeM(Envelope zoomingEnvelope)
    {
        mNavigator.zoomToEnvelopeM(zoomingEnvelope);
        invalidate();
    }
    
    // getter setter =======================================================================
    
    public MapViewData getData()
    {
        Coordinate position = mNavigator.getPositionM();
        data.position.x = position.x;
        data.position.y = position.y;
        data.zoom = mNavigator.getZoom();
        return data;
    }
    
    public void setData(Serializable data)
    {
        this.data = (MapViewData)data;
        
        Coordinate position = new Coordinate(this.data.position.x, this.data.position.y);
        mNavigator.setPositionM(position);
        mNavigator.setZoom(this.data.zoom);
    }
    
    /**
     * set zoom of canvas
     * @param zoom
     */
    public void setZoom(double zoom)
    {
        mNavigator.setZoom(zoom);
        invalidate();
    }
    
    /**
     * set center of view
     * @param lon
     * @param lat
     */
    public synchronized void setLonLatPosition(double lon, double lat)
    {
        try
        {
            mNavigator.setLonLatPosition(lon, lat);
            invalidate();
        }
        catch (Exception e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
        catch (ParseException e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
    }
        
    /**
     * set height of ActionBar
     * @param heightActionBar
     */
    public void setMainActivity(MainActivity mainActivity)
    {
        this.mMainActivity = mainActivity;
        mMapFragment = this.mMainActivity.getMapFragment();
    }
    
    /**
     * rin invalidate timer
     */
    public void runInvalidateTimer()
    {
        if(mInvalidateTimer != null)
        {
            return;
        }
 
        mInvalidateTimer = new Timer();
        mInvalidateTimer.schedule(new InvalidateTask(), INVALIDATE_TIME);
    }
    // on methods ==========================================================================
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        canvas.drawColor(Color.WHITE);
        
        if(mTouchStatus == TouchStatus.PINCH)
        {            
            canvas.scale(mScale, mScale, mPivot.x, mPivot.y);
        }
        
        if(mTouchStatus == TouchStatus.TOUCH)
        {            
            canvas.translate(mTouchDiff.x, mTouchDiff.y);
        }
        
        Drawer.draw(canvas, getWidth(), getHeight(), mMainActivity);
        
        drawLocations(canvas);
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
            mTouchStatus = TouchStatus.TOUCH;
            
            mTouchPoint.set(e.getX(), e.getY());
            mTouchDiff.set(0, 0);
            checkTouchPointMoving();
        }
        // pinch down
        else if(action == MotionEvent.ACTION_POINTER_DOWN)
        {
            changePositionByDiff();
            
            mTouchStatus = TouchStatus.PINCH;
            
            mPinchDistance.setStart(e.getX(0), e.getY(0), e.getX(1), e.getY(1));
            mPivot = mPinchDistance.getMiddle();
        }
        // moving fingers
        else if(action == MotionEvent.ACTION_MOVE)
        {
            // moving
            if(mTouchStatus == TouchStatus.TOUCH)
            {
                float x = e.getX();
                float y = e.getY();
                
                float diffX = x - mTouchPoint.x;
                float diffY = y - mTouchPoint.y;
                
                // moving selected point
                if(mMovingSelectedPoint)
                {
                    movePoint(x, y);
                }
                // moving map
                else
                {
                    moveMap(diffX, diffY);
                }
            }
            //zooming map
            else if(mTouchStatus == TouchStatus.PINCH)
            {
                mPinchDistance.set(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                mScale = mPinchDistance.getRateDistance();
                
                invalidate();
            }
        }
        // pinch up
        else if(action == MotionEvent.ACTION_POINTER_UP)
        {
            mTouchStatus = TouchStatus.IDLE;
                
            changeZoomByScale();
        }
        // touch up
        else if(action == MotionEvent.ACTION_UP)
        {
            mTouchStatus = TouchStatus.IDLE;
            
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
        if(Math.abs(mTouchDiff.x) >= 0.5 || Math.abs(mTouchDiff.y) >= 0.5)
        {
            mNavigator.offsetSurfacePx(-mTouchDiff.x, -mTouchDiff.y);
            mTouchDiff.set(0, 0);
            
            invalidate();
        }
    }
    
    /**
     * change zoom by scale
     */
    private void changeZoomByScale()
    {
        if(mScale != 1)
        {
            mNavigator.zoomByScale(mScale, mPivot);
            
            invalidate();
        }
    }
    
    /**
     * draw crosses at positions
     * @param canvas
     */
    private synchronized void drawLocations(Canvas canvas)
    {
        Coordinate location = mMapFragment.getCoordinatesLocation();
        if(location != null)
        {
            Drawer.drawIconM(canvas, location, locationIcon);
        }
        
        location = mMapFragment.getCoordinatesAddPoint();
        if(location != null)
        {
            Drawer.drawIconM(canvas, location, locationAddPointIcon);
        }
    }
    
    
    /**
     * @param diffX
     * @param diffY
     */
    private void moveMap(float diffX, float diffY)
    {
        if(Math.abs(diffX - mTouchDiff.x) > 1 || Math.abs(diffY - mTouchDiff.y) > 1)
        {
            mTouchDiff.set(diffX, diffY);
            
            invalidate();
        }
    }
    
    /**
     * move selected point to postion
     * @param x
     * @param y
     */
    private void movePoint(float x, float y)
    {
        VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
        if(selectedLayer != null)
        {
            Coordinate point = mNavigator.surfacePxToM(new PointF(x, y), null);
            selectedLayer.setPositionSelectedPoint(point);
            
            invalidate();
        }
    }
    
    /**
     * check if is moving of point
     * @param x
     * @param y
     */
    private void checkTouchPointMoving()
    {
        mMovingSelectedPoint = false;
        
        if(mMainActivity.getActivityMode() == ActivityMode.EDIT &&
                !mMainActivity.isAddPointMode())
        {
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                Coordinate point = mNavigator.surfacePxToM(mTouchPoint, null);
                
                if(selectedLayer.isNearSelectedPoint(point))
                {
                    mMovingSelectedPoint = true;
                }
            }
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
            int size = ConvertUnits.dp2px(Settings.DP_SIZE_SIDE_CLICK);
            ActivityMode mode = mMainActivity.getActivityMode();
            
            if(mTouchPoint.y <= size)
            {
                mMainActivity.showActionBar();
            }
            else if(mTouchPoint.x <= size && mMainActivity.isHiddenLayersFragment())
            {
                mMainActivity.showPanel();
            }
            // cursor for add point
            else if(mMainActivity.isAddPointMode())
            {
                mMapFragment.setCoordinatesAddPointM(mNavigator.surfacePxToM(mTouchPoint, null));
            }
            // cursor for add topology
            else if(mMainActivity.isTopologymode())
            {
                ArrayList<AbstractLayer> layers = LayerManager.getInstance().getLayers();
                
                Coordinate result = null;
                
                for(AbstractLayer layerFromAll : layers)
                {
                    if (layerFromAll instanceof VectorLayer)
                    {
                        try
                        {
                            result = ((VectorLayer)layerFromAll).
                                    getClickPoint(mNavigator.getMRectangle(null),
                                            mNavigator.surfacePxToM(mTouchPoint, null));
                            
                            if(result != null)
                            {
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                        catch (ParseException e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                
                mMapFragment.setCoordinatesAddPointM(result);
                invalidate();
            }
            else if(mMainActivity.canSelectObject())
            {
                LayersFragment layersFragment = mMainActivity.getLayersFragment();
                VectorLayer layer = layersFragment.getSelectedLayerIfVector();
                if(layer != null)
                {
                    Coordinate clickedPoint = mNavigator.surfacePxToM(mTouchPoint, null);
                    boolean selectVertex = (mode == ActivityMode.EDIT);
                    try
                    {
                        layer.clickSelectionObject(mNavigator.getMRectangle(null), clickedPoint, selectVertex);
                        mMainActivity.getAttributesFragment().selectItemWithRowid(layer.getSelectedRowid());
                        
                        mMapFragment.setMapTools();
                        invalidate();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(mMainActivity, R.string.database_error,
                                Toast.LENGTH_LONG).show();
                    }
                    catch (ParseException e)
                    {
                        Toast.makeText(mMainActivity, R.string.database_error,
                                Toast.LENGTH_LONG).show();
                    }
                    
                    invalidate();
                }
            }
            else if(mode == ActivityMode.RECORD)
            {
                Coordinate clickedPoint = mNavigator.surfacePxToM(mTouchPoint, null);

                VectorLayer layer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();

                if(layer != null)
                {

                    // open old object
                    if(!layer.hasOpenedRecordObject() && layer.getType() != VectorLayerType.POINT)
                    {
                        try
                        {
                            layer.clickRecordingObject(mNavigator.getMRectangle(null), clickedPoint);
                            mMapFragment.setMapTools();
                            invalidate();
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                        catch (ParseException e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        try
                        {
                            layer.checkClickRecordedVertex(mNavigator.getMRectangle(null), clickedPoint);
                            mMapFragment.setMapTools();
                            invalidate();
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                        catch (ParseException e)
                        {
                            Toast.makeText(mMainActivity, R.string.database_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            
            return true;
        }        
    }
    
    /**
     * class for invalidate map from timer
     * @author jules
     *
     */
    class InvalidateTask extends TimerTask
    {
        private InvalidateRunnable invalidateRunnable = new InvalidateRunnable();
 
        public void run()
        {
            mInvalidateTimer.cancel();
            mInvalidateTimer.purge();
            mInvalidateTimer = null;
            mMainActivity.runOnUiThread(invalidateRunnable);
        }
     }
 
    /**
     * class for invalidate map from timer
     * @author jules
     *
     */
    class InvalidateRunnable implements Runnable
    {
        public void run()
        {
            invalidate();
        }
    };
}