package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.exception.TerrainGISException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * class which communicate with all layers
 * @author jules
 *
 */
public class LayerManager
{
    // constants ==========================================================================
    
    private final static double O_EARTH_ZOOM_LEVEL = 40075016.68557849;
    
    // attributes =========================================================================
    
    private Projection projection;
    
    private ArrayList<ILayer> layers = new ArrayList<ILayer>();
    private double zoom;
    private Point2D.Double latLonPosition = new Point2D.Double(0,0);
    
    public LayerManager()
    {
    }
    
    // public methods ======================================================================
    
    public void appendTileLayer(final MapTileProviderBase aTileProvider, final Context aContext)
    {
        layers.add(new TilesLayer(aTileProvider, aContext, this));
    }
    
    public void redraw(Canvas canvas, Rect screenRect)
    {
        for(ILayer layer: layers)
        {
            layer.draw(canvas, screenRect);
        }
    }
    
    public void offsetPx(int x, int y)
    {
        Point pxPosition = getPositionPx();
        pxPosition.offset(x, y);
        
        setPositionPx(pxPosition);
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
    
    public Point2D.Double getLatLonPosition()
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
        return latLonToPx(latLonPosition, (Point)null);
    }

    public void setPositionPx(Point pxPosition)
    {               
        pxToLatLon(pxPosition, latLonPosition);
    }
    
    public void setPositionPx(PointF pxPosition)
    {               
        pxToLatLon(pxPosition, latLonPosition);
    }
    
    public Point2D.Double pxToLatLon(Point input, Point2D.Double output)
    {
        Point2D.Double meters = pxToM(input, null);
        
        return mToLatLon(meters, output);
    }

    public Point2D.Double pxToLatLon(PointF input, Point2D.Double output)
    {
        Point2D.Double meters = pxToM(input, null);
        
        return mToLatLon(meters, output);
    }

    public Point latLonToPx(Point2D.Double input, Point output)
    {
        Point2D.Double meters = latLonToM(input, null);
        
        return mToPx(meters, output);
    }
    
    public PointF latLonToPx(Point2D.Double input, PointF output)
    {
        Point2D.Double meters = latLonToM(input, null);
        
        return mToPx(meters, output);
    }
    
    // static public methods ===============================================================
    
    static public int mpxToZoomLevel(double zoom)
    {
        double doubleZoomLevel = -(Math.log(zoom/O_EARTH_ZOOM_LEVEL)+8*Math.log(2))/Math.log(2);
        return (int)Math.ceil(doubleZoomLevel);
    }
    
    static public double zoomLevelToMpx(int zoomLevel)
    {
        return O_EARTH_ZOOM_LEVEL/Math.pow(2, (zoomLevel+8));
    }
   
    // private methods =====================================================================
    
    private Point mToPx(Point2D.Double input, Point output)
    {
        if(output == null)
        {
            output = new Point();
        }
        
        output.set((int)Math.round(input.x/zoom),
                  (int)Math.round(input.y/zoom));
        
        return output;
    }
    
    private PointF mToPx(Point2D.Double input, PointF output)
    {
        if(output == null)
        {
            output = new PointF();
        }
        
        output.set((float)(input.x/zoom), (float)(input.y/zoom));
        
        return output;
    }

    private Point2D.Double pxToM(Point input, Point2D.Double output)
    {
        if(output == null)
        {
            output = new Point2D.Double();
        }
        
        output.x = input.x * zoom;
        output.y = input.y * zoom;
        
        return output;
    }
    
    private Point2D.Double pxToM(PointF input, Point2D.Double output)
    {
        if(output == null)
        {
            output = new Point2D.Double();
        }
        
        output.x = input.x * zoom;
        output.y = input.y * zoom;
        
        return output;
    }

    private Point2D.Double latLonToM(Point2D.Double input, Point2D.Double output)
    {
        if(projection == null)
        {
            throw new TerrainGISException("Projection is not set!");
        }
        
        if(output == null)
        {
            output = new Point2D.Double();
        }
        
        return projection.transform(input, output);
    }
    
    private Point2D.Double mToLatLon(Point2D.Double input, Point2D.Double output)
    {
        if(projection == null)
        {
            throw new TerrainGISException("Projection is not set!");
        }
        
        if(output == null)
        {
            output = new Point2D.Double();
        }
        
        return projection.inverseTransform(input, output);
    }
}