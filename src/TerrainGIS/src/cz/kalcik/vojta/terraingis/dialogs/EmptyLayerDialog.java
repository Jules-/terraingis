/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author jules
 *
 */
public class EmptyLayerDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(R.string.empty_layer_message);
         
         LayoutInflater inflater = getActivity().getLayoutInflater();
         dialogBuilder.setView(inflater.inflate(R.layout.empty_layer_dialog, null));
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, negativeHandler);
         
         return dialogBuilder.create();
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
            EditText nameEditText = (EditText)getDialog().findViewById(R.id.edit_text_name);
            String name = nameEditText.getText().toString();
            if(name.isEmpty())
            {
                Toast.makeText(getActivity(), R.string.name_error, Toast.LENGTH_LONG).show();
                return;
            }
            //check exist name
            LayerManager layerManager = LayerManager.getInstance();
            if(layerManager.hasLayer(name))
            {
                String errorMessage = getString(R.string.name_exist_error);
                Toast.makeText(getActivity(), 
                        String.format(errorMessage, name), Toast.LENGTH_LONG).show();
                return;
            }            
            // type
            Spinner spinnerLayerType = (Spinner)getDialog().findViewById(R.id.spinner_layer_type);
            String layerType = (String)spinnerLayerType.getSelectedItem();
                       
            SpatiaLiteManager spatialite = LayerManager.getInstance().getSpatialiteManager();
            
            spatialite.createEmptyLayer(name, SpatiaLiteManager.GEOMETRY_COLUMN_NAME,
                                        layerType, SpatiaLiteManager.EPSG_LONLAT);
            LayerManager.getInstance().loadSpatialite();
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
}
