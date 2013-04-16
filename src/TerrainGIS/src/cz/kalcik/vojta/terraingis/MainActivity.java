package cz.kalcik.vojta.terraingis;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.components.TileCache;
import cz.kalcik.vojta.terraingis.dialogs.ExitDialog;
import cz.kalcik.vojta.terraingis.fragments.AttributesFragment;
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
    private AttributesFragment mAttributesFragment;
    private LinearLayout mMapLayout;
    private LinearLayout mPanelLayout;
    private LinearLayout mLayersLayout;
    private LinearLayout mAttributesLayout;
    private MainActivityData data = new MainActivityData(ActivityMode.EXPLORE, false);
    private ConnectivityManager mConnectivityManager; 
    
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
        if(mPanelLayout.isShown())
        {
            mPanelLayout.setVisibility(View.GONE);
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
        if(!mPanelLayout.isShown())
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
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mPanelLayout.getLayoutParams();
                    params.weight = i;
                    run = false;
                }
                
                i--;
            }
            
            hideActionBarNow();
            mPanelLayout.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * @return if can be selected object
     */
    public boolean canSelectObject()
    {
        return data.activityMode != ActivityMode.RECORD;
    }
    
    /**
     * @return true if network is available
     */
    public boolean isNetworkAvailable()
    {
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    // getter, setter =====================================================
    
    /**
     * check if LayersFragment is hidden
     * @return
     */
    public boolean isHiddenLayersFragment()
    {
        return !mPanelLayout.isShown();
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
     * @return AttributesFragment
     */
    public AttributesFragment getAttributesFragment()
    {
        return mAttributesFragment;
    }
    
    /**
     * @return MapFragment
     */
    public MapFragment getMapFragment()
    {
        return mMapFragment;
    }
    
    /**
     * @return layers layout
     */
    public LinearLayout getLayersLayout()
    {
        return mLayersLayout;
    }

    /**
     * @return attributes layout
     */
    public LinearLayout getAttributesLayout()
    {
        return mAttributesLayout;
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
        mPanelLayout = (LinearLayout)findViewById(R.id.panel_layout);
        mLayersLayout = (LinearLayout)findViewById(R.id.layers_layout);
        mAttributesLayout = (LinearLayout)findViewById(R.id.attributes_layout);
        
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        mLayersFragment = (LayersFragment)getFragmentManager().findFragmentById(R.id.layers_fragment);
        mAttributesFragment = (AttributesFragment)getFragmentManager().findFragmentById(R.id.attributes_fragment);
        
        mLocationWorker = new LocationWorker(this);
        MapView map = mMapFragment.getMap();
        map.setMainActivity(this);
        
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        
        // tile cache
        TileCache.getInstance().open(TILE_CACHE_FILE, getResources(), map);
        
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
                changeMode(ActivityMode.EXPLORE);
            }
            else
            {
                changeMode(ActivityMode.RECORD);
            }
        }
        // add point mode
        else if(mMenuAddPoint.getItemId() == id)
        {
            if(data.addPointMode)
            {
                setAddPointMode(false);
            }
            else
            {
                setAddPointMode(true);
            }
        }
        // edit
        else if(mMenuEdit.getItemId() == id)
        {
            if(data.activityMode == ActivityMode.EDIT)
            {
                changeMode(ActivityMode.EXPLORE);
            }
            else
            {
                changeMode(ActivityMode.EDIT);
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
        outState.putBoolean(SHOWN_LAYERS, mPanelLayout.isShown());
        
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
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(mPanelLayout.isShown())
            {
                hideLayersFragment();
            }
            else
            {
                ExitDialog dialog = new ExitDialog();
                dialog.setMessage(getString(R.string.exit_message));
                showDialog(dialog);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
            changeMode(ActivityMode.EXPLORE);
        }
        
        mLocationWorker.stop();
        
        mMapFragment.stopLocation();        
    }
    
    private void changeMode(ActivityMode mode)
    {
        // old mode
        getLayersFragment().removeSelectedObject();
        
        if(data.activityMode == ActivityMode.EDIT)
        {
            setAddPointMode(false);
        }
        
        data.activityMode = mode;
        
        // new mode
        if(data.activityMode == ActivityMode.RECORD)
        {
            if(!mLocationWorker.isRunLocation())
            {
                startLocation();
            }
        }
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
    
    /**
     * change add point mode
     * @param state
     */
    private void setAddPointMode(boolean state)
    {
        data.addPointMode = state;
        
        if(!state)
        {
            mMapFragment.setCoordinatesAddPointM(null);
        }
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
