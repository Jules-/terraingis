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

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.app.DialogFragment;

public abstract class CreateLayerDialog extends DialogFragment
{
    // protected methods =====================================================
    protected void checkName(String name)
    {
        checkNameEmpty(name);
        //check exist name
        LayerManager layerManager = LayerManager.getInstance();
        if(layerManager.hasLayer(name))
        {
            String errorMessage = getString(R.string.name_exist_error);
            throw new RuntimeException(String.format(errorMessage, name));
        }     
    }
    
    protected void checkNameEmpty(String name)
    {
        if(name.isEmpty())
        {
            throw new RuntimeException(getString(R.string.name_layer_error));
        }        
    }
}