/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialogImport.ShapefileDialogImportData;
import cz.kalcik.vojta.terraingis.io.ShapeFileIO;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.util.Log;

/**
 * @author jules
 *
 */
public class ShapefileDialogExport extends ShapefileDialog
{
    // constants ====================================================================================
    private static final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.ShapefileDialogExportSaveState";
    
    // attributes ===================================================================================
    private String mLayerName;
    
    // public methods ===============================================================================
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        if(savedInstanceState != null)
        {
            mLayerName = savedInstanceState.getString(TAG_SAVESTATE);
        }
        initDialog();
        
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(TAG_SAVESTATE, mLayerName);
        
        super.onSaveInstanceState(outState);
    } 
    
    // getter setter =================================================================================   
    /**
     * set name of exported layer
     * @param layerName
     */
    public void setLayerName(String layerName)
    {
        mLayerName = layerName;
        data.name = mLayerName + ".shp";
    }
    
    /**
     * set srid
     * @param srid
     */
    public void setSrid(int srid)
    {
        data.srid = srid;
    }
    
    // protected methods ============================================================================
    @Override
    protected void initDialog()
    {
        getDialog().setTitle("Export " + mLayerName);
    }

    @Override
    protected void checkValues(String name)
    {
        checkNameEmpty(name);
    }

    @Override
    protected void exec(String name, String sridString, String charset)
    {
        name = removeSuffix(name);
        
        try
        {
            if(!MainActivity.OUTPUT_DIRECTORY.exists())
            {
                MainActivity.OUTPUT_DIRECTORY.mkdirs();
            }
            
            ShapeFileIO.getInstance().exportShapefile(MainActivity.OUTPUT_DIRECTORY.getAbsolutePath(),
                    name, charset, mLayerName, Integer.parseInt(sridString), mMainActivity.getMapFragment());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
            throw new RuntimeException(getString(R.string.export_shapefile_error));
        }        
    }
    
    // private methods ===============================================================================
    /**
     * remove suffix of name
     * @param name
     * @return
     */
    private String removeSuffix(String name)
    {
        if(name.endsWith(MAIN_SUFFIX))
        {
            name = name.substring(0, name.length()-MAIN_SUFFIX.length());
        }
        
        return name;
    }
}
