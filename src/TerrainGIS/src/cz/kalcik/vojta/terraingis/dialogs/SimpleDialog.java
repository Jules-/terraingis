/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author jules
 *
 */
public abstract class SimpleDialog extends DialogFragment
{
    private String mMessage;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(mMessage);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, negativeHandler);
         
         return dialogBuilder.create();
    }
    
    // getters, setters ===============================================================================
    
    /**
     * @param mMessage the mMessage to set
     */
    public void setMessage(String mMessage)
    {
        this.mMessage = mMessage;
    }
    
    // private methods ================================================================================
    
    /**
     * do something
     */
    protected abstract void execute();
    
    // handlers =======================================================================================

    /**
     * positive button
     */
    DialogInterface.OnClickListener positiveHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            execute();
        }        
    };

    
    /**
     * negative button
     */
    DialogInterface.OnClickListener negativeHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
        }        
    };
}
