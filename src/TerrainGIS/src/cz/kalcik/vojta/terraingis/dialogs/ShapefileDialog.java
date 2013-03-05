package cz.kalcik.vojta.terraingis.dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

public class ShapefileDialog extends CreateLayerDialog
{
    // constants =====================================================================================
    private final Set<String> SUFFIXS = new TreeSet<String>(Arrays.asList(".shp", ".shx", ".dbf")); 
    // attributes ====================================================================================
    private File mFile;
    private String mNameNoSuffix;
    EditText mNameEditText;
    
    // public methods ================================================================================
    
    
    // getter setter =================================================================================
    /**
     * set filename for import
     * @param file
     * @return
     */
    public boolean setFile(File file)
    {
        mFile = file;
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        
        if(dotIndex == -1)
        {
            return false;
        }
        
        String suffix = name.substring(dotIndex);
        if(!SUFFIXS.contains(suffix))
        {
            return false;
        }
        
        mNameNoSuffix = name.substring(0, dotIndex);
        
        return true;
    }
    
    // on methods ====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(mFile.getName());
         
         LayoutInflater inflater = getActivity().getLayoutInflater();
         dialogBuilder.setView(inflater.inflate(R.layout.shapefile_dialog, null));
         mNameEditText = (EditText)getDialog().findViewById(R.id.edit_text_name_shapefile);
         mNameEditText.setText(mNameNoSuffix);
         
         dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
         dialogBuilder.setNegativeButton(R.string.negative_button, negativeHandler);
         
         return dialogBuilder.create();
    }
    
    // private methods ================================================================================
    
    // handlers =======================================================================================
 
    /**
     * positive button
     */
    DialogInterface.OnClickListener positiveHandler = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            String name = mNameEditText.getText().toString();
            checkName(name);
//            ShapeFile.getInstance().load(getActivity(), mFile);
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
