package cz.kalcik.vojta.view;

import android.content.Context;
import android.util.AttributeSet;

public class MapView extends org.osmdroid.views.MapView
{
    public MapView(Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public MapView(Context context, int tileSizePixels)
    {
        super(context, tileSizePixels);
    }   
}