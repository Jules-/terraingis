/**
 * 
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
