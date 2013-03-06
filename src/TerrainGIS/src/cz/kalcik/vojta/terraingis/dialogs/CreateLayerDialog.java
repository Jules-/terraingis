package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.app.DialogFragment;

public abstract class CreateLayerDialog extends DialogFragment
{
    // public methods =====================================================
    public void checkName(String name)
    {
        // name
        if(name.isEmpty())
        {
            throw new RuntimeException(getString(R.string.name_error));
        }
        //check exist name
        LayerManager layerManager = LayerManager.getInstance();
        if(layerManager.hasLayer(name))
        {
            String errorMessage = getString(R.string.name_exist_error);
            throw new RuntimeException(String.format(errorMessage, name));
        }     
    }
}