/**
 * 
 */
package cz.kalcik.vojta.terraingis.view;

import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import android.content.Context;
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
