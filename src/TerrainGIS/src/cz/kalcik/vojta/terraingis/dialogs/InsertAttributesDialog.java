package cz.kalcik.vojta.terraingis.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.DialogInterface;

import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;

/**
 * @author jules
 *
 */
public class InsertAttributesDialog extends SetAttributesDialog
{
    public enum InsertObjectType{RECORDING, EDITING};
    // attributes =======================================================================================
    private InsertObjectType mInsertObjectType;
    
    // public methods ===================================================================================
    /**
     * @param insertObjectType the mInsertObjectType to set
     */
    public void setInsertObjectType(InsertObjectType insertObjectType)
    {
        this.mInsertObjectType = insertObjectType;
    }
    // protected methods ================================================================================    
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {

        if(column.name.equals(DATETIME_NAME) && column.type == DATETIME_TYPE)
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mmZ", Locale.UK);
            return df.format(new Date());
        }
        
        return null;
    }

    @Override
    protected void execute()
    {
        AttributeRecord attributes = new AttributeRecord(mLayer.getAttributeHeader(), getValues());
        if(mInsertObjectType == InsertObjectType.RECORDING)
        {
            mLayer.insertRecordedObject(attributes);
        }
        else if(mInsertObjectType == InsertObjectType.EDITING)
        {
            mLayer.insertEditedObject(attributes);
        }
    }
    
    @Override
    protected void emptyExecute()
    {
        execute();
    }
    
    
}
