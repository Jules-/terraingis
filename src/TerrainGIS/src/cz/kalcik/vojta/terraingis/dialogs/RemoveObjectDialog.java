package cz.kalcik.vojta.terraingis.dialogs;

import android.widget.Toast;
import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class RemoveObjectDialog extends SimpleDialog
{
    String mRowid;
    String mLayerName;
    
    // getter, setter =========================================================
    
    /**
     * @param rowid the mRowid to set
     */
    public void setRowid(String rowid)
    {
        this.mRowid = rowid;
    }
        
    /**
     * @param layerName the mLayerName to set
     */
    public void setLayerName(String layerName)
    {
        this.mLayerName = layerName;
    }

    // protected method =======================================================   
    @Override
    protected void execute()
    {
        AttributeTableActivity activity = (AttributeTableActivity)getActivity();
        SpatiaLiteIO spatialite = activity.getSpatialite();
        
        try
        {
            spatialite.removeObject(mLayerName, Integer.parseInt(mRowid));
            activity.removeSelectedRow();
        }
        catch (Exception e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
    }
}
