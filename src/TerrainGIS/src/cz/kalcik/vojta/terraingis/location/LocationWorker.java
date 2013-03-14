package cz.kalcik.vojta.terraingis.location;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * class for manage GPS and other location services
 * @author jules
 *
 */
public class LocationWorker implements LocationListener
{
    // constants =====================================================================
    
    private enum ProviderType {GPS, BOTH};
    // data =========================================================================
    public static class LocationWorkerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public boolean runLocation;

        public LocationWorkerData(boolean runLocation, int currentMindist)
        {
            this.runLocation = runLocation;
        }
    }
    
    // attributes ====================================================================
    private MainActivity mMainActivity;
    private Settings mSettings = Settings.getInstance();
    private MapView map;
    private Coordinate locationPoint = new Coordinate();
    private CommonLocationListener mCommon;
    private GPSStatusListener mGPSStatusListener = new GPSStatusListener();
    private ProviderType currentProvider;
    private LocationWorkerData data = new LocationWorkerData(false, Settings.LOCATION_MINDIST_DEFAULT);
    private boolean hasGPS;
    private long mLastGPSMillis = 0;
    
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
    public void resume()
    {
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
    public void onLocationChanged(Location location)
    {
        synchronized(this)
        {
            // switch to GPS
            if(location.getProvider().equals(LocationManager.GPS_PROVIDER))
            {
                mLastGPSMillis = SystemClock.elapsedRealtime();
                
                if(currentProvider == ProviderType.BOTH)
                {
                    currentProvider = ProviderType.GPS;
                    switchProvider();
                }
            }
        }

        locationPoint.x = location.getLongitude();
        locationPoint.y = location.getLatitude();
        locationPoint.z = location.getAltitude();
        
        map.setLonLatLocation(locationPoint);
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    public void onProviderEnabled(String provider)
    {

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
            mCommon.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mSettings.getLocationMinTime(),
                    Settings.LOCATION_MINDIST_DEFAULT, this);
            mCommon.locationManager.addGpsStatusListener(mGPSStatusListener);
        }        
    }
    
    /**
     * start Network listen
     */
    private void runNetwork()
    {
        // default
        mCommon.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mSettings.getLocationMinTime(),
                Settings.LOCATION_MINDIST_DEFAULT, this);
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
            mCommon.locationManager.removeGpsStatusListener(mGPSStatusListener);
        }
        
        mCommon.locationManager.removeUpdates(this);
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
    }
    
    // classes =======================================================================
    
    /**
     * listener for GPS status
     * @author jules
     *
     */
    private class GPSStatusListener implements GpsStatus.Listener
    {
        private boolean isGPSFix = false;
        
        public synchronized void onGpsStatusChanged(int event)
        {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
            {
                if (mLastGPSMillis != 0)
                {
                    isGPSFix = (SystemClock.elapsedRealtime() - mLastGPSMillis) < mSettings.getLocationMinTime() * 2;
                }
            }
            else if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
            {
                isGPSFix = true;
            }
            
            ProviderType previousType = currentProvider;
            currentProvider = isGPSFix ? ProviderType.GPS : ProviderType.BOTH;
            
            if(previousType != currentProvider)
            {
                switchProvider();
            }
        }
    }
}