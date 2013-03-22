/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

import jsqlite.Exception;
import android.content.DialogInterface;
import android.widget.Toast;
import cz.kalcik.vojta.terraingis.AttributeTableActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.view.AttributeTableRow;

/**
 * @author jules
 *
 */
public class UpdateAttributesDialog extends SetAttributesDialog
{
    // attributes ====================================================================================
    private String[] mOriginValues = null;
    private AttributeTableRow mRow;
    
    // public methods ================================================================================
    
    // getter setter ==================================================================================    
    /**
     * @param row the mRow to set
     */
    public void setRow(AttributeTableRow row)
    {
        this.mRow = row;
        mOriginValues = row.getValues();
    }

    // protected methods ==============================================================================
    @Override
    protected String getValueOfAttribute(Column column, int i)
    {
        return mOriginValues[i];
    }

    @Override
    protected void execute()
    {
        AttributeTableActivity activity = (AttributeTableActivity)getActivity();
        SpatiaLiteIO spatialite = activity.getSpatialite();
        try
        {
            spatialite.updateAttributes(mLayer.getData().name,
                    mLayer.getAttributeHeader().getUpdateSQLArgs(false), mOriginValues,
                    Integer.parseInt(mRow.getRowid()));
            mRow.reloadCells(activity.getLayoutInflater(), getValues());
        }
        catch (Exception e)
        {
            Toast.makeText(activity, R.string.database_error, Toast.LENGTH_LONG).show();
        }
    }
}
