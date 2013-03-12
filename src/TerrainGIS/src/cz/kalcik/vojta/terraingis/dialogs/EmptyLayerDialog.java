/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import java.util.ArrayList;
import java.util.TreeSet;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    
    
    // attributes =====================================================================================
    MainActivity mMainActivity;
    LinearLayout mLayout;
    LayoutInflater mInflater;
    ArrayList<AttributeColumnLayout> mAttributes = new ArrayList<AttributeColumnLayout>();
    int attributeId = 0;
    
    // on methods =====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(R.string.empty_layer_message);
         
         mMainActivity = (MainActivity)getActivity();
         mInflater = mMainActivity.getLayoutInflater();
         
         View view = mInflater.inflate(R.layout.empty_layer_dialog, null);
         mLayout = (LinearLayout)view.findViewById(R.id.empty_layer_dialog_layout);
         addAttribute(AttributeHeader.DATETIME_COLUMN, AttributeHeader.DATETIME_TYPE, false);
         setBackgroundColors();
         ImageButton addButton = (ImageButton)view.findViewById(R.id.button_add);
         addButton.setOnClickListener(addAttributeHandler);
         dialogBuilder.setView(view);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, null);
         dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
         AlertDialog dialog = dialogBuilder.create();
         dialog.setOnShowListener(onShowListener);
         
         return dialog;
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
        int color1 = getResources().getColor(R.color.background_list1);
        int color2 = getResources().getColor(R.color.background_list2);
        boolean firstColor = true;
        
        for(AttributeColumnLayout layout : mAttributes)
        {
            int color = firstColor ? color1 : color2;

            layout.setBackgroundColor(color);
            firstColor = !firstColor;
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
                throw new RuntimeException(getString(R.string.concrete_name_attribute_error));
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
            EditText nameEditText = (EditText)getDialog().findViewById(R.id.edit_text_name_empty);
            String name = nameEditText.getText().toString();
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
            Spinner spinnerLayerType = (Spinner)getDialog().findViewById(R.id.spinner_layer_type);
            String layerType = (String)spinnerLayerType.getSelectedItem();
                       
            SpatiaLiteIO spatialite = LayerManager.getInstance().getSpatialiteManager();
            
            spatialite.createEmptyLayer(name, layerType, 
                    createAttributeTable().createSQLColumns(), SpatiaLiteIO.EPSG_LONLAT);
            LayerManager.getInstance().loadSpatialite();
            ((MainActivity)getActivity()).getLayersFragment().invalidateListView();
            
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
}
