package cz.kalcik.vojta.terraingis.dialogs;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.ShapeFileIO;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ShapefileDialog extends CreateLayerDialog
{
    // constants =====================================================================================
    private final Set<String> SUFFIXS = new TreeSet<String>(Arrays.asList(".shp", ".shx", ".dbf")); 
    // attributes ====================================================================================
    private File mFile;
    private String mNameNoSuffix;
    EditText mNameEditText;
    EditText mSridEditText;
    
    // public methods ================================================================================
    
    
    // getter setter =================================================================================
    /**
     * set filename for import
     * @param file
     * @return
     */
    public void setFile(File file)
    {
        mFile = file;
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        
        if(dotIndex == -1)
        {
            throw new RuntimeException();
        }
        
        String suffix = name.substring(dotIndex);
        if(!SUFFIXS.contains(suffix))
        {
            throw new RuntimeException();
        }
        
        mNameNoSuffix = name.substring(0, dotIndex);
    }
    
    // on methods ====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
         Builder dialogBuilder = new AlertDialog.Builder(getActivity());
         
         dialogBuilder.setTitle(mFile.getName());
         
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View dialogView = inflater.inflate(R.layout.shapefile_dialog, null);
         dialogBuilder.setView(dialogView);
         mNameEditText = (EditText)dialogView.findViewById(R.id.edit_text_name_shapefile);
         mNameEditText.setText(mNameNoSuffix);
         mSridEditText = (EditText)dialogView.findViewById(R.id.edit_text_srid_shapefile);
         
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
            try
            {
                checkName(name);
                String sridString = mSridEditText.getText().toString();
                if(sridString.isEmpty())
                {
                    throw new RuntimeException(getString(R.string.srid_empty_error));
                }
                
                try
                {
                    ShapeFileIO.getInstance().load(mFile.getParent(), mNameNoSuffix, name, Integer.parseInt(sridString));
                }
                catch (Exception e)
                {
                    Log.e("TerrainGIS", e.getMessage());
                    throw new RuntimeException(getString(R.string.load_shapefile_error));
                }
                
                ((MainActivity)getActivity()).getLayersFragment().invalidateListView();
            }
            catch(RuntimeException e)
            {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
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
