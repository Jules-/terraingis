package cz.kalcik.vojta.terraingis.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
    private static int LOAD_REQUESTCODE = 0;
    
    // properties =========================================================
    private ArrayAdapter<AbstractLayer> mArrayAdapter;
    private LayersView mListView;
    private MainActivity mMainActivity;
    private LayerManager mLayerManager = LayerManager.getInstance();
    
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener()
            {
                @Override
                public void drop(int from, int to)
                {
                    if (from != to)
                    {
                        AbstractLayer item = mArrayAdapter.getItem(from);
                        mArrayAdapter.remove(item);
                        mArrayAdapter.insert(item, to);
                        mListView.moveCheckState(from, to);
                        
                        //change selection
                        int selectedItem = mListView.getMySelectedPosition();
                        
                        if(from == selectedItem)
                        {
                            mListView.setMySelectedPosition(to);
                        }
                        else if(from < selectedItem && to >= selectedItem)
                        {
                            mListView.setMySelectedPosition(selectedItem-1);
                        }
                        else if(from > selectedItem && to <= selectedItem)
                        {
                            mListView.setMySelectedPosition(selectedItem+1);
                        }
                        
                        mListView.invalidateViews();
                        mMainActivity.getMap().invalidate();
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
        mListView = (LayersView) myView.findViewById(R.id.list_layers);
        mListView.setDropListener(onDrop);
        
        ArrayList<AbstractLayer> layers = mLayerManager.getLayers();
        
        mArrayAdapter = new ArrayAdapter<AbstractLayer>(getActivity(), R.layout.list_item_handle_left, R.id.text_item, layers)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View itemView = super.getView(position, convertView, parent);
                    
                    // selected layer
                    int selectedPosition = mListView.getMySelectedPosition();
                    if (selectedPosition == position)
                    {
                        itemView.setBackgroundColor(getResources().getColor(R.color.highlight_selected_item));
                    }
                    else
                    {
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    
                    // visible layer
                    TextView textView = (TextView)itemView.findViewById(R.id.text_item);
                    if(getItem(position).isVisible())
                    {
                        textView.setTextColor(Color.BLACK);
                        textView.setTypeface(null, Typeface.NORMAL);
                    }
                    else
                    {
                        textView.setTextColor(Color.GRAY);
                        textView.setTypeface(null, Typeface.ITALIC);
                    }
                    
                    return itemView;
                }
            };
        
        mListView.setAdapter(mArrayAdapter);
        
        // buttons
        ImageButton buttonHide = (ImageButton)myView.findViewById(R.id.button_hide);
        buttonHide.setOnClickListener(hidePanelHandler);
        ImageButton buttonHideLayer = (ImageButton)myView.findViewById(R.id.button_hide_layer);
        buttonHideLayer.setOnClickListener(hideLayerHandler);
        ImageButton buttonZoomLayer = (ImageButton)myView.findViewById(R.id.button_zoom_to_layer);
        buttonZoomLayer.setOnClickListener(zoomLayerHandler);
        ImageButton buttonAdd = (ImageButton)myView.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(loadFileHandler);
        
        // main activity
        mMainActivity = (MainActivity)getActivity();
        
        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            mListView.setMySelectedPosition(savedInstanceState.getInt(SELECTED_POSITION));
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // Map view state
        outState.putInt(SELECTED_POSITION, mListView.getMySelectedPosition());
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == LOAD_REQUESTCODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                File shapefile = new File(data.getData().getPath());
                mLayerManager.addVirtualShape(shapefile);
            }            
        }
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
            int selectedPosition = mListView.getMySelectedPosition();
            if(selectedPosition < 0)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_layer, Toast.LENGTH_LONG).show();
                return;
            }
            
            AbstractLayer layer = (AbstractLayer)mListView.getItemAtPosition(selectedPosition);
            SpatiaLiteManager spatialite = mLayerManager.getSpatialiteManager();
            
            int from = layer.getSrid();
            int to = mLayerManager.getSrid();
            Envelope envelope = layer.getEnvelope();
            
            if(from != to)
            {
                envelope = spatialite.transformSRSEnvelope(envelope, from, to);
            }
            
            mMainActivity.getMap().zoomToEnvelopeM(envelope);
        }
    };

    /**
     * hide panel
     */
    View.OnClickListener hidePanelHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mMainActivity.hideLayersFragment();
        }        
    };
    
    /**
     * show/hide panel
     */
    View.OnClickListener hideLayerHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mArrayAdapter.getItem(mListView.getMySelectedPosition()).toggleVisibility();
            mMainActivity.getMap().invalidate();
            mListView.invalidateViews();
        }        
    };
    
    /**
     * open file manager for open ShapeFile
     */
    View.OnClickListener loadFileHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            startActivityForResult(intent, LOAD_REQUESTCODE);            
        }        
    };
    // classes =============================================================================
}