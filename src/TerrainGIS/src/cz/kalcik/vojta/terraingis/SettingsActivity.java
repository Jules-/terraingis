/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import cz.kalcik.vojta.terraingis.fragments.SettingsFragment;
import android.app.Activity;
import android.os.Bundle;

/**
 * @author jules
 * 
 */
public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
