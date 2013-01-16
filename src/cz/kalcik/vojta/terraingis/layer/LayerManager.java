package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.jhlabs.map.proj.Projection;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D;
import cz.kalcik.vojta.terraingis.exception.TerrainGISException;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;

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
    public void addTilesLayer(final MapTileProviderBase aTileProvider, final Context aContext, MapView map)
    {               
        layers.add(new TilesLayer(aTileProvider, aContext));
        
        Handler mTileRequestCompleteHandler = new SimpleInvalidationHandler(map);
        aTileProvider.setTileRequestCompleteHandler(mTileRequestCompleteHandler);
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
        int size = layers.size();
        
        for(int i=size-1; i >= 0; i--)
        {
            layers.get(i).draw(canvas, rect);
        }
    }
   
    public void detach()
    {
        for(AbstractLayer layer: layers)
        {
            layer.detach();
        }
    }
    
    // getter setter =======================================================================
    /**
     * @return the projection
     */   
    public Projection getProjection()
    {
        return projection;
    }

    /**
     * set the projection
     */
    public void setProjection(Projection projection)
    {
        this.projection = projection;
    }
    
    /**
     * @return the layers
     */
    public ArrayList<AbstractLayer> getLayers()
    {
        return layers;
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