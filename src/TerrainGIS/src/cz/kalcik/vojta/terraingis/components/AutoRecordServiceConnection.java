/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.terraingis.components.AutoRecordService.AutoRecordBinder;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
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
        serviceReference.unsetMapFragment();
    }
}
