/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import android.os.Bundle;
import android.util.Log;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.ShapeFileIO;
import cz.kalcik.vojta.terraingis.layer.LayerManager;

/**
 * @author jules
 *
 */
public class ShapefileDialogImport extends ShapefileDialog
{
    // constants =====================================================================================
    private static final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.ShapefileDialogImportSaveState";

    // attributes ====================================================================================
    public static class ShapefileDialogImportData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public File mFile;
        public String mNameNoSuffix;
    }
    
    private ShapefileDialogImportData mImportData = new ShapefileDialogImportData();

    // public methods ================================================================================
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        if(savedInstanceState != null)
        {
            mImportData = (ShapefileDialogImportData) savedInstanceState.getSerializable(TAG_SAVESTATE);
        }
        
        initDialog();
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(TAG_SAVESTATE, mImportData);
        
        super.onSaveInstanceState(outState);
    } 
    
    // getter setter =================================================================================
    /**
     * set filename for import
     * @param file
     * @return
     */
    public void setFile(File file)
    {
        mImportData.mFile = file;
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        
        if(dotIndex == -1)
        {
            throw new RuntimeException();
        }
        
        String suffix = name.substring(dotIndex);
        if(!MAIN_SUFFIX.equals(suffix))
        {
            throw new RuntimeException();
        }
        
        mImportData.mNameNoSuffix = name.substring(0, dotIndex);
        data.name = mImportData.mNameNoSuffix;
        checkFilesWithProjection();
        checkFileWithCharset();
    }
    
    // protected methods ============================================================================
    @Override
    protected void initDialog()
    {
        getDialog().setTitle("Import " + mImportData.mFile.getName());
    }

    @Override
    protected void checkValues(String name)
    {
        checkName(name);        
    }

    @Override
    protected void exec(String name, String sridString, String charset)
    {
        try
        {
            ShapeFileIO.getInstance().importShapefile(mImportData.mFile.getParent(), mImportData.mNameNoSuffix, name,
                    charset, Integer.parseInt(sridString), mMainActivity.getMapFragment());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
            throw new RuntimeException(getString(R.string.import_shapefile_error));
        }
    }
    // private methods ================================================================================
    /**
     * check if exist qpj file with projection
     */
    private void checkFilesWithProjection()
    {
        File file = new File(mImportData.mFile.getParent(), mImportData.mNameNoSuffix + PROJECTION_SUFFIX);
        if(file.exists())
        {
            String text = "";
            try
            {
                text = readTextFromFile(file);
            }
            catch (IOException e)
            {
                return;
            }
            
            try
            {
                data.srid = LayerManager.getInstance().getSpatialiteIO().getSridByWKT(text);
            }
            catch (jsqlite.Exception e)
            {
                // nothing
            }
        }
    }
    
    /**
     * check charset in cpg file
     */
    private void checkFileWithCharset()
    {
        File file = new File(mImportData.mFile.getParent(), mImportData.mNameNoSuffix + CHARSET_SUFFIX);
        if(file.exists())
        {
            String charset = "";
            
            try
            {
                charset = readTextFromFile(file);
            }
            catch (IOException e)
            {
                return;
            }
            
            if(!charset.isEmpty())
            {
                data.charset = charset;
            }
        }        
    }
    
    /**
     * read text from some file
     * @param file
     * @return
     * @throws IOException
     */
    private String readTextFromFile(File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        StringBuilder builder = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null)
        {
            builder.append(line);
        }
        
        reader.close();
        
        return builder.toString().trim();        
    }
}
