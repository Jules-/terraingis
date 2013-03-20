/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import java.io.File;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Environment;

/**
 * @author jules
 *
 */
public abstract class AbstractActivity extends Activity
{
    public static final File APP_DIRECTORY = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TerrainGIS");
    public static final File DB_FILE = new File(APP_DIRECTORY.getAbsoluteFile()+"/db.sqlite");
    protected static final String DIALOG_TAG = "DialogTag";
    
    /**
     * show dialog
     * @param dialog
     */
    public void showDialog(DialogFragment dialog)
    {
        dialog.show(getFragmentManager(), DIALOG_TAG);
    }
}
