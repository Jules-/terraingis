package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.view.Navigator;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * class for settings
 * @author jules
 *
 */
public class Settings
{
    // singleton code =====================================================================
    
    private static Settings instance = new Settings();
    
    private Settings() { }
    
    public static Settings getInstance()
    {
        return instance;
    }
    
    // attributes ===================================================================
    private int locationIcon = R.drawable.location;
    private boolean hideActionBar = true;
    private int timeHideActionBar = 5000;
    
    // getters, setters =============================================================
    
    /**
     * getter location icon
     * @return
     */
    public int getLocationIcon()
    {
        return locationIcon;
    }
    
    public boolean isHideActionBar()
    {
        return hideActionBar;
    }

    public void setHideActionBar(boolean hideActionBar)
    {
        this.hideActionBar = hideActionBar;
    }
    
    public int getTimeHideActionBar()
    {
        return timeHideActionBar;
    }

    public void setTimeHideActionBar(int timeHideActionBar)
    {
        this.timeHideActionBar = timeHideActionBar;
    }
}