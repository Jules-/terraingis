/**
 * 
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
}
