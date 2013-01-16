package cz.kalcik.vojta.terraingis;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.components.LocationWorker;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.view.MapView;

public class MainActivity extends FragmentActivity
{
    // constants ==========================================================
    private static String LOCATION_WORKER_DATA = "LocationWorkerData";
    
    // properties =========================================================
    private MenuItem menuRunLocation;
    private MenuItem menuShowLocation;
    private Timer timer;
    private LocationWorker locationWorker;
    private Settings settings = Settings.getInstance();
    private MapFragment mapFragment;
    private LayersFragment layersFragment;
    
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
    
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        layersFragment = (LayersFragment)getSupportFragmentManager().findFragmentById(R.id.layers_fragment);
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
        // gps state
        outState.putSerializable(LOCATION_WORKER_DATA, locationWorker.getData());
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        
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
