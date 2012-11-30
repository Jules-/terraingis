package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.exception.TerrainGISException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

public class LayerManager
{
    // constants ==========================================================================
    
    private final static double O_EARTH_ZOOM_LEVEL = 40075016.68557849;
    
    // attributes =========================================================================
    
    private Projection projection;
    
    private ArrayList<AbstarctLayer> layers = new ArrayList<AbstarctLayer>();
    private double zoom;
    private Point2D.Double latLonPosition = new Point2D.Double(0,0);
    
    public LayerManager()
    {
    }
    
    public void appendTileLayer(final MapTileProviderBase aTileProvider, final Context aContext)
    {
        layers.add(new TilesLayer(aTileProvider, aContext, this));
    }
    
    public void redraw(Canvas canvas, Rect screenRect)
    {
        for(AbstarctLayer layer: layers)
        {
            layer.draw(canvas, screenRect, zoom);
        }
    }
   
    // getter setter =======================================================================
    
    public Projection getProjection()
    {
        return projection;
    }

    public void setProjection(Projection projection)
    {
        this.projection = projection;
    }

    public double getZoom()
    {
        return zoom;
    }

    public void setZoom(double zoom)
    {
        this.zoom = zoom;
    }

    public void setZoomLevel(int zoomLevel)
    {
        setZoom(zoomLevelToMpx(zoomLevel));
    }
    
    public Point2D getLatLonPosition()
    {
        return latLonPosition;
    }

    public void setLatLonPosition(Point2D.Double latLonPosition)
    {
        this.latLonPosition = latLonPosition;
    }
    
    public void setLatLonPosition(double lon, double lat)
    {
        latLonPosition.x = lon;
        latLonPosition.y = lat;
    } 
    
    public Point getPositionPx()
    {
        Point2D.Double position = getMeteresPosition();
        
        return new Point(meteresToPixels(position.x), meteresToPixels(position.y));
    }
    
    // static public methods ===============================================================
    
    static public int mpxToZoomLevel(double zoom)
    {
        double doubleZoomLevel = -(Math.log(zoom/O_EARTH_ZOOM_LEVEL)+8*Math.log(2))/Math.log(2);
        return (int)Math.round(doubleZoomLevel);
    }
    
    static public double zoomLevelToMpx(int zoomLevel)
    {
        return O_EARTH_ZOOM_LEVEL/Math.pow(2, (zoomLevel+8));
    }
    
    // private methods =====================================================================
    
    private Point2D.Double getMeteresPosition()
    {
        if(projection == null)
        {
            throw new TerrainGISException("Projection is not set!");
        }
        
        Point2D.Double output = new Point2D.Double();
        return projection.transform(latLonPosition, output);
    }
    
    private int meteresToPixels(double meters)
    {
        return (int)Math.round(meters/zoom);
    }
}