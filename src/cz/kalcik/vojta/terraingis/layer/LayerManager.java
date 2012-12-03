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
    // singleton code =====================================================================
   
    private static LayerManager instance = new LayerManager();
    
    private LayerManager() { }
    
    public static LayerManager getInstance()
    {
        return instance;
    }
    
    // attributes =========================================================================
    
    private Projection projection;
    
    private ArrayList<AbstractLayer> layers = new ArrayList<AbstractLayer>();
    
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
    public void addLayer(AbstractLayer layer)
    {
        layers.add(layer);
    }
    
    public void redraw(Canvas canvas, Rectangle2D.Double rect)
    {
        for(AbstractLayer layer: layers)
        {
            layer.draw(canvas, rect);
        }
    }
   
    public void onDetach()
    {
        for(AbstractLayer layer: layers)
        {
            layer.onDetach();
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
    
    // public methods =======================================================================

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
       
    public Point2D.Double mToLonLat(Point2D.Double input, Point2D.Double output)
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