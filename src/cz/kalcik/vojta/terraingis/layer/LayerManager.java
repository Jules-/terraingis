package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

public class LayerManager
{
    private ArrayList<AbstarctLayer> layers = new ArrayList<AbstarctLayer>();
    
    public LayerManager()
    {
    }
    
    public void appendTileLayer(final MapTileProviderBase aTileProvider, final Context aContext)
    {
        layers.add(new TileLayer(aTileProvider, aContext));
    }
    
    public void redraw(Canvas canvas, Rect screenRect)
    {
        for(AbstarctLayer layer: layers)
        {
            layer.draw(canvas, screenRect);
        }
    }
}