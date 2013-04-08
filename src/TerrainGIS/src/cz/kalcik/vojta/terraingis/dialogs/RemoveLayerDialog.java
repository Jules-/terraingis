/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import android.widget.Toast;
import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;

/**
 * @author jules
 * Dialog for removing layer
 */
public class RemoveLayerDialog extends SimpleDialog
{
    /**
     * remove layer
     */
    @Override
    protected void execute()
    {
        MainActivity mainActivity = (MainActivity)getActivity();
        MapFragment mapFragment = mainActivity.getMapFragment();
        
        LayersFragment layersFragment = mainActivity.getLayersFragment();
        VectorLayer selectedVectorLayer = (VectorLayer)layersFragment.getSelectedLayer();
        
        try
        {
            selectedVectorLayer.remove();
            LayerManager.getInstance().loadSpatialite(mapFragment);
            
            layersFragment.deselect();
            
            layersFragment.invalidateListView();
            mapFragment.getMap().invalidate();
        }
        catch (Exception e)
        {
            Toast.makeText(mainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
    }

}
