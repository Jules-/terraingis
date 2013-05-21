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

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * @author jules
 *
 */
public class AttributeColumnLayout extends LinearLayout
{
    public AttributeColumnLayout(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    /**
     * @return name of attribute
     */
    public String getName()
    {
        EditText editText = (EditText)findViewById(R.id.edit_text_name_column);
        return editText.getText().toString();
    }
    
    /**
     * @return selected type of attribute
     */
    public AttributeType getType()
    {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_attribute_type);
        return (AttributeType) spinner.getSelectedItem();
    }
    
    /**
     * @return true if user can change name
     */
    public boolean canChange()
    {
        return ((EditText)findViewById(R.id.edit_text_name_column)).isEnabled();
    }
}
