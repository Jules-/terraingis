package cz.kalcik.vojta.terraingis.dialogs;

import java.util.ArrayList;

import jsqlite.Exception;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.SpatialiteSRS;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FindSRSDialog extends DialogFragment
{
    // constants =====================================================================
    private static final String DEFAULT_SEARCH = "WGS 84";
    
    // attributes ====================================================================
    MainActivity mMainActivity;
    EditText mNameSRS;
    SpatiaLiteIO mSpatiaLite;
    ListView mSRSListView;
    ShapefileDialog mParentDialog;
    
    // public method ==================================================================
    /**
     * set parent dialog for result
     * @param parentDialog
     */
    public void setParentDialog(ShapefileDialog parentDialog)
    {
        mParentDialog = parentDialog;
    }
    
    // on methods =====================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        mMainActivity = (MainActivity)getActivity();
        mSpatiaLite = LayerManager.getInstance().getSpatialiteIO();
        
        Builder dialogBuilder = new AlertDialog.Builder(mMainActivity);
        
        dialogBuilder.setTitle(R.string.find_srid_message);
         
        View dialogView = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_find_srid, null);
        Button findButton = (Button)dialogView.findViewById(R.id.button_find);
        findButton.setOnClickListener(findSridHandler);
         
        mNameSRS = (EditText)dialogView.findViewById(R.id.editText_srs_name);
        mSRSListView = (ListView)dialogView.findViewById(R.id.listView_srs);
        
        findSRS(DEFAULT_SEARCH);
         
        dialogBuilder.setView(dialogView);
         
        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            dismiss();
        }
    }
    // private methods ================================================================================
    
    private void findSRS(String text)
    {
        ArrayList<SpatialiteSRS> foundSRS;
        try
        {
            foundSRS = mSpatiaLite.findSRSByName(text);
            ArrayAdapter<SpatialiteSRS> adapter = new ArrayAdapter<SpatialiteSRS>(mMainActivity,
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    foundSRS.toArray(new SpatialiteSRS[foundSRS.size()]));
            mSRSListView.setAdapter(adapter);
            mSRSListView.setOnItemClickListener(clickSRSHandler);
        }
        catch (Exception e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }        
    }
    
    // handlers =======================================================================================
    
    /**
     * get SRS by name
     */
    View.OnClickListener findSridHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            findSRS(mNameSRS.getText().toString());
        }        
    };
    
    /**
     * click on SRS
     */
    AdapterView.OnItemClickListener clickSRSHandler = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            int srid = ((SpatialiteSRS)parent.getItemAtPosition(position)).srid;
            mParentDialog.setSridFromFind(srid);
            getDialog().dismiss();
        }
    };
}
