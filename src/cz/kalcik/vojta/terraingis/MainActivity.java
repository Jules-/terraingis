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

import cz.kalcik.vojta.terraingis.components.LocationWorker;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.view.MapView;

public class MainActivity extends FragmentActivity
{
    // constants ==========================================================
    private static final String LOCATION_WORKER_DATA = "LocationWorkerData";
    private static final String SHOWN_LAYERS = "ShownLayers";
    private static final float MIN_WIDTH_PANEL_DP = 250;
    
    // properties =========================================================
    private MenuItem menuRunLocation;
    private MenuItem menuShowLocation;
    private Timer timer;
    private LocationWorker locationWorker;
    private Settings settings = Settings.getInstance();
    private MapFragment mapFragment;
    private LinearLayout mMapLayout;
    private LinearLayout mLayersLayout;
    
    // public methods =====================================================
    
    /**
     * show ActionBar
     */
    public void showActionBar()
    {
        getActionBar().show();
        if(settings.isHideActionBar())
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
            float dp_width = px2dp(display.getWidth());
            
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
    
    /**
     * convert value in dp to px
     * @param dp
     * @return
     */
    public int dp2px(float dp)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    // getter, setter =====================================================
    
    /**
     * return height of ActionBar
     * @return
     */
    public int getActionBarHeight()
    {
        return getActionBar().getHeight();
    }
    
    /**
     * return MapView
     * @return
     */
    public MapView getMap()
    {
        return mapFragment.getMap();
    }
    
    /**
     * check if LayersFragment is hidden
     * @return
     */
    public boolean isHiddenLayersFragment()
    {
        return !mLayersLayout.isShown();
    }
    
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mMapLayout = (LinearLayout)findViewById(R.id.map_layout);
        mLayersLayout = (LinearLayout)findViewById(R.id.layers_layout);
        
        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        
        MapView map = mapFragment.getMap();
        map.setMainActivity(this);
        
        locationWorker = new LocationWorker(this, map);
        if(settings.isHideActionBar())
        {
            runTimerActionBar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        
        menuRunLocation = menu.findItem(R.id.menu_location);
        menuShowLocation = menu.findItem(R.id.menu_show_location);
        setActionBarByLocation();
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(menuRunLocation.getItemId() == id)
        {
            if(locationWorker.isRunLocation())
            {
                locationWorker.stop();
            }
            else
            {
                locationWorker.start();
            }
            setActionBarByLocation();
        }
        else if(menuShowLocation.getItemId() == id)
        {
            if(!mapFragment.getMap().showLocation())
            {
                Toast.makeText(this, R.string.warning_location_fix, Toast.LENGTH_LONG).show();
            }
        }
     
        return true;
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        locationWorker.pause();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        locationWorker.resume();
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        // Shown layers
        outState.putBoolean(SHOWN_LAYERS, mLayersLayout.isShown());
        
        // gps state
        outState.putSerializable(LOCATION_WORKER_DATA, locationWorker.getData());
        
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
        locationWorker.setData(savedInstanceState.getSerializable(LOCATION_WORKER_DATA));
        setActionBarByLocation();
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
        timer.schedule(new HideActionBarTask(), settings.getTimeHideActionBar());
    }
    
    /**
     * set icons in ActionBar by location
     */
    private void setActionBarByLocation()
    {
        if(locationWorker.isRunLocation())
        {
            menuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.gps_off));
            menuShowLocation.setVisible(true);
        }
        else
        {
            menuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.gps_on));
            menuShowLocation.setVisible(false);
        }        
    }
    
    /**
     * convert value in px to dp
     * @param px
     * @return dp
     */
    private float px2dp(int px)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return px/scale;
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
