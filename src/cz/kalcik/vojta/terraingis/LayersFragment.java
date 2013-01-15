package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;

import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * list with layers
 * @author jules
 *
 */
public class LayersFragment extends ListFragment
{
    // properties =========================================================
    AbstractLayer[] layersArray;
    ArrayAdapter<AbstractLayer> arrayAdapter;

    // public methods =====================================================
    /**
     *  reload list of layers
     */
    public void reloadLayers()
    {
        ArrayList<AbstractLayer> layers = LayerManager.getInstance().getLayers();
        layersArray = layers.toArray(new AbstractLayer[layers.size()]);
        
        setListAdapter(new ArrayAdapter<AbstractLayer>(getActivity(), android.R.layout.simple_list_item_activated_1, layersArray));
    }
    
    // on methods =========================================================
    
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//    }
}