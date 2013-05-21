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

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Dialog select type of new layer
 * @author jules
 *
 */
public class NewLayerDialog extends DialogFragment implements OnClickListener
{
    // constants ======================================================================
    
    // attributes =====================================================================
    private String[] ITEMS;
    
    // on methods =====================================================================
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
            ((MainActivity)getActivity()).getLayersFragment().loadShapeFile();
        }
    }
}
