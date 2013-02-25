/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Dialog select type of new layer
 * @author jules
 *
 */
public class NewLayerDialog extends DialogFragment implements OnClickListener
{
    private String[] ITEMS;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         ITEMS = new String[2];
         ITEMS[0] = getString(R.string.empty_layer);
         ITEMS[1] = getString(R.string.import_shapefile);
         
         dialogBuilder.setItems(ITEMS, this);
         dialogBuilder.setTitle(R.string.new_layer_message);
         return dialogBuilder.create();
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        // empty_layer
        if(which == 0)
        {
            MainActivity mainActivity = (MainActivity)getActivity();
            mainActivity.showDialog(new EmptyLayerDialog());
        }
        // import shapefile
        else if(which == 1)
        {
            
        }
    }
}
