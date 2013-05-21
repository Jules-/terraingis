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
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author jules
 *
 */
public class AttributeValueLayout extends LinearLayout
{
    /**
     * @param context
     * @param attrs
     */
    public AttributeValueLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    /**
     * @return name of attribute
     */
    public String getValue()
    {
        EditText editText = (EditText)findViewById(R.id.editText_value_attribute);
        
        String result = editText.getText().toString();
        if(result.isEmpty())
        {
            result = null;
        }
        
        return result;
    }
 
    /**
     * set value of attribute
     * @param value
     */
    public void setValue(String value)
    {
        EditText editText = (EditText)findViewById(R.id.editText_value_attribute);
        
        editText.setText(value);
    }
    
    /**
     * set name of attribute
     * @param name
     */
    public void setName(String name)
    {
        TextView textView = (TextView)findViewById(R.id.textView_name);
        textView.setText(name);
    }
    
    public void setInputType(AttributeType type)
    {
        EditText editText = (EditText)findViewById(R.id.editText_value_attribute);
        
        if(type == AttributeType.TEXT)
        {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        else if(type == AttributeType.INTEGER)
        {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        else if(type == AttributeType.REAL)
        {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
    }
}
