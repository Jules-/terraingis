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
package cz.kalcik.vojta.terraingis.fragments;

import cz.kalcik.vojta.terraingis.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
{
    // constants ===================================================================
    public static final String KEY_GPS_MINDIST = "settings_record_min_distance";
    public static final String KEY_GPS_MINACCURACY = "settings_record_min_accuracy";
    public static final String KEY_GPS_MINTIME = "settings_record_min_time";
    
    // on methods ==================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
    
    // public static methods =========================================================
    /**
     * @param sharedPref
     * @param key
     * @return int value from shared preference
     */
    public static int getIntShareSettings(SharedPreferences sharedPref, String key)
    {
        return Integer.parseInt(sharedPref.getString(key, null));
    }
}
