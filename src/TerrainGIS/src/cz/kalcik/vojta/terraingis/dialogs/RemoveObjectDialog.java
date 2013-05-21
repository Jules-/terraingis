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

import com.vividsolutions.jts.io.ParseException;

import android.os.Bundle;
import android.widget.Toast;
import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.AttributesFragment;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;

public class RemoveObjectDialog extends SimpleDialog
{
    // constatnts =============================================================
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.RemoveObjectDialogSaveState";
    
    // attributes =============================================================
    private static class RemoveObjectDialogData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public String mRowid;
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
        MainActivity activity = (MainActivity)getActivity();
        AttributesFragment fragment = activity.getAttributesFragment();
        
        try
        {
            VectorLayer layer = activity.getLayersFragment().getSelectedLayerIfVector();
            if(layer != null)
            {
                layer.removeSelectionOfObject();
            }
            
            layer.removeObject(data.mRowid);
            fragment.removeSelectedRow();
            
            activity.getMapFragment().getMap().invalidate();
        }
        catch (Exception e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
        catch (ParseException e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
    }
}
