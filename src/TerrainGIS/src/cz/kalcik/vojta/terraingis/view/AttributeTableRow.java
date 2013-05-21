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
package cz.kalcik.vojta.terraingis.view;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.AttributesFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author jules
 * class for row in attribute table
 */
public class AttributeTableRow extends TableRow
{
    // attributes ====================================================================
    private AttributesFragment mAttributesFragment;
    
    private String mRowid;
    
    // public methods ================================================================    
    public AttributeTableRow(Context context)
    {
        super(context);
        
        mAttributesFragment = ((MainActivity)getContext()).getAttributesFragment();
    }
    
    /**
     * create cells of row
     * @param row
     * @param inflater
     * @param values
     * @param count
     */
    public void createCells(LayoutInflater inflater, String[] values, int count)
    {
        for(int i = 0; i< count; i++)
        {
            TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell, null);
            cell.setText(values[i]);
            addView(cell);
        }        
    }
    
    /**
     * reload all cells
     * @param inflater
     * @param values
     */
    public void reloadCells(LayoutInflater inflater, String[] values)
    {
        removeAllViews();
        createCells(inflater, values, values.length);
    }
    
    /**
     * @return values of attributes
     */
    public String[] getValues()
    {
        int count = getChildCount();
        String[] values = new String[count];
        for(int i=0; i<count; i++)
        {
            TextView text = (TextView) getChildAt(i);
            values[i] = text.getText().toString();
        }
        
        return values;
    } 
    // getter setter =================================================================

    /**
     * @return the mRowid
     */
    public String getRowid()
    {
        return mRowid;
    }

    /**
     * @param data.mRowid the mRowid to set
     */
    public void setRowid(String rowid)
    {
        this.mRowid = rowid;
    }    
    
    // on methods ====================================================================
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            mAttributesFragment.setTouchedRow(this);
        }
        
        return false;
    }   
    // classes =======================================================================
}
