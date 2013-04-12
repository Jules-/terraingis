/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;

import java.io.File;

import android.app.AlertDialog.Builder;
import android.util.Log;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.dialogs.ShapefileDialog;

/**
 * @author jules
 *
 */
public class ShapefileDialogImport extends ShapefileDialog
{
    // constants =====================================================================================
    private final String SUFFIX = ".shp";
    
    // attributes ====================================================================================
    private File mFile;
    private String mNameNoSuffix;
    
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
        if(!SUFFIX.equals(suffix))
        {
            throw new RuntimeException();
        }
        
        mNameNoSuffix = name.substring(0, dotIndex);
    }

    // protected methods ============================================================================
    @Override
    protected void initDialog(Builder dialogBuilder)
    {
        dialogBuilder.setTitle("Import " + mFile.getName());
        mNameEditText.setText(mNameNoSuffix);
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
            ShapeFileIO.getInstance().importShapefile(mFile.getParent(), mNameNoSuffix, name,
                    Integer.parseInt(sridString), charset, mMainActivity.getMapFragment());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
            throw new RuntimeException(getString(R.string.load_shapefile_error));
        }
    }
}
