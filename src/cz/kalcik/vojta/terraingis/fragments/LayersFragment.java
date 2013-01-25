package cz.kalcik.vojta.terraingis.fragments;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.view.LayersView;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.R;

/**
 * list with layers
 * @author jules
 *
 */
public class LayersFragment extends Fragment
{
    // constants =========================================================
    private static String SELECTED_POSITION = "SelectedPosition";
    
    // properties =========================================================
    private ArrayAdapter<AbstractLayer> arrayAdapter;
    private LayersView listView;
    private MainActivity mainActivity;
    private LayerManager mLayerManager = LayerManager.getInstance();
    
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
                        
                        //change selection
                        int selectedItem = listView.getMySelectedPosition();
                        
                        if(from == selectedItem)
                        {
                            listView.setMySelectedPosition(to);
                        }
                        else if(from < selectedItem && to >= selectedItem)
                        {
                            listView.setMySelectedPosition(selectedItem-1);
                        }
                        else if(from > selectedItem && to <= selectedItem)
                        {
                            listView.setMySelectedPosition(selectedItem+1);
                        }
                        
                        listView.invalidateViews();
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
        
        ArrayList<AbstractLayer> layers = mLayerManager.getLayers();
        
        arrayAdapter = new ArrayAdapter<AbstractLayer>(getActivity(), R.layout.list_item_handle_left, R.id.text, layers)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View itemView = super.getView(position, convertView, parent);
                    int selectedPosition = listView.getMySelectedPosition();
                    if (selectedPosition == position)
                        itemView.setBackgroundColor(getResources().getColor(R.color.highlight_selected_item));
                    else
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                    return itemView;
                }
            };
        
        listView.setAdapter(arrayAdapter);
        
        // buttons
        ImageButton buttonZoomLayer = (ImageButton)myView.findViewById(R.id.button_zoom_to_layer);
        buttonZoomLayer.setOnClickListener(zoomLayerHandler);
        ImageButton buttonHide = (ImageButton)myView.findViewById(R.id.button_hide);
        buttonHide.setOnClickListener(hideHandler);
        
        // main activity
        mainActivity = (MainActivity)getActivity();
        
        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            listView.setMySelectedPosition(savedInstanceState.getInt(SELECTED_POSITION));
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // Map view state
        outState.putInt(SELECTED_POSITION, listView.getMySelectedPosition());
        
        super.onSaveInstanceState(outState);
    }
    
    // handlers ===============================================================
    
    /**
     * zoom to selected layer
     */
    View.OnClickListener zoomLayerHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int selectedPosition = listView.getMySelectedPosition();
            if(selectedPosition < 0)
            {
                Toast.makeText(mainActivity, R.string.not_selected_layer, Toast.LENGTH_LONG).show();
                return;
            }
            
            AbstractLayer layer = (AbstractLayer)listView.getItemAtPosition(selectedPosition);
            SpatiaLiteManager spatialite = mLayerManager.getSpatialiteManager();
            
            int from = layer.getSrid();
            int to = mLayerManager.getSrid();
            Envelope envelope = layer.getEnvelope();
            
            if(from != to)
            {
                envelope = spatialite.transformSRSEnvelope(envelope, from, to);
            }
            
            mainActivity.getMap().zoomToEnvelopeM(envelope);
        }
    };

    /**
     * hide panel
     */
    View.OnClickListener hideHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mainActivity.hideLayersFragment();
        }        
    };
    
    // classes =============================================================================
}