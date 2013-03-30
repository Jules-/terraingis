/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
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
        LayersFragment layersFragment = mainActivity.getLayersFragment();
        VectorLayer selectedVectorLayer = (VectorLayer)layersFragment.getSelectedLayer();
        selectedVectorLayer.remove();
        
        LayerManager.getInstance().loadSpatialite();

        layersFragment.deselect();
        
        layersFragment.invalidateListView();
        mainActivity.getMapFragment().getMap().invalidate();
    }

}
