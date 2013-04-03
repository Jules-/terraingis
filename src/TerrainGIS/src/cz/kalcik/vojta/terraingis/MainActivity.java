package cz.kalcik.vojta.terraingis;

import java.io.File;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.location.LocationWorker;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.R;

public class MainActivity extends AbstractActivity
{
    // constants ==========================================================
    private static final String LOCATION_WORKER_DATA = "LocationWorkerData";
    private static final String MAIN_ACTIVITY_DATA = "MainActivityData";
    private static final String SHOWN_LAYERS = "ShownLayers";
    private static final float MIN_WIDTH_PANEL_DP = 300;
    
    public enum ActivityMode {EXPLORE, RECORD, EDIT};
    // properties =========================================================
    public static class MainActivityData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public ActivityMode activityMode;
        public boolean addPointMode;

        public MainActivityData(ActivityMode activityMode, boolean addPointMode)
        {
            this.activityMode = activityMode;
            this.addPointMode = addPointMode;
        }
    }
    
    private MenuItem mMenuRunLocation;
    private MenuItem mMenuShowLocation;
    private MenuItem mMenuRecord;
    private MenuItem mMenuEdit;
    private MenuItem mMenuAddPoint;
    private Timer timer;
    private LocationWorker mLocationWorker;
    private Settings mSettings = Settings.getInstance();
    private MapFragment mMapFragment;
    private LayersFragment mLayersFragment;
    private LinearLayout mMapLayout;
    private LinearLayout mLayersLayout;
    private MainActivityData data = new MainActivityData(ActivityMode.EXPLORE, false);
    
    // public methods =====================================================
    
    /**
     * show ActionBar
     */
    public void showActionBar()
    {
        getActionBar().show();
        if(mSettings.isHideActionBar())
        {
            runTimerActionBar();
        }
    }
    
    /**
     * hide layers fragment
     */
    public void hideLayersFragment()
    {
        if(mLayersLayout.isShown())
        {
            mLayersLayout.setVisibility(View.GONE);
            if(!mMapLayout.isShown())
            {
                mMapLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * show layers fragment
     */
    public void showLayersFragment()
    {
        if(!mLayersLayout.isShown())
        {
            Display display = getWindowManager().getDefaultDisplay();
            
            Point outSize = new Point();
            display.getSize(outSize);
            
            float dp_width = ConvertUnits.px2dp(outSize.x);
            
            boolean run = true;
            int i = 3;
            while(run)
            {
                if(i == 0)
                {
                    mMapLayout.setVisibility(View.GONE);
                    run = false;
                }
                else if(dp_width/(i+1) > MIN_WIDTH_PANEL_DP)
                {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mLayersLayout.getLayoutParams();
                    params.weight = i;
                    run = false;
                }
                
                i--;
            }
            
            hideActionBarNow();
            mLayersLayout.setVisibility(View.VISIBLE);
        }
    }
    
    // getter, setter =====================================================
    
    /**
     * @return height of ActionBar
     */
    public int getActionBarHeight()
    {
        return getActionBar().getHeight();
    }
    
    /**
     * check if LayersFragment is hidden
     * @return
     */
    public boolean isHiddenLayersFragment()
    {
        return !mLayersLayout.isShown();
    }
    
    /**
     * @return activity mode
     */
    public ActivityMode getActivityMode()
    {
        return data.activityMode;
    }
    
    /**
     * @return LayersFragment
     */
    public LayersFragment getLayersFragment()
    {
        return mLayersFragment;
    }
    
    /**
     * @return MapFragment
     */
    public MapFragment getMapFragment()
    {
        return mMapFragment;
    }
    
    /**
     * @return the mLocationWorker
     */
    public LocationWorker getLocationWorker()
    {
        return mLocationWorker;
    }
    
    /**
     * @return if is run add point mode of edit
     */
    public boolean isAddPointMode()
    {
        return data.addPointMode;
    }
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ConvertUnits.setDensity(getResources().getDisplayMetrics().density);
        checkAppDirectory();
        
        getActionBar().setHomeButtonEnabled(true);
        
        mMapLayout = (LinearLayout)findViewById(R.id.map_layout);
        mLayersLayout = (LinearLayout)findViewById(R.id.layers_layout);
        
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        mLayersFragment = (LayersFragment)getFragmentManager().findFragmentById(R.id.layers_fragment);
        
        mLocationWorker = new LocationWorker(this);
        MapView map = mMapFragment.getMap();
        map.setMainActivity(this);
        
        if(mSettings.isHideActionBar())
        {
            runTimerActionBar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        
        mMenuRunLocation = menu.findItem(R.id.menu_location);
        mMenuShowLocation = menu.findItem(R.id.menu_show_location);
        mMenuRecord = menu.findItem(R.id.menu_record);
        mMenuEdit = menu.findItem(R.id.menu_edit);
        mMenuAddPoint = menu.findItem(R.id.menu_add_point);
        setActionBarIcons();
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        // stop/start location
        if(mMenuRunLocation.getItemId() == id)
        {
            if(mLocationWorker.isRunLocation())
            {
                stopLocation();
            }
            else
            {
                startLocation();
            }
        }
        // show location
        else if(mMenuShowLocation.getItemId() == id)
        {
            try
            {
                mMapFragment.showLocation();
            }
            catch(RuntimeException e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        // record
        else if(mMenuRecord.getItemId() == id)
        {
            if(data.activityMode == ActivityMode.RECORD)
            {
                startExploreMode();
            }
            else
            {
                startRecordMode();
            }
        }
        // add point mode
        else if(mMenuAddPoint.getItemId() == id)
        {
            if(data.addPointMode)
            {
                data.addPointMode = false;
                mMapFragment.setCoordinatesAddPointM(null);
            }
            else
            {
                data.addPointMode = true;
            }
        }
        // edit
        else if(mMenuEdit.getItemId() == id)
        {
            if(data.activityMode == ActivityMode.EDIT)
            {
                getLayersFragment().removeSelectedObject();
                startExploreMode();
            }
            else
            {
                startEditMode();
            }
        }
        // hide icon
        else if(android.R.id.home == id)
        {
            hideActionBarNow();
        }        
        
        setActionBarIcons();
        mMapFragment.setMapTools();
     
        return true;
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        mLocationWorker.pause();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        mLocationWorker.resume();
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        // Shown layers
        outState.putBoolean(SHOWN_LAYERS, mLayersLayout.isShown());
        
        // gps state
        outState.putSerializable(LOCATION_WORKER_DATA, mLocationWorker.getData());
        
        //MainActivity state
        outState.putSerializable(MAIN_ACTIVITY_DATA, data);
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        
        // Shown layers
        if(savedInstanceState.getBoolean(SHOWN_LAYERS))
        {
            showLayersFragment();
        }
        
        // GPS state
        mLocationWorker.setData(savedInstanceState.getSerializable(LOCATION_WORKER_DATA));
        
        //MainActivity state
        data = (MainActivityData) savedInstanceState.getSerializable(MAIN_ACTIVITY_DATA);
        
        mMapFragment.setMapTools();
        setActionBarIcons();
    }
    
    // private methods ========================================================
    
    /**
     * timer for hidding ActionBar
     */
    private void runTimerActionBar()
    {
        cancelTimer();
        
        timer = new Timer();
        timer.schedule(new HideActionBarTask(), mSettings.getTimeHideActionBar());
    }
    
    /**
     * cancel timer for hidding action bar
     */
    private void cancelTimer()
    {
        if(timer != null)
        {
            timer.cancel();
            timer.purge();
        }        
    }
    
    /**
     * set icons in ActionBar by location
     */
    private void setActionBarIcons()
    {
        // location icons
        if(mLocationWorker.isRunLocation())
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_gps_on));
            mMenuShowLocation.setVisible(true);
        }
        else
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_gps_off));
            mMenuShowLocation.setVisible(false);
        }
        
        // record icon
        if(data.activityMode == ActivityMode.RECORD)
        {
            mMenuRecord.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_record_on));
        }
        else
        {
            mMenuRecord.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_record_off));
        }
        
        // edit icon
        if(data.activityMode == ActivityMode.EDIT)
        {
            mMenuEdit.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_edit_on));
            
            // add point icon
            mMenuAddPoint.setVisible(true);
            if(data.addPointMode)
            {
                mMenuAddPoint.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_add_point_on));
            }
            else
            {
                mMenuAddPoint.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_add_point_off));
            }
        }
        else
        {
            mMenuEdit.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_edit_off));
            mMenuAddPoint.setVisible(false);
        }
    }

    /**
     * start location service
     */
    private void startLocation()
    {
        mLocationWorker.start();
        
        mMapFragment.startLocation();
    }
    
    /**
     * stop location service
     */
    private void stopLocation()
    {
        if(data.activityMode == ActivityMode.RECORD)
        {
            startExploreMode();
        }
        
        mLocationWorker.stop();
        
        mMapFragment.stopLocation();        
    }
    
    /**
     * start record mode of activity
     */
    private void startRecordMode()
    {
        data.activityMode = ActivityMode.RECORD;
        if(!mLocationWorker.isRunLocation())
        {
            startLocation();
        }
    }

    /**
     * start explore mode of activity
     */
    private void startExploreMode()
    {
        data.activityMode = ActivityMode.EXPLORE;
    }

    /**
     * start edit mode of activity
     */
    private void startEditMode()
    {
        data.activityMode = ActivityMode.EDIT;
    }
    
    /**
     * make directory of application when it doesn't exist
     */
    private void checkAppDirectory()
    {
        if (!APP_DIRECTORY.exists())
        {
            APP_DIRECTORY.mkdir();
        }
    }
    
    /**
     * hide action bar
     */
    private void hideActionBar()
    {
        getActionBar().hide();
    }
    
    /**
     * hide now actionBar
     */
    private void hideActionBarNow()
    {
        cancelTimer();
        hideActionBar();        
    }
    // classes =================================================================
    /**
     * task for hidding action bar
     * @author jules
     *
     */
    class HideActionBarTask extends TimerTask
    {
        private HideActionBarRunnable hideActionBarRunnable = new HideActionBarRunnable();
        
        public void run()
        {
            MainActivity.this.runOnUiThread(hideActionBarRunnable);
        }
     }
    
    /**
     * runnable hidding action bar
     * @author jules
     *
     */
    class HideActionBarRunnable implements Runnable
    {
        public void run()
        {
            hideActionBar();
        }
    };
}
