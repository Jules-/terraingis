/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import android.content.DialogInterface;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;

/**
 * @author jules
 *
 */
public class UpdateAttributesDialog extends SetAttributesDialog
{
    // attributes ====================================================================================
    private String[] mValues = null;
    
    // public methods ================================================================================
    
    public UpdateAttributesDialog()
    {
        super();
        
        positiveHandler = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                // TODO edit attributes
            }        
        };
    }
    
    // getter setter ==================================================================================
    /**
     * @param values the mValues to set
     */
    public void setValues(String[] values)
    {
        this.mValues = values;
    }

    // protected methods ==============================================================================
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {
        return mValues[i];
    }
}
