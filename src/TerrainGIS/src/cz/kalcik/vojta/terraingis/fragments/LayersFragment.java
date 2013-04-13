package cz.kalcik.vojta.terraingis.fragments;

import java.io.File;

import jsqlite.Exception;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.MainActivity.ActivityMode;
import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.dialogs.NewLayerDialog;
import cz.kalcik.vojta.terraingis.dialogs.RemoveLayerDialog;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialog;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialogExport;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialogImport;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.TilesLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
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
    public static String LAYER_ATTRIBUTE_TABLE = "cz.kalcik.vojta.LayerAttributeTable";
    public static int LOAD_REQUESTCODE = 0;
    
    private static String SELECTED_POSITION = "SelectedPosition";
    // properties =========================================================
    private LayersView mListView;
    private MainActivity mMainActivity;
    private ArrayAdapter<AbstractLayer> mArrayAdapter;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private AbstractLayer mContextMenuSelectedlayer;
    private ListBackgroundColors mBackgroundColors;
        
    // public methods =====================================================
    
    /**
     * remove selection
     */
    public void deselect()
    {
        mListView.deselect();
        
        mMainActivity.getMapFragment().setMapTools();
    }
    
    /**
     * invalidate listView
     */
    public void invalidateListView()
    {
        mListView.invalidateViews();
    }
    
    /**
     * load shapefile
     */
    public void loadShapeFile()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, LOAD_REQUESTCODE);        
    }
    
    // getter, setter =====================================================
    
    // public method ======================================================
    /**
     * when layer is not selected return null
     * @return selected layer
     */
    public AbstractLayer getSelectedLayer()
    {
        int position = mListView.getMySelectedPosition();
        
        if(position < 0 || position >= mArrayAdapter.getCount())
        {
            return null;
        }
        else
        {
            return mArrayAdapter.getItem(position);
        }
    }

    /**
     * @return selected vector layer else null
     */
    public VectorLayer getSelectedLayerIfVector()
    {

        AbstractLayer selectedLayer = getSelectedLayer();

        if(selectedLayer instanceof VectorLayer)
        {
            return (VectorLayer)selectedLayer;
        }
        
        return null;
    }
    
    /**
     * remove selected object in selected layer
     */
    public void removeSelectedObject()
    {
        AbstractLayer abstrctLayer = getSelectedLayer();
        
        if(abstrctLayer instanceof VectorLayer)
        {
            try
            {
                ((VectorLayer)abstrctLayer).removeSelectionOfObject();
                mMainActivity.getMapFragment().getMap().invalidate();
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
            }
            
        }        
    }
    
    // on methods =========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.layers_layout, container, false);
        
        // listView
        mListView = (LayersView) myView.findViewById(R.id.list_layers);
        mListView.setDropListener(onDrop);
        registerForContextMenu(mListView);
        
        setArrayAdapter();
        mListView.setAdapter(mArrayAdapter);
        
        // buttons
        ImageButton buttonHide = (ImageButton)myView.findViewById(R.id.button_hide);
        buttonHide.setOnClickListener(hidePanelHandler);
        ImageButton buttonHideLayer = (ImageButton)myView.findViewById(R.id.button_hide_layer);
        buttonHideLayer.setOnClickListener(hideLayerHandler);
        ImageButton buttonZoomLayer = (ImageButton)myView.findViewById(R.id.button_zoom_to_layer);
        buttonZoomLayer.setOnClickListener(zoomLayerHandler);
        ImageButton buttonAdd = (ImageButton)myView.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(addLayerHandler);
        ImageButton buttonRemove = (ImageButton)myView.findViewById(R.id.button_remove);
        buttonRemove.setOnClickListener(removeLayerHandler);
        
        // main activity
        mMainActivity = (MainActivity)getActivity();
        
        // background colors
        mBackgroundColors = new ListBackgroundColors(mMainActivity);
        
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

                ShapefileDialogImport dialog = new ShapefileDialogImport();
                
                try
                {
                    dialog.setFile(shapefile);
                    mMainActivity.showDialog(dialog);
                }
                catch(RuntimeException e)
                {
                    Toast.makeText(mMainActivity, R.string.bad_shapefile_error, Toast.LENGTH_LONG).show();
                }
            }            
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        mContextMenuSelectedlayer = mArrayAdapter.getItem(info.position);

        getActivity().getMenuInflater().inflate(R.menu.layers_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.menuitem_show_attribute_table)
        {
            if(mContextMenuSelectedlayer instanceof VectorLayer)
            {
                Intent intent = new Intent(mMainActivity, AttributeTableActivity.class);
                intent.putExtra(LAYER_ATTRIBUTE_TABLE, mContextMenuSelectedlayer.getData().name);
                
                this.startActivity(intent);
                
                return true;
            }
            else
            {
                Toast.makeText(mMainActivity, R.string.layer_attribute_table_error, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else if(item.getItemId() == R.id.menuitem_export_shapefile)
        {
            if(mContextMenuSelectedlayer instanceof VectorLayer)
            {
                ShapefileDialogExport dialog = new ShapefileDialogExport();
                dialog.setLayerName(mContextMenuSelectedlayer.getData().name);
                dialog.setSrid(mContextMenuSelectedlayer.getSrid());
                mMainActivity.showDialog(dialog);
                
                return true;
            }
            else
            {
                Toast.makeText(mMainActivity, R.string.layer_export_error, Toast.LENGTH_LONG).show();
                return false;
            }
                
        }
        else
        {
            return super.onContextItemSelected(item);
        }
    }

    // private methods ========================================================
    /**
     * set ArrayAdapter of layers    
     */
    private void setArrayAdapter()
    {
        mArrayAdapter =
                new ArrayAdapter<AbstractLayer>(getActivity(), R.layout.list_item_handle_left,
                        R.id.text_item, mLayerManager.getLayers())
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View itemView = super.getView(position, convertView, parent);
                
                // selected layer
                int selectedPosition = mListView.getMySelectedPosition();
                
                // background color
                if(position == 0)
                {
                    mBackgroundColors.reset();
                }
                
                int backgroundColor = mBackgroundColors.getNextColor();
                
                if (selectedPosition == position)
                {
                    itemView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                }
                else
                {
                    itemView.setBackgroundColor(backgroundColor);
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
            AbstractLayer selectedLayer = getSelectedLayer();
            if(selectedLayer == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_layer, Toast.LENGTH_LONG).show();
                return;
            }
            
            SpatiaLiteIO spatialite = mLayerManager.getSpatialiteIO();
            
            // empty layer
            if(selectedLayer instanceof VectorLayer &&
                    ((VectorLayer)selectedLayer).getCountObjects() == 0)
            {
                String message = getString(R.string.empty_layer_error);
                Toast.makeText(mMainActivity, String.format(message, selectedLayer.toString()),
                        Toast.LENGTH_LONG).show();
                return;
            }
            
            int from = selectedLayer.getSrid();
            int to = mLayerManager.getSrid();
            Envelope envelope = selectedLayer.getEnvelope();
            
            if(from != to)
            {
                try
                {
                    envelope = spatialite.transformSRSEnvelope(envelope, from, to);
                }
                catch (Exception e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                catch (ParseException e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
            
            mMainActivity.getMapFragment().getMap().zoomToEnvelopeM(envelope);
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
            mMainActivity.getMapFragment().getMap().invalidate();
            mListView.invalidateViews();
        }        
    };
    
    /**
     * open dialog for add layer
     */
    View.OnClickListener addLayerHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mMainActivity.showDialog(new NewLayerDialog());
        }        
    };
    
    /**
     * open dialog for remove layer
     */
    View.OnClickListener removeLayerHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            AbstractLayer selectedLayer = getSelectedLayer();
            if(selectedLayer == null)
            {
                Toast.makeText(getActivity(), R.string.not_selected_layer, Toast.LENGTH_LONG).show();
                return;               
            }
            // check selectedLayer
            if (selectedLayer instanceof TilesLayer)
            {
                Toast.makeText(getActivity(), R.string.tileslayer_remove_error, Toast.LENGTH_LONG).show();
                return;
            }
            
            RemoveLayerDialog dialog = new RemoveLayerDialog();
            String text = getString(R.string.confirm_remove_message);
            dialog.setMessage(String.format(text, getSelectedLayer().toString()));
            
            mMainActivity.showDialog(dialog);
        }        
    };

    /**
     * drop listener
     */
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
                        mMainActivity.getMapFragment().getMap().invalidate();
                    }
                }
            };
    
    // classes =============================================================================
}