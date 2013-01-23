package cz.kalcik.vojta.terraingis.components;

import java.io.Serializable;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

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
public class LocationWorker
{
    // constants =====================================================================
    private final int MINTIME = 2000;
    private final int MINDIST = 3;   
    private final FixReceiver FIX_RECEIVER = new FixReceiver();
    private final IntentFilter INTENT_FILTER = new IntentFilter("android.location.GPS_FIX_CHANGE");
    
    private enum ProviderType {GPS, BOTH};
    
    // data =========================================================================
    public static class LocationWorkerData implements Serializable
    {
        private static final long serialVersionUID = -2490598884003552835L;
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
    private LocationListener locationListener;
    private MapView map;
    private boolean hasGPS;
    private Context context;
    private ProviderType currentProvider;
    private LocationWorkerData data = new LocationWorkerData(false, MINDIST);
    private boolean validGPS = true; // for external bluetooth GPS

    /**
     * constructor
     * @param context
     * @param map
     */
    public LocationWorker(Context context, MapView map)
    {
        this.map = map;
        this.context = context;
        hasGPS = hasGPSDevice();
        locationManager = 
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        locationListener = new MyLocationListener(map);
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
    
    // private methods ===============================================================
    /**
     * check if device has GPS
     * @param context
     * @return
     */
    private boolean hasGPSDevice()
    {
        final LocationManager mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(mgr == null)
        {
            return false;
        }
        
        final List<String> providers = mgr.getAllProviders();
        if ( providers == null )
        {
            return false;
        }
        
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    /**
     * start GPS listen
     */
    private void runGPS()
    {
        if(hasGPS)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINTIME, data.currentMindist, locationListener);
            
            context.registerReceiver(FIX_RECEIVER, INTENT_FILTER);
        }        
    }
    
    /**
     * start Network listen
     */
    private void runNetwork()
    {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINTIME, data.currentMindist, locationListener);       
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
            context.unregisterReceiver(FIX_RECEIVER);
        }
        
        locationManager.removeUpdates(locationListener);
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
     * Location listener
     * @author jules
     *
     */
    class MyLocationListener implements LocationListener
    {
        private MapView map;
        private Coordinate locationPoint = new Coordinate();
        
        /**
         * constructor
         * @param map
         */
        public MyLocationListener(MapView map)
        {
            this.map = map;
        }
        
        /**
         * receive new location update
         */
        public void onLocationChanged(Location location)
        {
            // switch to GPS
            if(currentProvider == ProviderType.BOTH &&
               location.getProvider().equals(LocationManager.GPS_PROVIDER) &&
               validGPS)
            {
                currentProvider = ProviderType.GPS;
                switchProvider();
            }
            
            // not show not valid GPS
            if(location.getProvider().equals(LocationManager.GPS_PROVIDER) && !validGPS)
            {
                map.setLocationValid(false);
                return;
            }
            
            locationPoint.x = location.getLongitude();
            locationPoint.y = location.getLatitude();
            locationPoint.z = location.getAltitude();
            map.setLonLatLocation(locationPoint);
            
            float accuracy = location.getAccuracy();
            
            // change mindist by accuracy
            if(accuracy < MINDIST * 2)
            {
                data.currentMindist = 0;
            }
            else
            {
                data.currentMindist = MINDIST;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d("TerrainGIS", String.format("onStatusChanged %d %s", status, provider));
            if(provider.equals(LocationManager.GPS_PROVIDER) && status != LocationProvider.AVAILABLE)
            {
                validGPS = false;
                if(currentProvider != ProviderType.BOTH)
                {
                    currentProvider = ProviderType.BOTH;
                    switchProvider();
                }
            }
            else if(provider.equals(LocationManager.GPS_PROVIDER) && status == LocationProvider.AVAILABLE)
            {
                validGPS = true;
            }
        }

        public void onProviderEnabled(String provider)
        {
            Log.d("TerrainGIS", String.format("onProviderEnabled %s", provider));
            if(provider.equals(LocationManager.GPS_PROVIDER))
            {
                currentProvider = ProviderType.BOTH;
                switchProvider();
            }
        }

        public void onProviderDisabled(String provider)
        {
            Log.d("TerrainGIS", String.format("onProviderDisabled %s", provider));
        }
    }
    
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