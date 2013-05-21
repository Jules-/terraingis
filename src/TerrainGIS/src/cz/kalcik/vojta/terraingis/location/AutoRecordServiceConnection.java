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

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.location.AutoRecordService.AutoRecordBinder;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author jules
 *
 */
public class AutoRecordServiceConnection implements ServiceConnection
{
    private MapFragment mMapFragment;
    private AutoRecordService serviceReference = null;
    
    // public methods =================================================================
    /**
     * constructor
     * @param mapFragment
     */
    public AutoRecordServiceConnection(MapFragment mapFragment)
    {
        mMapFragment = mapFragment;
    }
    
    public void unsetMapFragment()
    {
        serviceReference.unsetMapFragment();
    }
    // on methods =====================================================================
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        serviceReference = ((AutoRecordBinder)service).getService();
        serviceReference.setMapFragment(mMapFragment);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
    }
}
