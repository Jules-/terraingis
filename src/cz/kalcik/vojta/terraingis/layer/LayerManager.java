package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D;
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
    
    // singleton code =====================================================================
   
    private static LayerManager instance = new LayerManager();
    
    private LayerManager() {}
    
    public static LayerManager getInstance()
    {
        return instance;
    }
    
    // attributes =========================================================================
    
    private Projection projection;
    
    private ArrayList<ILayer> layers = new ArrayList<ILayer>();
    private double zoom;
    private Point2D.Double lonLatPosition = new Point2D.Double(0,0);
    
    // public methods ======================================================================
    /**
     * create tiles layer
     * @param aTileProvider
     * @param aContext
     */
    public void addTilesLayer(final MapTileProviderBase aTileProvider, final Context aContext)
    {
        layers.add(new TilesLayer(aTileProvider, aContext));
    }
    
    /**
     * add layer in layers
     * @param layer
     */
    public void addLayer(ILayer layer)
    {
        layers.add(layer);
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
    
    public Point2D.Double getLonLatPosition()
    {
        return lonLatPosition;
    }

    public void setLonLatPosition(Point2D.Double lonLatPosition)
    {
        this.lonLatPosition = lonLatPosition;
    }
    
    public void setLonLatPosition(double lon, double lat)
    {
        lonLatPosition.x = lon;
        lonLatPosition.y = lat;
    } 
        
    public Point getPositionPx()
    {
        return lonLatToPx(lonLatPosition, (Point)null);
    }

    public void setPositionPx(Point pxPosition)
    {               
        pxToLonLat(pxPosition, lonLatPosition);
    }
    
    public void setPositionPx(PointF pxPosition)
    {               
        pxToLonLat(pxPosition, lonLatPosition);
    }
    
    public Point2D.Double pxToLonLat(Point input, Point2D.Double output)
    {
        Point2D.Double meters = pxToM(input, null);
        
        return mToLonLat(meters, output);
    }

    public Point2D.Double pxToLonLat(PointF input, Point2D.Double output)
    {
        Point2D.Double meters = pxToM(input, null);
        
        return mToLonLat(meters, output);
    }

    public Point lonLatToPx(Point2D.Double input, Point output)
    {
        Point2D.Double meters = lonLatToM(input, null);
        
        return mToPx(meters, output);
    }
    
    public PointF lonLatToPx(Point2D.Double input, PointF output)
    {
        Point2D.Double meters = lonLatToM(input, null);
        
        return mToPx(meters, output);
    }
    
    public Rectangle2D.Double pxToM(Rect input, Rectangle2D.Double output)
    {
        if(output == null)
        {
            output = new Rectangle2D.Double();
        }
        
        Point2D.Double lbPoint = pxToM(new Point(input.left, input.top), null);
        Point2D.Double rtPoint = pxToM(new Point(input.right, input.bottom), null);
        
        output.setRect(lbPoint.x, lbPoint.y, rtPoint.x-lbPoint.x, rtPoint.y-lbPoint.y);
        
        return output;
    }
    
    public PointF mToPx(Point2D.Double input, PointF output)
    {
        if(output == null)
        {
            output = new PointF();
        }
        
        output.set((float)(input.x/zoom), (float)(input.y/zoom));
        
        return output;
    }
    

    public Point2D.Double lonLatToM(Point2D.Double input, Point2D.Double output)
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
    
    private Point2D.Double mToLonLat(Point2D.Double input, Point2D.Double output)
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