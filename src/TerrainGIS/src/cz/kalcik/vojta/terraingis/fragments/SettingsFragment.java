package cz.kalcik.vojta.terraingis.fragments;

import cz.kalcik.vojta.terraingis.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
{
    // constants ===================================================================
    public static final String KEY_GPS_MINDIST = "settings_record_min_distance";
    public static final String KEY_GPS_MINTIME = "settings_record_min_time";
    
    // on methods ==================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}
