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