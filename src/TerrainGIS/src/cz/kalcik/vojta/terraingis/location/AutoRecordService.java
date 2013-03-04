/**
 * 
 */
package cz.kalcik.vojta.terraingis.location;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

/**
 * @author jules
 * service for automatic recording
 */
public class AutoRecordService extends Service implements LocationListener
{
    // attributes ==================================================================
    private CommonLocationListener mCommon;
    private Settings mSettings = Settings.getInstance();
    private final IBinder binder = new AutoRecordBinder();
    private ArrayList<Coordinate> mPoints = new ArrayList<Coordinate>();
    private MapFragment mMapFragment = null;
    
    // public methods ==============================================================
    public synchronized void setMapFragment(MapFragment mapFragment)
    {
        mMapFragment = mapFragment;
        if(mPoints.size() > 0)
        {
            mMapFragment.recordPointsAuto(mPoints);
            mPoints.clear();
        }
    }
    
    public synchronized void unsetMapFragment()
    {
        mMapFragment = null;
    }
    
    // on methods ==================================================================
    @Override
    public void onCreate()
    {    
        mCommon = new CommonLocationListener(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(!mCommon.hasGPSDevice())
        {
            Toast.makeText(this, R.string.device_no_gps_error, Toast.LENGTH_LONG).show();
            
            stopSelf();
        }
        else
        {
            runGPS();
        }            
        
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }
    
    @Override
    public synchronized void onLocationChanged(Location location)
    {
        if(location.getAccuracy() <= mSettings.getRecordMinAccuracy())
        {
            Coordinate point = new Coordinate(location.getLongitude(), location.getLatitude(),
                    location.getAltitude());
            
            if(mMapFragment == null)
            {
                mPoints.add(point);
            }
            else
            {
                mMapFragment.recordPointAuto(point);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }
        
    @Override
    public void onDestroy()
    {
        stopGPS();
        
        super.onDestroy();
    }

    // private methods ==============================================================
    /**
     * start GPS listen
     */
    private void runGPS()
    {
        // default
        mCommon.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mSettings.getLocationMinTime(),
                mSettings.getAutoRecordMinDist(), this);
    }
    
    /**
     * stop GPS
     */
    private void stopGPS()
    {
        mCommon.locationManager.removeUpdates(this);
    }
    
    // classes =====================================================================
    /**
     * Bind interface for service interaction
     */
    public class AutoRecordBinder extends Binder
    {
        /**
         * Called by the activity when binding.
         * Returns itself.
         * @return the service
         */
        public AutoRecordService getService()
        {         
            return AutoRecordService.this;
        }
    }
}
