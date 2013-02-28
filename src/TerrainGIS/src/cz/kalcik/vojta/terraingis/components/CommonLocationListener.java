/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import java.util.List;

import android.content.Context;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * @author jules
 *
 */
public class CommonLocationListener
{
    public boolean validGPS = true; // for external bluetooth GPS
    public Context context;
    
    public CommonLocationListener(Context context)
    {
        this.context = context;
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
    
    public void statusChanged(String provider, int status, Bundle extras)
    {
        if(provider.equals(LocationManager.GPS_PROVIDER) && status != LocationProvider.AVAILABLE)
        {
            validGPS = false;
        }
        else if(provider.equals(LocationManager.GPS_PROVIDER) && status == LocationProvider.AVAILABLE)
        {
            validGPS = true;
        }
    }
}
