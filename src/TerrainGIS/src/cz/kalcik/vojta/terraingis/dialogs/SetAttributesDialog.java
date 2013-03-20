/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.view.AttributeValueLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * @author jules
 *
 */
public abstract class SetAttributesDialog extends DialogFragment
{
    // constants =====================================================================================
    protected String DATETIME_NAME = "datetime";
    protected AttributeType DATETIME_TYPE = AttributeType.TEXT;
    // attributes ====================================================================================
    private Activity mActivity;
    private LinearLayout mMainLayout;
    protected ArrayList<AttributeValueLayout> mAttributes = new ArrayList<AttributeValueLayout>();
    protected VectorLayer mLayer;
    protected DialogInterface.OnClickListener positiveHandler;
    
    // public methods ================================================================================
    
    // on methods ====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         mActivity = (Activity)getActivity();
        
         Builder dialogBuilder = new AlertDialog.Builder(mActivity);
         
         dialogBuilder.setTitle(R.string.set_attributes_message);
         
         ScrollView scrollView = new ScrollView(mActivity);
         
         mMainLayout = new LinearLayout(mActivity);
         mMainLayout.setOrientation(LinearLayout.VERTICAL);
         scrollView.addView(mMainLayout);
         
         dialogBuilder.setView(scrollView);
         loadAttributes();
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
         return dialogBuilder.create();
    }
    
    // getter setter ==================================================================================
    /**
     * @param mLayer the mLayer to set
     */
    public void setLayer(VectorLayer layer)
    {
        this.mLayer = layer;
    }
    
    // private methods ================================================================================
    /**
     * attributes of selected layer
     */
    private void loadAttributes()
    {
        AttributeHeader attributeHeader = mLayer.getAttributeHeader();
        LayoutInflater inflater = mActivity.getLayoutInflater();
        
        ArrayList<Column> columns = attributeHeader.getColumns();
        int size = columns.size();
        for(int i=0; i < size; i++)
        {
            Column column = columns.get(i);
            
            if(!column.isPK)
            {
                AttributeValueLayout item = (AttributeValueLayout)inflater.inflate(R.layout.set_attribute_value_item, null);
                mAttributes.add(item);
                item.setName(column.name);
                item.setInputType(column.type);
                
                String value = getValueOfAttribute(column, i);
                if(value != null)
                {
                    item.setValue(value);
                }
                
                mMainLayout.addView(item);
            }
        }
    }
    
    // abstract protected methods =====================================================================
    
    protected abstract String getValueOfAttribute(Column column, int i);
}
