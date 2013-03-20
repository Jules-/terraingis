package cz.kalcik.vojta.terraingis.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    // public methods ================================================================================
    public InsertAttributesDialog()
    {
        super();
        
        positiveHandler = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                AttributeHeader attributeHeader = mLayer.getAttributeHeader();
                String[] values = new String[attributeHeader.getCountColumns()];
                
                int indexAllColumns = 0;
                int indexNoPKColumns = 0;
                
                for(Column column: attributeHeader.getColumns())
                {
                    if(column.isPK)
                    {
                        values[indexAllColumns] = null;
                    }
                    else
                    {
                        values[indexAllColumns] = mAttributes.get(indexNoPKColumns).getValue();
                        indexNoPKColumns++;
                    }
                    indexAllColumns++;
                }
                
                mLayer.endObject(new AttributeRecord(attributeHeader, values));
            }        
        };
    }
    
    // protected methods ================================================================================    
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {

        if(column.name.equals(DATETIME_NAME) && column.type == DATETIME_TYPE)
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX");
            return df.format(new Date());
        }
        
        return null;
    }
}
