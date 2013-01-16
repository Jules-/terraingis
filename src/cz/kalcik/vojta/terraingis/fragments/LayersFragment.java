package cz.kalcik.vojta.terraingis.fragments;

import java.util.ArrayList;
import java.util.Collections;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.R.id;
import cz.kalcik.vojta.terraingis.R.layout;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

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
        Collections.reverse(layers);
        layersArray = layers.toArray(new AbstractLayer[layers.size()]);
        
        setListAdapter(new ArrayAdapter<AbstractLayer>(getActivity(), R.layout.list_item_radio, R.id.text, layersArray));
    }
    
    // on methods =========================================================
    
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//    }
}