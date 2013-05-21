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

import java.util.List;

import android.content.Context;
import android.location.LocationManager;

/**
 * @author jules
 *
 */
public class CommonLocationListener
{
    // constants ======================================================================================
    
    // attributes =====================================================================================
    public LocationManager locationManager;
    public Context context;
    
    // public methods =================================================================================
    
    public CommonLocationListener(Context context)
    {
        this.context = context;
        
        locationManager = 
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * check if device has GPS
     * @param context
     * @return
     */
    public boolean hasGPSDevice()
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
}
