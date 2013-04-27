/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import android.widget.Toast;
import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;

/**
 * @author jules
 *
 */
public class RemoveEditedObjectDialog extends SimpleDialog
{
    @Override
    protected void execute()
    {
        MainActivity activity = (MainActivity)getActivity();
        
        VectorLayer layer = activity.getLayersFragment().getSelectedLayerIfVector();
        if(layer != null)
        {
            try
            {
                layer.removeSelectedEdited();
                
                activity.getAttributesFragment().reload();
                
                MapFragment fragment = activity.getMapFragment();
                fragment.setMapTools();
                fragment.getMap().invalidate();
            }
            catch (NumberFormatException e)
            {
                Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
