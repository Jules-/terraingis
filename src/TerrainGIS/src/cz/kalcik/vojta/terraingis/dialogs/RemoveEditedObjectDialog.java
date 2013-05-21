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
