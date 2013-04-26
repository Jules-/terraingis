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
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.UK);
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
