/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import java.util.List;

import cz.kalcik.vojta.terraingis.components.LocationWorker.FixReceiver;

import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

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
