/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis.dialogs;

import java.io.Serializable;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public abstract class ShapefileDialog extends CreateLayerDialog
{
    // constants =====================================================================================
    protected static final String MAIN_SUFFIX = ".shp";
    protected static final String PROJECTION_SUFFIX = ".qpj";
    protected static final String CHARSET_SUFFIX = ".cpg";
    
    private static final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.ShapefileDialogSaveState";
    private static final String DEFAULT_CHARSET = "UTF-8";
    
    // attributes ====================================================================================
    protected static class ShapefileDialogData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public int srid;
        public String charset;
        public String name;
        
        public ShapefileDialogData(int srid, String charset, String name)
        {
            this.srid = srid;
            this.charset = charset;
            this.name = name;
        }
    }
    
    protected MainActivity mMainActivity;
    protected EditText mNameEditText;
    protected EditText mSridEditText;
    protected EditText mCharsetEditText;
    protected ShapefileDialogData data = new ShapefileDialogData(-1, DEFAULT_CHARSET, "");
    
    // public methods ================================================================================
    /**
     * set EPSG srid
     * @param srid
     */
    public void setSridFromFind(int srid)
    {
        mSridEditText.setText(Integer.toString(srid));
    }
    
    // on methods ====================================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        mMainActivity = (MainActivity)getActivity();
        Builder dialogBuilder = new AlertDialog.Builder(mMainActivity);
        
         
        LayoutInflater inflater = mMainActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shapefile, null);
        dialogBuilder.setView(dialogView);
        mNameEditText = (EditText)dialogView.findViewById(R.id.edit_text_name_shapefile);
        mSridEditText = (EditText)dialogView.findViewById(R.id.edit_text_srid_shapefile);
        mCharsetEditText = (EditText)dialogView.findViewById(R.id.edit_text_charset_shapefile);
        Button findButton = (Button)dialogView.findViewById(R.id.button_run_find_dialog);
        findButton.setOnClickListener(findSridHandler);
        
        dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
        dialogBuilder.setNegativeButton(R.string.negative_button, null);
         
        return dialogBuilder.create();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            data = (ShapefileDialogData)savedInstanceState.getSerializable(TAG_SAVESTATE);
        }

        if(data.srid != -1)
        {
            mSridEditText.setText(Integer.toString(data.srid));
        }
        
        mCharsetEditText.setText(data.charset);
        mNameEditText.setText(data.name);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        data.name = mNameEditText.getText().toString();
        data.srid = Integer.parseInt(mSridEditText.getText().toString());
        data.charset = mCharsetEditText.getText().toString();
        outState.putSerializable(TAG_SAVESTATE, data);
        
        super.onSaveInstanceState(outState);
    }  
    // abstract protected methods ================================================================================
    
    protected abstract void initDialog();
    protected abstract void checkValues(String name);
    protected abstract void exec(String name, String sridString, String charset);
    
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
                checkValues(name);
                
                // srid
                String sridString = mSridEditText.getText().toString();
                if(sridString.isEmpty())
                {
                    throw new RuntimeException(getString(R.string.srid_empty_error));
                }
                
                // charset
                String charset = mCharsetEditText.getText().toString();
                if(charset.isEmpty())
                {
                    throw new RuntimeException(getString(R.string.charset_empty_error));
                }
                
                exec(name, sridString, charset);
                
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
