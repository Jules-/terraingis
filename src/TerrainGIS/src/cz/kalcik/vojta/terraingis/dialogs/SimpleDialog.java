/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author jules
 *
 */
public abstract class SimpleDialog extends DialogFragment
{
    // constatnts =====================================================================================
    private final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.SimpleDialogSaveState";
    
    // attributes =====================================================================================
    private String mMessage = "";
    
    // on methods =====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
         return dialogBuilder.create();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        if(savedInstanceState != null)
        {
            mMessage = savedInstanceState.getString(TAG_SAVESTATE);
        }
        
        getDialog().setTitle(mMessage);
        
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(TAG_SAVESTATE, mMessage);
        
        super.onSaveInstanceState(outState);
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
}
