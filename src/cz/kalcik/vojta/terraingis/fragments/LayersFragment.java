package cz.kalcik.vojta.terraingis.fragments;

import java.util.ArrayList;
import java.util.Collections;

import com.mobeta.android.dslv.DragSortListView;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.view.LayersView;
import cz.kalcik.vojta.terraingis.view.MapView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * list with layers
 * @author jules
 *
 */
public class LayersFragment extends Fragment
{
    // constants =========================================================

    
    // properties =========================================================
    private ArrayAdapter<AbstractLayer> arrayAdapter;
    private LayersView listView;
    private MainActivity mainActivity;
    
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener()
            {
                @Override
                public void drop(int from, int to)
                {
                    if (from != to)
                    {
                        AbstractLayer item = arrayAdapter.getItem(from);
                        arrayAdapter.remove(item);
                        arrayAdapter.insert(item, to);
                        listView.moveCheckState(from, to);
                        
                        mainActivity.getMap().invalidate();
                    }
                }
            };

    
    // public methods =====================================================
    
    // on methods =========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.layers_layout, container, false);
        
        // listView
        listView = (LayersView) myView.findViewById(R.id.list_layers);
        listView.setDropListener(onDrop);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        ArrayList<AbstractLayer> layers = LayerManager.getInstance().getLayers();
        
        arrayAdapter = new ArrayAdapter<AbstractLayer>(getActivity(), R.layout.list_item_radio, R.id.text, layers);
        listView.setAdapter(arrayAdapter);
        
        // main activity
        mainActivity = (MainActivity)getActivity();
        
        return myView;
    }

    // classes =============================================================================
}