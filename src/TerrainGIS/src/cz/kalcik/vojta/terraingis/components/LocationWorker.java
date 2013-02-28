package cz.kalcik.vojta.terraingis.components;

import java.io.Serializable;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * class for manage GPS and other location services
 * @author jules
 *
 */
public class LocationWorker implements LocationListener
{
    // constants =====================================================================
    private final FixReceiver FIX_RECEIVER = new FixReceiver();
    private final IntentFilter INTENT_FILTER = new IntentFilter("android.location.GPS_FIX_CHANGE");
    
    private enum ProviderType {GPS, BOTH};
    // data =========================================================================
    public static class LocationWorkerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public boolean runLocation;
        public int currentMindist;

        public LocationWorkerData(boolean runLocation, int currentMindist)
        {
            this.runLocation = runLocation;
            this.currentMindist = currentMindist;
        }
    }
    
    // attributes ====================================================================
    private LocationManager locationManager;
    private MainActivity mMainActivity;
    private Settings mSettings = Settings.getInstance();
    private MapView map;
    private Coordinate locationPoint = new Coordinate();
    private boolean hasGPS;
    private ProviderType currentProvider;
    private LocationWorkerData data = new LocationWorkerData(false, Settings.LOCATION_MINDIST_DEFAULT);
    private boolean isPaused = false;
    private CommonLocationListener mCommon;
    
    /**
     * constructor
     * @param context
     * @param map
     */
    public LocationWorker(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
        mCommon = new CommonLocationListener(mMainActivity);
        map = mMainActivity.getMap();
        hasGPS = mCommon.hasGPSDevice();
        locationManager = 
                (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
    }
    
    // public methods ================================================================
    /**
     * start location service
     * @return
     */
    public void start()
    {
        startLocation();
        data.runLocation = true;
        
        map.startLocation();
    }
    
    /**
     * stop location service
     */
    public void stop()
    {
        map.stopLocation();
        
        data.runLocation = false;
        stopLocation();
    }
    
    /**
     * resume location service
     */
    public synchronized void resume()
    {
        isPaused = false;
        
        if(data.runLocation)
        {
            startLocation();
        }
    }   
 
    /**
     * pause location service
     */
    public void pause()
    {
        isPaused = true;
        if(data.runLocation)
        {
            stopLocation();
        }
    }

    // getter setter =================================================================
    
    public LocationWorkerData getData()
    {
        return data;
    }
    
    public void setData(Serializable data)
    {
        this.data = (LocationWorkerData)data;
    }
    
    public boolean isRunLocation()
    {
        return data.runLocation;
    }
    
    // on methods ===================================================================
    
    /**
     * receive new location update
     */
    public synchronized void onLocationChanged(Location location)
    {
        // switch to GPS
        if(currentProvider == ProviderType.BOTH &&
           location.getProvider().equals(LocationManager.GPS_PROVIDER) &&
           mCommon.validGPS)
        {
            currentProvider = ProviderType.GPS;
            switchProvider();
        }
        
        // not show not valid GPS
        if(location.getProvider().equals(LocationManager.GPS_PROVIDER) && !mCommon.validGPS)
        {
            map.setLocationValid(false);
            return;
        }
        

        float accuracy = location.getAccuracy();
        
        // change mindist by accuracy
        if(accuracy < Settings.LOCATION_MINDIST_DEFAULT * 2)
        {
            data.currentMindist = 0;
        }
        else
        {
            data.currentMindist = Settings.LOCATION_MINDIST_DEFAULT;
        }
        
        // if paused
        if(isPaused)
        {
            return;
        }
        
        locationPoint.x = location.getLongitude();
        locationPoint.y = location.getLatitude();
        locationPoint.z = location.getAltitude();
        
        map.setLonLatLocation(locationPoint);
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        mCommon.statusChanged(provider, status, extras);
        
        if(!mCommon.validGPS && currentProvider != ProviderType.BOTH)
        {
            currentProvider = ProviderType.BOTH;
            switchProvider();
        }
    }

    public void onProviderEnabled(String provider)
    {
        if(provider.equals(LocationManager.GPS_PROVIDER))
        {
            currentProvider = ProviderType.BOTH;
            switchProvider();
        }
    }

    public void onProviderDisabled(String provider)
    {
        
    }
    
    // private methods ===============================================================
    /**
     * start GPS listen
     */
    private void runGPS()
    {
        if(hasGPS)
        {
            // default
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mSettings.getLocationMinTime(),
                    data.currentMindist, this);
            
            mMainActivity.registerReceiver(FIX_RECEIVER, INTENT_FILTER);
        }        
    }
    
    /**
     * start Network listen
     */
    private void runNetwork()
    {
        // default
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mSettings.getLocationMinTime(),
                data.currentMindist, this);
    }

    /**
     * stop location service
     */
    private void startLocation()
    {
        currentProvider = ProviderType.BOTH;
        runGPS();
        runNetwork();
    }
    
    /**
     * stop location service
     */
    private void stopLocation()
    {
        if(hasGPS)
        {
            mMainActivity.unregisterReceiver(FIX_RECEIVER);
        }
        
        locationManager.removeUpdates(this);
    }
    
    /**
     * switch to only GPS or both
     * @param providerType
     */
    private void switchProvider()
    {
        stopLocation();

        runGPS();
        
        if(currentProvider == ProviderType.BOTH)
        {
            runNetwork();
        }
        
        Log.d("TerrainGIS", "switched to "+currentProvider.toString());
    }
    
    // classes =======================================================================
    
    /**
     * Fix GPS receiver
     * @author jules
     *
     */
    class FixReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle b = intent.getExtras();
            boolean isFix = b.getBoolean("enabled");
            currentProvider = isFix ? ProviderType.GPS : ProviderType.BOTH;
            Log.d("TerrainGIS", String.format("onReceive fix %s", currentProvider.toString()));
            
            switchProvider();
        }        
    }
}