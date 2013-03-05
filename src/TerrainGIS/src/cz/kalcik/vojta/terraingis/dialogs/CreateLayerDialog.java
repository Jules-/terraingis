package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public abstract class CreateLayerDialog extends DialogFragment
{
    // public methods =====================================================
    public void checkName(String name)
    {
        // name
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
    }
}