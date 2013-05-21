/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis.location;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.fragments.SettingsFragment;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * @author jules
 * service for automatic recording
 */
public class AutoRecordService extends Service implements LocationListener
{
    // attributes ==================================================================
    private CommonLocationListener mCommon;
    private final IBinder binder = new AutoRecordBinder();
    private ArrayList<Coordinate> mPoints = new ArrayList<Coordinate>();
    private MapFragment mMapFragment = null;
    private SharedPreferences mSharedPref;
    private int minAccuracy;
    
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
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
        if(location.getAccuracy() <= minAccuracy)
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
        minAccuracy = SettingsFragment.getIntShareSettings(mSharedPref, SettingsFragment.KEY_GPS_MINACCURACY);
        // default
        mCommon.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * SettingsFragment.getIntShareSettings(mSharedPref, SettingsFragment.KEY_GPS_MINTIME),
                SettingsFragment.getIntShareSettings(mSharedPref, SettingsFragment.KEY_GPS_MINDIST), this);
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
