package cz.kalcik.vojta.terraingis.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jsqlite.Exception;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;

/**
 * @author jules
 *
 */
public class InsertAttributesDialog extends SetAttributesDialog
{
    // constants ========================================================================================
    public enum InsertObjectType{RECORDING, EDITING};
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.InsertAttributesDialogSaveState";
    
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
    
    // on methods =======================================================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            setInsertObjectType((InsertObjectType)savedInstanceState.getSerializable(TAG_SAVESTATE));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(TAG_SAVESTATE, mInsertObjectType);
        
        super.onSaveInstanceState(outState);
    }
    
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
            if(mInsertObjectType == InsertObjectType.RECORDING)
            {
                mLayer.insertRecordedObject(attributes);
                
                mainActivity.getMapFragment().setMapTools();
            }
            else if(mInsertObjectType == InsertObjectType.EDITING)
            {
                mLayer.insertEditedObject(attributes);
            }
            
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
