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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jsqlite.Exception;

import android.widget.Toast;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;

/**
 * @author jules
 *
 */
public class InsertAttributesDialog extends SetAttributesDialog
{
    // constants ========================================================================================
    
    // attributes =======================================================================================
    
    // public methods ===================================================================================
    
    // on methods =======================================================================================
    
    // protected methods ================================================================================    
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {

        if(column.name.equals(DATETIME_NAME) && column.type == DATETIME_TYPE)
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
            return df.format(new Date());
        }
        
        return null;
    }

    @Override
    protected void execute()
    {
        AttributeRecord attributes = new AttributeRecord(mLayer.getAttributeHeader(), getValues());
        MainActivity mainActivity = (MainActivity)getActivity();
        try
        {
            mLayer.insertEditedObject(attributes);
            mainActivity.getMapFragment().setMapTools();

            
            mainActivity.getAttributesFragment().reload();
            mainActivity.getMapFragment().getMap().invalidate();
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void emptyExecute()
    {
        execute();
    }
    
    
}
