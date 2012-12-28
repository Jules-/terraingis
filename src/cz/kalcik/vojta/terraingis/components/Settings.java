package cz.kalcik.vojta.terraingis.components;

import cz.kalcik.vojta.terraingis.R;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * class for settings
 * @author jules
 *
 */
public class Settings
{
    // attributes ===================================================================
    private Context context;
    private Drawable locationIcon;
    
    /**
     * constructor
     * @param context
     */
    public Settings(Context context)
    {
        this.context = context;
        locationIcon = context.getResources().getDrawable(R.drawable.location);
    }
    
    // getters, setters =============================================================
    
    /**
     * getter location icon
     * @return
     */
    public Drawable getLocationIcon()
    {
        return locationIcon;
    }
}