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
    public static final File TILE_CACHE_FILE = new File(APP_DIRECTORY.getAbsoluteFile()+"/tile_cache.sqlite");
    public static final File OUTPUT_DIRECTORY = new File(APP_DIRECTORY.getAbsoluteFile()+"/output/");
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
