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
    public static final double MIN_M_DISTANCE = 0.000001;
    public static final int LOCATION_MINDIST_DEFAULT = 0; 
    public static final int DP_RADIUS_CLICK = 20; 
    
    // attributes ===================================================================
    private int locationIcon = R.drawable.location;
    private boolean hideActionBar = true;
    private int timeHideActionBar = 8000;
    private int mAutoRecordMinDist = 10;
    private int mLocationMinTime = 2000;
    private int mRecordMinAccuracy = 20;
    
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
    public void setAutoRecordMinDist(int autoRecordMinDist)
    {
        this.mAutoRecordMinDist = autoRecordMinDist;
    }
    
    /**
     * @return the mLocationMinTime
     */
    public int getLocationMinTime()
    {
        return mLocationMinTime;
    }

    /**
     * @param mLocationMinTime the mLocationMinTime to set
     */
    public void setLocationMinTime(int locationMinTime)
    {
        this.mLocationMinTime = locationMinTime;
    }

    /**
     * @return the mRecordMinAccuracy
     */
    public int getRecordMinAccuracy()
    {
        return mRecordMinAccuracy;
    }

    /**
     * @param mRecordMinAccuracy the mRecordMinAccuracy to set
     */
    public void setRecordMinAccuracy(int mRecordMinAccuracy)
    {
        this.mRecordMinAccuracy = mRecordMinAccuracy;
    }
}