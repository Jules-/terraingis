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
    
    // attributes ===================================================================
    private int locationIcon = R.drawable.location;
    private boolean hideActionBar = true;
    private int timeHideActionBar = 5000;
    private int mAutoRecordMinDist = 10;
    
    // getters, setters =============================================================
    
    /**
     * @return location icon
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
    
    /**
     * @return the mAutoRecordMinDist
     */
    public int getAutoRecordMinDist()
    {
        return mAutoRecordMinDist;
    }

    /**
     * @param mAutoRecordMinDist the mAutoRecordMinDist to set
     */
    public void setAutoRecordMinDist(int mAutoRecordMinDist)
    {
        this.mAutoRecordMinDist = mAutoRecordMinDist;
    }
}