package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * class for manage GPS and other location services
 * @author jules
 *
 */
public class LocationWorker
{
    private LocationManager locationManager;
    private LocationListener locationListener;
    private MapView map;
    
    /**
     * constructor
     * @param context
     * @param map
     */
    public LocationWorker(Context context, MapView map)
    {
        this.map = map;
        locationManager = 
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        locationListener = new MyLocationListener(map);
    }
    
    // public methods ================================================================
    
    public void start()
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
        map.startLocation();
    }
    
    public void stop()
    {
        locationManager.removeUpdates(locationListener);
        map.stopLocation();
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
        
        public MyLocationListener(MapView map)
        {
            this.map = map;
        }
        
        public void onLocationChanged(Location location)
        {
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
}