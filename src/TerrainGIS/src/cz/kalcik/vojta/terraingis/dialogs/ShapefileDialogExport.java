/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import android.app.AlertDialog.Builder;

/**
 * @author jules
 *
 */
public class ShapefileDialogExport extends ShapefileDialog
{
    private String mLayerName;
    
    // getter setter =================================================================================   
    /**
     * set name of exported layer
     * @param layerName
     */
    public void setLayerName(String layerName)
    {
        mLayerName = layerName;
    }

    // protected methods ============================================================================
    @Override
    protected void initDialog(Builder dialogBuilder)
    {
        dialogBuilder.setTitle("Export " + mLayerName);
        mNameEditText.setText(mLayerName + ".shp");
    }

    @Override
    protected void checkValues(String name)
    {
        checkNameEmpty(name);
    }

    @Override
    protected void exec(String name, String sridString, String charset)
    {
        // TODO Auto-generated method stub
        
    }

}
