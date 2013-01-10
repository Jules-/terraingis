package cz.kalcik.vojta.terraingis.components;

import java.util.List;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * class for manage GPS and other location services
 * @author jules
 *
 */
public class LocationWorker
{
    // constants =====================================================================
    private final int MINTIME = 2000;
    private final int MINDIST = 2;   
    private final FixReceiver FIX_RECEIVER = new FixReceiver();
    private final IntentFilter INTENT_FILTER = new IntentFilter("android.location.GPS_FIX_CHANGE");
    
    private enum ProviderType {GPS, BOTH};
    
    // attributes ====================================================================
    private LocationManager locationManager;
    private LocationListener locationListener;
    private MapView map;
    private boolean hasGPS;
    private boolean runLocation = false;
    private Context context;
    private ProviderType currentProvider;
        
    /**
     * constructor
     * @param context
     * @param map
     */
    public LocationWorker(Context context, MapView map)
    {
        this.map = map;
        this.context = context;
        hasGPS = hasGPSDevice(context);
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
        runGPS();
        runNetwork();
        runLocation = true;
        
        map.startLocation();
    }
    
    /**
     * stop location service
     */
    public void stop()
    {
        map.stopLocation();
        
        runLocation = false;
        stopLocation();
    }
    
    /**
     * resume location service
     */
    public void resume()
    {
        if(runLocation)
        {
            runGPS();
            runNetwork();
        }
    }   
 
    /**
     * pause location service
     */
    public void pause()
    {
        if(runLocation)
        {
            stopLocation();
        }        
    }
    
    // private methods ===============================================================
    /**
     * check if device has GPS
     * @param context
     * @return
     */
    private boolean hasGPSDevice(Context context)
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINTIME, MINDIST, locationListener);
            
            context.registerReceiver(FIX_RECEIVER, INTENT_FILTER);
        }        
    }
    
    /**
     * start Network listen
     */
    private void runNetwork()
    {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINTIME, MINDIST, locationListener);       
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
        private Point2D.Double locationPoint = new Point2D.Double();
        
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
            if(currentProvider == ProviderType.BOTH && location.getProvider().equals(LocationManager.GPS_PROVIDER))
            {
                currentProvider = ProviderType.GPS;
                switchProvider();
            }
            
            locationPoint.setLocation(location.getLongitude(), location.getLatitude());
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
            Toast.makeText(context, "fix change", Toast.LENGTH_LONG).show();
            Bundle b = intent.getExtras();
            boolean isFix = b.getBoolean("enabled");
            currentProvider = isFix ? ProviderType.GPS : ProviderType.BOTH;
            
            switchProvider();
        }        
    }
}