package cz.kalcik.vojta.terraingis;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.LocationWorker;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.R;

public class MainActivity extends FragmentActivity
{
    // constants ==========================================================
    private static final String LOCATION_WORKER_DATA = "LocationWorkerData";
    private static final String SHOWN_LAYERS = "ShownLayers";
    private static final float MIN_WIDTH_PANEL_DP = 300;
    
    // properties =========================================================
    private MenuItem mMenuRunLocation;
    private MenuItem mMenuShowLocation;
    private MenuItem mMenuRecord;
    private Timer timer;
    private LocationWorker mLocationWorker;
    private Settings mSettings = Settings.getInstance();
    private MapFragment mMapFragment;
    private LayersFragment mLayersFragment;
    private LinearLayout mMapLayout;
    private LinearLayout mLayersLayout;
    private boolean mRecordMode = false;
    
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
            float dp_width = ConvertUnits.px2dp(display.getWidth());
            
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
     * @return MapView
     */
    public MapView getMap()
    {
        return mMapFragment.getMap();
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
     * @return record mode state
     */
    public boolean isRecordMode()
    {
        return mRecordMode;
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
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ConvertUnits.setDensity(getResources().getDisplayMetrics().density);
        
        mMapLayout = (LinearLayout)findViewById(R.id.map_layout);
        mLayersLayout = (LinearLayout)findViewById(R.id.layers_layout);
        
        mMapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mLayersFragment = (LayersFragment)getSupportFragmentManager().findFragmentById(R.id.layers_fragment);
        
        MapView map = mMapFragment.getMap();
        map.setMainActivity(this);
        mLocationWorker = new LocationWorker(this);
        
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
                mLocationWorker.start();
            }
        }
        // show location
        else if(mMenuShowLocation.getItemId() == id)
        {
            if(!mMapFragment.getMap().showLocation())
            {
                Toast.makeText(this, R.string.warning_location_fix, Toast.LENGTH_LONG).show();
            }
        }
        // record
        else if(mMenuRecord.getItemId() == id)
        {
            if(mRecordMode)
            {
                stopRecord();
            }
            else
            {
                startRecord();
            }
        }
        
        setActionBarIcons();
     
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
        setActionBarIcons();
    }
    
    // private methods ========================================================
    
    /**
     * timer for hidding ActionBar
     */
    private void runTimerActionBar()
    {
        if(timer != null)
        {
            timer.cancel();
            timer.purge();
        }
        
        timer = new Timer();
        timer.schedule(new HideActionBarTask(), mSettings.getTimeHideActionBar());
    }
    
    /**
     * set icons in ActionBar by location
     */
    private void setActionBarIcons()
    {
        // location icons
        if(mLocationWorker.isRunLocation())
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.gps_on));
            mMenuShowLocation.setVisible(true);
        }
        else
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.gps_off));
            mMenuShowLocation.setVisible(false);
        }
        
        // record icon
        if(mRecordMode)
        {
            mMenuRecord.setIcon(this.getResources().getDrawable(R.drawable.record_on));
        }
        else
        {
            mMenuRecord.setIcon(this.getResources().getDrawable(R.drawable.record_off));
        }
    }

    /**
     * stop location service
     */
    private void stopLocation()
    {
        if(mRecordMode)
        {
            stopRecord();
        }
        
        mLocationWorker.stop();
    }
    
    /**
     * start record state
     */
    private void startRecord()
    {
        mRecordMode = true;
        if(!mLocationWorker.isRunLocation())
        {
            mLocationWorker.start();
        }
        mMapFragment.changeRecordButtons();
    }

    /**
     * stop record state
     */
    private void stopRecord()
    {
        mRecordMode = false;
        mMapFragment.changeRecordButtons();
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
            getActionBar().hide();
        }
    };
}
