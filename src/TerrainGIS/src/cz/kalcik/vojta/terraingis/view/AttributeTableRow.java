/**
 * 
 */
package cz.kalcik.vojta.terraingis.view;

import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import cz.kalcik.vojta.terraingis.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author jules
 * class for row in attribute table
 */
public class AttributeTableRow extends TableRow
{
    // attributes ====================================================================
    private AttributeTableActivity mActivity;
    
    private String mRowid;
    
    // public methods ================================================================    
    public AttributeTableRow(Context context)
    {
        super(context);
        
        mActivity = (AttributeTableActivity)getContext();
    }
    
    /**
     * @return values of attributes
     */
    public String[] getValues()
    {
        int count = getChildCount();
        String[] result = new String[count];
        for(int i=0; i<count; i++)
        {
            TextView text = (TextView) getChildAt(i);
            result[i] = text.getText().toString();
        }
        
        return result;
    }
    
    /**
     * create cells of row
     * @param row
     * @param inflater
     * @param values
     * @param count
     */
    public void createCells(LayoutInflater inflater, String[] values, int count)
    {
        for(int i = 0; i< count; i++)
        {
            TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell, null);
            cell.setText(values[i]);
            addView(cell);
        }        
    }
    
    /**
     * reload all cells
     * @param inflater
     * @param values
     */
    public void reloadCells(LayoutInflater inflater, String[] values)
    {
        removeAllViews();
        createCells(inflater, values, values.length);
    }
    // getter setter =================================================================

    /**
     * @return the mRowid
     */
    public String getRowid()
    {
        return mRowid;
    }

    /**
     * @param mRowid the mRowid to set
     */
    public void setRowid(String rowid)
    {
        this.mRowid = rowid;
    }    
    
    // on methods ====================================================================
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            mActivity.setTouchedRow(this);
        }
        
        return false;
    }
    // classes =======================================================================
}
