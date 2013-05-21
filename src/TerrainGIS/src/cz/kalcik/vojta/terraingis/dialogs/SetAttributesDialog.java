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

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.view.AttributeValueLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * @author jules
 *
 */
public abstract class SetAttributesDialog extends DialogFragment
{
    // constants =====================================================================================
    protected final String DATETIME_NAME = "datetime";
    protected final AttributeType DATETIME_TYPE = AttributeType.TEXT;
    
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.SetAttributesDialogSaveState";
    // attributes ====================================================================================
    private static class SetAttributesDialogSaveData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public String layerName;
        public String[] values = null;
    }
    
    private Activity mActivity;
    private LinearLayout mMainLayout;
    protected ArrayList<AttributeValueLayout> mAttributes = new ArrayList<AttributeValueLayout>();
    protected VectorLayer mLayer;
    protected SetAttributesDialogSaveData data = new SetAttributesDialogSaveData();

    // public methods ================================================================================
    
    // on methods ====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         mActivity = (Activity)getActivity();
        
         Builder dialogBuilder = new AlertDialog.Builder(mActivity);
         
         dialogBuilder.setTitle(R.string.set_attributes_message);
         
         ScrollView scrollView = new ScrollView(mActivity);
         
         mMainLayout = new LinearLayout(mActivity);
         mMainLayout.setOrientation(LinearLayout.VERTICAL);
         scrollView.addView(mMainLayout);
         
         dialogBuilder.setView(scrollView);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
         return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            data = (SetAttributesDialogSaveData)savedInstanceState.getSerializable(TAG_SAVESTATE);
            mLayer = LayerManager.getInstance().getLayerByName(data.layerName);
        }
        
        checkCountAttributes();
        loadAttributes();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        data.layerName = mLayer.getData().name;
        data.values = getValues();
        outState.putSerializable(TAG_SAVESTATE, data);
        
        super.onSaveInstanceState(outState);
    }    
    // getter setter ==================================================================================
    /**
     * @param mLayer the mLayer to set
     */
    public void setLayer(VectorLayer layer)
    {
        this.mLayer = layer;
    }
    
    // protected methods ==============================================================================
    /**
     * @return values of dialog
     */
    protected String[] getValues()
    {
        AttributeHeader attributeHeader = mLayer.getAttributeHeader();
        String[] values = new String[attributeHeader.getCountColumns()];
        
        int indexAllColumns = 0;
        int indexNoPKColumns = 0;
        
        for(Column column: attributeHeader.getColumns())
        {
            if(column.isPK)
            {
                values[indexAllColumns] = null;
            }
            else
            {
                values[indexAllColumns] = mAttributes.get(indexNoPKColumns).getValue();
                indexNoPKColumns++;
            }
            indexAllColumns++;
        }
        
        return values;
    }    
    
    // private methods ================================================================================
    /**
     * attributes of selected layer
     */
    private void loadAttributes()
    {
        AttributeHeader attributeHeader = mLayer.getAttributeHeader();
        LayoutInflater inflater = mActivity.getLayoutInflater();
        
        ArrayList<Column> columns = attributeHeader.getColumns();
        int size = columns.size();
        for(int i=0; i < size; i++)
        {
            Column column = columns.get(i);
            
            if(!column.isPK)
            {
                AttributeValueLayout item = (AttributeValueLayout)inflater.inflate(R.layout.set_attribute_value_item, null);
                mAttributes.add(item);
                item.setName(column.name);
                item.setInputType(column.type);
                
                String value;
                if(data.values != null)
                {
                    value = data.values[i];
                }
                else
                {
                    value = getValueOfAttribute(column, i);
                }
                
                if(value != null)
                {
                    item.setValue(value);
                }
                
                mMainLayout.addView(item);
            }
        }
    }
    
    /**
     * if layer doesn't attributes hide dialog
     */
    private void checkCountAttributes()
    {
        AttributeHeader attributeHeader = mLayer.getAttributeHeader();
        if(attributeHeader.getCountColumnsWithoutPK() == 0)
        {
            emptyExecute();
            dismiss();
        }
    }
    // handlers =======================================================================================
    
    protected DialogInterface.OnClickListener positiveHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            execute();
        }        
    };
    
    // abstract protected methods =====================================================================
    /**
     * @param column
     * @param i
     * @return value of attribute by init loading
     */
    protected abstract String getValueOfAttribute(Column column, int i);
    
    /**
     * run action with inserted attributes
     */
    protected abstract void emptyExecute();
    protected abstract void execute();
    
    // classes =======================================================================================

}
