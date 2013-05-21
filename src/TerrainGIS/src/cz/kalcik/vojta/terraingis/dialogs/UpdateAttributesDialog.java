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

import jsqlite.Exception;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.Toast;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
import cz.kalcik.vojta.terraingis.view.AttributeTableRow;

/**
 * @author jules
 *
 */
public class UpdateAttributesDialog extends SetAttributesDialog
{
    // constants ========================================================================================
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.UpdateAttributesDialogSaveState";
    
    // attributes ====================================================================================
    private String[] mOriginValues = null;
    private AttributeTableRow mRow;
    
    // public methods ================================================================================
    
    // getter setter ==================================================================================    
    /**
     * @param row the mRow to set
     */
    public void setRow(AttributeTableRow row)
    {
        this.mRow = row;
        mOriginValues = row.getValues();
    }
    // on methods =======================================================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        if(savedInstanceState != null)
        {
            int index = savedInstanceState.getInt(TAG_SAVESTATE);
            AttributeTableRow row = ((MainActivity)getActivity()).getAttributesFragment().getRowAtIndex(index);
            setRow(row);
        }
        
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(TAG_SAVESTATE, ((TableLayout)mRow.getParent()).indexOfChild(mRow));
        
        super.onSaveInstanceState(outState);
    }
    
    // protected methods ==============================================================================
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {
        return mOriginValues[i];
    }

    @Override
    protected void execute()
    {
        Activity activity = getActivity();
        SpatiaLiteIO spatialite = LayerManager.getInstance().getSpatialiteIO();
        AttributeHeader attributeHeader = mLayer.getAttributeHeader();
        AttributeRecord record = new AttributeRecord(attributeHeader, getValues());
        
        int rowid = Integer.parseInt(mRow.getRowid());
        try
        {
            String name = mLayer.getData().name;
            spatialite.updateAttributes(name,
                    mLayer.getAttributeHeader().getUpdateSQLArgs(false), record.getValuesWithoutPK(),
                    rowid);
            String[] values = spatialite.getAttributes(name, attributeHeader, rowid);
            mRow.reloadCells(activity.getLayoutInflater(), values);
        }
        catch (Exception e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void emptyExecute()
    {
        // do nothing
    }
}
