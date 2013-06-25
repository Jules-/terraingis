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
package cz.kalcik.vojta.terraingis.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import jsqlite.Exception;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.view.AttributeColumnLayout;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author jules
 *
 */
public class EmptyLayerDialog extends CreateLayerDialog
{
    // constants ======================================================================================
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.EmptyLayerDialogSaveState";
    
    // attributes =====================================================================================
    public static class EmptyLayerDialogData implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public String name;
        public String layerType;
        public ArrayList<SavedItem> items;

        public EmptyLayerDialogData(){}
    }
    
    private MainActivity mMainActivity;
    private LinearLayout mLayout;
    private LayoutInflater mInflater;
    private ArrayList<AttributeColumnLayout> mAttributes = new ArrayList<AttributeColumnLayout>();
    private EmptyLayerDialogData data = new EmptyLayerDialogData();
    private EditText mNameEditText;
    private Spinner mSpinnerLayerType;

    // on methods =====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(R.string.empty_layer_message);
         
         mMainActivity = (MainActivity)getActivity();
         mInflater = mMainActivity.getLayoutInflater();
         
         View view = mInflater.inflate(R.layout.dialog_empty_layer, null);
         mLayout = (LinearLayout)view.findViewById(R.id.empty_layer_dialog_layout);

         //form items
         mNameEditText = (EditText)view.findViewById(R.id.edit_text_name_empty);
         mSpinnerLayerType = (Spinner)view.findViewById(R.id.spinner_layer_type);
         
         ImageButton addButton = (ImageButton)view.findViewById(R.id.button_add);
         addButton.setOnClickListener(addAttributeHandler);
         dialogBuilder.setView(view);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, null);
         dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
         AlertDialog dialog = dialogBuilder.create();
         dialog.setOnShowListener(onShowListener);
         
         return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            data = (EmptyLayerDialogData) savedInstanceState.getSerializable(TAG_SAVESTATE);
            mNameEditText.setText(data.name);
            
            String[] types = getResources().getStringArray(R.array.layer_type);
            int count = types.length;
            for(int i=0; i < count; i++)
            {
                if(types[i].equals(data.layerType))
                {
                    mSpinnerLayerType.setSelection(i);
                    break;
                }
            }
            
            for(SavedItem item: data.items)
            {
                addAttribute(item.name, item.type, item.canChange);
            }
            
            setBackgroundColors();  
        }
        else
        {
            addAttribute(AttributeHeader.DATETIME_COLUMN, AttributeHeader.DATETIME_TYPE, false);
            setBackgroundColors();            
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        data.name = mNameEditText.getText().toString();
        data.layerType = (String)mSpinnerLayerType.getSelectedItem();
        data.items = new ArrayList<EmptyLayerDialog.SavedItem>();
        for(AttributeColumnLayout layout: mAttributes)
        {
            data.items.add(new SavedItem(layout.getName(), layout.getType(), layout.canChange()));
        }
        
        outState.putSerializable(TAG_SAVESTATE, data);
        
        super.onSaveInstanceState(outState);
    }  
    // private methods ================================================================================
    /**
     * add attribute to form
     */
    private void addAttribute()
    {
        addAttribute(null, null, true);
    }
    
    private void addAttribute(String name, AttributeType type, boolean canChange)
    {
        AttributeColumnLayout item = (AttributeColumnLayout)mInflater.inflate(R.layout.attribute_column, null);
        // set name
        EditText editText = (EditText)item.findViewById(R.id.edit_text_name_column);
        if(name != null)
        {
            editText.setText(name);
        }
        if(!canChange)
        {
            editText.setEnabled(false);
        }
        // set spinner
        Spinner spinner = (Spinner)item.findViewById(R.id.spinner_attribute_type);
        ArrayAdapter<AttributeType> adapter = new ArrayAdapter<AttributeType>(mMainActivity,
                android.R.layout.simple_spinner_dropdown_item, AttributeType.values());
        spinner.setAdapter(adapter);
        if(type != null)
        {
            spinner.setSelection(adapter.getPosition(type));
        }
        if(!canChange)
        {
            spinner.setEnabled(false);
        }
        // button
        ImageButton button = (ImageButton)item.findViewById(R.id.button_delete);
        button.setOnClickListener(removeAttributeHandler);
        
        mLayout.addView(item);
        
        mAttributes.add(item);        
    }
    
    /**
     * set background colors of attributes
     */
    private void setBackgroundColors()
    {
        ListBackgroundColors colors = new ListBackgroundColors(mMainActivity); 
        
        for(AttributeColumnLayout layout : mAttributes)
        {
            layout.setBackgroundColor(colors.getNextColor());
        }
    }
    
    /**
     * check attributes
     */
    private void checkAttributes()
    {
        TreeSet<String> names = new TreeSet<String>();
        
        for(AttributeColumnLayout layout : mAttributes)
        {
            String name = layout.getName();
            
            if(name.isEmpty())
            {
                throw new RuntimeException(getString(R.string.name_attribute_error));
            }
            else if(name.equals(SpatiaLiteIO.ID_COLUMN_NAME))
            {
                throw new RuntimeException(String.format(getString(R.string.concrete_name_attribute_error), SpatiaLiteIO.ID_COLUMN_NAME));
            }
            
            if(names.contains(name))
            {
                throw new RuntimeException(getString(R.string.name_attribute_same_error));
            }
            
            names.add(name);
        }        
    }
    
    /**
     * create attribute table for new layer
     * @return
     */
    private AttributeHeader createAttributeTable()
    {
        AttributeHeader result = new AttributeHeader();

        result.addColumn(SpatiaLiteIO.ID_COLUMN_NAME,
                SpatiaLiteIO.ID_COLUMN_TYPE, true);
        
        for(AttributeColumnLayout layout : mAttributes)
        {
            result.addColumn(layout.getName(), layout.getType(), false);
        }
        
        return result;
    }
    // handlers =======================================================================================
    DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            Button okButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
            okButton.setOnClickListener(positiveHandler);
        }
    };
    
    
    /**
     * positive button
     */
    View.OnClickListener positiveHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // name
            String name = mNameEditText.getText().toString();
            try
            {
                checkName(name);
                checkAttributes();
            }
            catch(RuntimeException e)
            {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            // type
            String layerType = (String)mSpinnerLayerType.getSelectedItem();
                       
            SpatiaLiteIO spatialite = LayerManager.getInstance().getSpatialiteIO();
            
            try
            {
                spatialite.createEmptyLayer(name, layerType, 
                        createAttributeTable().createSQLColumns(), SpatiaLiteIO.EPSG_LONLAT);
                LayerManager.getInstance().loadSpatialite(mMainActivity.getMapFragment());
                mMainActivity.getLayersFragment().invalidateListView();
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
            }
            
            getDialog().dismiss();
        }        
    };

    /**
     * add attribute
     */
    View.OnClickListener addAttributeHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            addAttribute();
            setBackgroundColors();
        }        
    };
    
    /**
     * add attribute
     */
    View.OnClickListener removeAttributeHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            LinearLayout parent = (LinearLayout)v.getParent();
            int position = mAttributes.indexOf(parent);
            mLayout.removeView(parent);
            mAttributes.remove(position);
            
            setBackgroundColors();
        }        
    };
    
    // classes ======================================================================================
    private class SavedItem
    {
        String name;
        AttributeType type;
        boolean canChange;
        
        public SavedItem(String name, AttributeType type, boolean canChange)
        {
            this.name = name;
            this.type = type;
            this.canChange = canChange;
        }
    }
}
