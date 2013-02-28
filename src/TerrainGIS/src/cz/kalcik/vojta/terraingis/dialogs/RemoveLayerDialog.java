/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
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
        LayerManager layerManager = LayerManager.getInstance();
        SpatiaLiteManager spatialite = layerManager.getSpatialiteManager();
        spatialite.removeLayer(selectedVectorLayer.toString(),
                selectedVectorLayer.getGeometrColumn());
        layerManager.loadSpatialite();
        layersFragment.deselect();
        
        layersFragment.invalidateListView();
        mainActivity.getMap().invalidate();
    }

}
