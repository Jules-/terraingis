/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis.fragments;

import java.io.File;

import jsqlite.Exception;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.dialogs.NewLayerDialog;
import cz.kalcik.vojta.terraingis.dialogs.RemoveLayerDialog;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialogExport;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialogImport;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.LinesLayer;
import cz.kalcik.vojta.terraingis.layer.PointsLayer;
import cz.kalcik.vojta.terraingis.layer.PolygonsLayer;
import cz.kalcik.vojta.terraingis.layer.TilesLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.view.LayersView;
import cz.kalcik.vojta.terraingis.R;

/**
 * list with layers
 * @author jules
 *
 */
public class LayersFragment extends PanelFragment
{
    // constants =========================================================
    public static String LAYER_ATTRIBUTE_TABLE = "cz.kalcik.vojta.LayerAttributeTable";
    public static int LOAD_REQUESTCODE = 0;
    
    private static String SELECTED_POSITION = "SelectedPosition";
    // properties =========================================================
    private LayersView mListView;
    private ArrayAdapter<AbstractLayer> mArrayAdapter;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private AbstractLayer mContextMenuSelectedlayer;
        
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
            catch (ParseException e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return;
            }
        }        
    }
    
    @Override
    protected void switchToMeChild()
    {
        mMainActivity.getAttributesLayout().setVisibility(View.GONE);
        mMainActivity.getLayersLayout().setVisibility(View.VISIBLE);
    }
    
    // getter, setter =====================================================
    
    /**
     * @return the mContextMenuSelectedlayer
     */
    public AbstractLayer getContextMenuSelectedlayer()
    {
        return mContextMenuSelectedlayer;
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
        
        setCommon(myView);
        
        // buttons
        ImageButton buttonHideLayer = (ImageButton)myView.findViewById(R.id.button_hide_layer);
        buttonHideLayer.setOnClickListener(hideLayerHandler);
        ImageButton buttonZoomLayer = (ImageButton)myView.findViewById(R.id.button_zoom_to_layer);
        buttonZoomLayer.setOnClickListener(zoomLayerHandler);
        ImageButton buttonAdd = (ImageButton)myView.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(addLayerHandler);
        
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
        int id = item.getItemId();
        if(id == R.id.menuitem_export_shapefile)
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
                Toast.makeText(mMainActivity, R.string.export_shapefile_error, Toast.LENGTH_LONG).show();
                return false;
            }
                
        }
        else if(id == R.id.menuitem_remove_layer)
        {
            // check selectedLayer
            if (mContextMenuSelectedlayer instanceof TilesLayer)
            {
                Toast.makeText(getActivity(), R.string.tileslayer_remove_error, Toast.LENGTH_LONG).show();
                return false;
            }
            
            RemoveLayerDialog dialog = new RemoveLayerDialog();
            String text = getString(R.string.confirm_remove_message);
            dialog.setMessage(String.format(text, mContextMenuSelectedlayer.toString()));
            
            mMainActivity.showDialog(dialog);
            
            return true;
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
                
                AbstractLayer item = getItem(position);
                View colorArea = itemView.findViewById(R.id.layer_color);
                // color
                if(item instanceof VectorLayer)
                {
                    colorArea.setBackgroundColor(((VectorLayer) item).getColor());
                }
                else
                {
                    colorArea.setBackgroundColor(Color.TRANSPARENT);
                }
                
                ImageView image = (ImageView) itemView.findViewById(R.id.drag_handle);
                // image handler
                if(item instanceof TilesLayer)
                {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.drag_handle_raster));
                }
                else if(item instanceof PointsLayer)
                {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.drag_handle_points));
                }
                else if(item instanceof LinesLayer)
                {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.drag_handle_lines));
                }
                else if(item instanceof PolygonsLayer)
                {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.drag_handle_polygon));
                }
                
                
                // visible layer
                TextView textView = (TextView)itemView.findViewById(R.id.text_item);
                if(item.isVisible())
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
            
            // empty layer
            if(selectedLayer instanceof VectorLayer &&
                    ((VectorLayer)selectedLayer).getCountObjects() == 0)
            {
                String message = getString(R.string.empty_layer_error);
                Toast.makeText(mMainActivity, String.format(message, selectedLayer.toString()),
                        Toast.LENGTH_LONG).show();
                return;
            }
                      
            try
            {
                Envelope envelope = selectedLayer.getEnvelope(mLayerManager.getSrid());
                mMainActivity.getMapFragment().getMap().zoomToEnvelopeM(envelope);
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
            }
            catch (ParseException e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
            }
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
            AbstractLayer selectedLayer = getSelectedLayer();
            if(selectedLayer == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_layer, Toast.LENGTH_LONG).show();
                return;
            }
            
            selectedLayer.toggleVisibility();
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