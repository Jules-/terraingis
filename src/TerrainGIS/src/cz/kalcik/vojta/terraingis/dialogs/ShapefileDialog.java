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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShapefileDialog extends CreateLayerDialog
{
    // constants =====================================================================================
    private final Set<String> SUFFIXS = new TreeSet<String>(Arrays.asList(".shp", ".shx", ".dbf")); 
    // attributes ====================================================================================
    MainActivity mMainActivity;
    private File mFile;
    private String mNameNoSuffix;
    EditText mNameEditText;
    EditText mSridEditText;
    
    // public methods ================================================================================
    /**
     * set EPSG srid
     * @param srid
     */
    public void setSrid(int srid)
    {
        mSridEditText.setText(Integer.toString(srid));
    }
    
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
        mMainActivity = (MainActivity)getActivity();
        Builder dialogBuilder = new AlertDialog.Builder(mMainActivity);
         
        dialogBuilder.setTitle(mFile.getName());
         
        LayoutInflater inflater = mMainActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shapefile, null);
        dialogBuilder.setView(dialogView);
        mNameEditText = (EditText)dialogView.findViewById(R.id.edit_text_name_shapefile);
        mNameEditText.setText(mNameNoSuffix);
        mSridEditText = (EditText)dialogView.findViewById(R.id.edit_text_srid_shapefile);
        Button findButton = (Button)dialogView.findViewById(R.id.button_run_find_dialog);
        findButton.setOnClickListener(findSridHandler);
         
        dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
        dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
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
                    ShapeFileIO.getInstance().load(mFile.getParent(), mNameNoSuffix, name,
                            Integer.parseInt(sridString), mMainActivity.getMapFragment());
                }
                catch (Exception e)
                {
                    Log.e("TerrainGIS", e.getMessage());
                    throw new RuntimeException(getString(R.string.load_shapefile_error));
                }
                
                ((MainActivity)mMainActivity).getLayersFragment().invalidateListView();
            }
            catch(RuntimeException e)
            {
                Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }        
    };
    
    View.OnClickListener findSridHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            FindSRSDialog dialog = new FindSRSDialog();
            dialog.setParentDialog(ShapefileDialog.this);
            mMainActivity.showDialog(dialog);
        }        
    };
}
