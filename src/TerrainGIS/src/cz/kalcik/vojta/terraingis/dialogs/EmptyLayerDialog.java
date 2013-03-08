/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import java.util.ArrayList;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.layer.AttributeTable.AttributeType;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
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
    // attributes =====================================================================================
    MainActivity mMainActivity;
    LinearLayout mLayout;
    LayoutInflater mInflater;
    ArrayList<LinearLayout> mAttributes = new ArrayList<LinearLayout>();
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
         addAttribute();
         dialogBuilder.setView(view);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, negativeHandler);
         
         return dialogBuilder.create();
    }
    
    // private methods ================================================================================
    /**
     * add attribute to form
     */
    private void addAttribute()
    {
        LinearLayout item = (LinearLayout)mInflater.inflate(R.layout.attribute_column, null);
        // set spinner
        Spinner spinner = (Spinner)item.findViewById(R.id.spinner_attribute_type);
        ArrayAdapter<AttributeType> adapter = new ArrayAdapter<AttributeType>(mMainActivity,
                android.R.layout.simple_spinner_dropdown_item, AttributeType.values());
        spinner.setAdapter(adapter);
        
        mLayout.addView(item);
        
        mAttributes.add(item);
    }
    
    
    // handlers =======================================================================================
 
    /**
     * positive button
     */
    DialogInterface.OnClickListener positiveHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            // name
            EditText nameEditText = (EditText)getDialog().findViewById(R.id.edit_text_name_empty);
            String name = nameEditText.getText().toString();
            try
            {
                checkName(name);
            }
            catch(RuntimeException e)
            {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            // type
            Spinner spinnerLayerType = (Spinner)getDialog().findViewById(R.id.spinner_layer_type);
            String layerType = (String)spinnerLayerType.getSelectedItem();
                       
            SpatiaLiteManager spatialite = LayerManager.getInstance().getSpatialiteManager();
            
            spatialite.createEmptyLayer(name, SpatiaLiteManager.GEOMETRY_COLUMN_NAME,
                                        layerType, SpatiaLiteManager.EPSG_LONLAT);
            LayerManager.getInstance().loadSpatialite();
            ((MainActivity)getActivity()).getLayersFragment().invalidateListView();
        }        
    };

    
    /**
     * negative button
     */
    DialogInterface.OnClickListener negativeHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            
        }        
    };
    
    // classes ======================================================================================
}
