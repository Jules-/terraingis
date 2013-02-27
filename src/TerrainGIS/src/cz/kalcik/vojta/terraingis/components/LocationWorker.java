package cz.kalcik.vojta.terraingis.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * class for manage GPS and other location services
 * @author jules
 *
 */
public class LocationWorker extends Service
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
        public boolean runAutoRecord;
        public boolean runAddPointRecord;

        public LocationWorkerData(boolean runLocation, int currentMindist,
                boolean runAutoRecord, boolean runAddPointRecord)
        {
            this.runLocation = runLocation;
            this.currentMindist = currentMindist;
            this.runAutoRecord = runAutoRecord;
            this.runAddPointRecord = runAddPointRecord;
        }
    }
    
    // attributes ====================================================================
    private LocationManager locationManager;
    private LocationListener mDefaultLocationListener;
    private LocationListener mAutoRecordLocationListener;
    private LocationListener mRecordPointLocationListener;
    private MainActivity mMainActivity;
    private Settings mSettings = Settings.getInstance();
    private MapFragment mMapFragment;
    private MapView map;
    private boolean hasGPS;
    private ProviderType currentProvider;
    private LocationWorkerData data = new LocationWorkerData(false, Settings.LOCATION_MINDIST_DEFAULT, false, false);
    private boolean validGPS = true; // for external bluetooth GPS
    private boolean isPaused = false;
    private ArrayList<Coordinate> mRecordedPoints = new ArrayList<Coordinate>();
    
    /**
     * constructor
     * @param context
     * @param map
     */
    public LocationWorker(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
        map = mMainActivity.getMap();
        mMapFragment = mMainActivity.getMapFragment();
        hasGPS = hasGPSDevice();
        locationManager = 
                (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        
        mDefaultLocationListener = new DefaultLocationListener(map);
        mAutoRecordLocationListener = new AutoRecordLocationListener();
        mRecordPointLocationListener = new RecordPointLocationListener();
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
        if(mRecordedPoints.size() > 0)
        {
            mMapFragment.recordPointsAuto(mRecordedPoints);
            mRecordedPoints.clear();
        }
        
        if(!data.runAutoRecord && data.runLocation)
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
        if(!data.runAutoRecord && data.runLocation)
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
    
    /**
     * run recording point
     */
    public boolean recordPoint()
    {
        if(data.runAddPointRecord)
        {
            return false;
        }
        
        data.runAddPointRecord = true;        
        switchProvider();
        
        return true;
    }

    /**
     * stop recording point
     */
    public void stopRecordingPoint()
    {
        data.runAddPointRecord = false;
        switchProvider();
    }
    
    /**
     * @return data.runAutoRecord
     */
    public boolean isRunAutoRecord()
    {
        return data.runAutoRecord;
    }
    
    /**
     * set auto recording
     * @param runAutoRecord
     */
    public void setRunAutoRecord(boolean runAutoRecord)
    {
        data.runAutoRecord = runAutoRecord;

        switchProvider();
    }
    
    // on methods ====================================================================
    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    // private methods ===============================================================
    /**
     * check if device has GPS
     * @param context
     * @return
     */
    private boolean hasGPSDevice()
    {
        final LocationManager mgr = (LocationManager)mMainActivity.getSystemService(Context.LOCATION_SERVICE);
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
            // default
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mSettings.getLocationMinTime(),
                    data.currentMindist, mDefaultLocationListener);
            // auto record
            if(data.runAutoRecord)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mSettings.getLocationMinTime(),
                        mSettings.getAutoRecordMinDist(), mAutoRecordLocationListener);
            }
            // add point record
            if(data.runAddPointRecord)
            {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mRecordPointLocationListener, Looper.getMainLooper());
            }
            
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
                data.currentMindist, mDefaultLocationListener);
        // auto record
        if(data.runAutoRecord)
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mSettings.getLocationMinTime(),
                    mSettings.getAutoRecordMinDist(), mAutoRecordLocationListener);
        }
        // add point record
        if(data.runAddPointRecord)
        {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mRecordPointLocationListener, Looper.getMainLooper());
        }
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
        
        locationManager.removeUpdates(mDefaultLocationListener);
        locationManager.removeUpdates(mAutoRecordLocationListener);
        locationManager.removeUpdates(mRecordPointLocationListener);
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
    
    /**
     * process manual recording one point; cancel timer and change task
     */
    private void processRecordingPoint(Location location)
    {
        mMapFragment.cancelTimer();
        data.runAddPointRecord = false;
        switchProvider();
        
        Coordinate point = new Coordinate(location.getLongitude(), location.getLatitude(), location.getAltitude());
        
        mMapFragment.recordPointAdd(point); 
    }
    
    // classes =======================================================================
    
    /**
     * Location listener for default communication with Location services
     * @author jules
     *
     */
    class DefaultLocationListener implements LocationListener
    {
        private MapView map;
        private Coordinate locationPoint = new Coordinate();
        
        /**
         * constructor
         * @param map
         */
        public DefaultLocationListener(MapView map)
        {
            this.map = map;
        }
        
        /**
         * receive new location update
         */
        public synchronized void onLocationChanged(Location location)
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
            if(provider.equals(LocationManager.GPS_PROVIDER))
            {
                currentProvider = ProviderType.BOTH;
                switchProvider();
            }
        }

        public void onProviderDisabled(String provider)
        {
            
        }
    }

    /**
     * Location listener for automatic recording
     * @author jules
     *
     */
    class AutoRecordLocationListener implements LocationListener
    {

        @Override
        public synchronized void onLocationChanged(Location location)
        {
            Coordinate point = new Coordinate(location.getLongitude(), location.getLatitude(), location.getAltitude());
            if(isPaused)
            {
                mRecordedPoints.add(point);
            }
            else
            {
                mMapFragment.recordPointAuto(point);
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
        
    }
    
    /**
     * Location listener for automatic recording
     * @author jules
     *
     */
    class RecordPointLocationListener implements LocationListener
    {

        @Override
        public synchronized void onLocationChanged(Location location)
        {
            processRecordingPoint(location);           
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