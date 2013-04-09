package cz.kalcik.vojta.terraingis.dialogs;

import java.io.Serializable;

import android.os.Bundle;
import android.widget.Toast;
import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class RemoveObjectDialog extends SimpleDialog
{
    // constatnts =============================================================
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.RemoveObjectDialogSaveState";
    
    // attributes =============================================================
    private class RemoveObjectDialogData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public String mRowid;
        public String mLayerName;

        public RemoveObjectDialogData()
        {
        }
    }

    RemoveObjectDialogData data = new RemoveObjectDialogData();
    
    // getter, setter =========================================================
    
    /**
     * @param rowid the mRowid to set
     */
    public void setRowid(String rowid)
    {
        this.data.mRowid = rowid;
    }
        
    /**
     * @param layerName the mLayerName to set
     */
    public void setLayerName(String layerName)
    {
        this.data.mLayerName = layerName;
    }

    // on methods =============================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        if(savedInstanceState != null)
        {
            data = (RemoveObjectDialogData)savedInstanceState.getSerializable(TAG_SAVESTATE);
        }
        
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(TAG_SAVESTATE, data);
        
        super.onSaveInstanceState(outState);
    }
    
    // protected method =======================================================   
    @Override
    protected void execute()
    {
        AttributeTableActivity activity = (AttributeTableActivity)getActivity();
        SpatiaLiteIO spatialite = activity.getSpatialite();
        
        try
        {
            spatialite.removeObject(data.mLayerName, Integer.parseInt(data.mRowid));
            activity.removeSelectedRow();
        }
        catch (Exception e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
    }
}
