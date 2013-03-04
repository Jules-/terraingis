/**
 * 
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
