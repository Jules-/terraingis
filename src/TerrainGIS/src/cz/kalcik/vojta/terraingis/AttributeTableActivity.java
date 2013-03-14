/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author jules
 * activity for showing attribute table
 */
public class AttributeTableActivity extends Activity
{
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_table);
    }
}
