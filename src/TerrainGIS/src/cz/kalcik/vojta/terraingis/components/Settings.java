package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.terraingis.R;

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
    
    // constants ==========================================================================
    public static final int LOCATION_MINDIST_DEFAULT = 0; 
    public static final int DP_RADIUS_CLICK = 20; 
    public static final int DP_SIZE_SIDE_CLICK = 50; 
    
    // attributes ===================================================================
    private int locationIcon = R.drawable.location;
    private int locationAddPontIcon = R.drawable.location_add_point;
    private boolean hideActionBar = true;
    private int timeHideActionBar = 8000;
    
    // getters, setters =============================================================
    
    /**
     * @return location icon
     */
    public int getLocationIcon()
    {
        return locationIcon;
    }
    
    public int getLocationAddPointIcon()
    {
        return locationAddPontIcon;
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