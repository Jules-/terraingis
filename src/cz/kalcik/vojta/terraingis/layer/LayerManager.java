package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
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
    
    // constants ==========================================================================
    
    private final int EPSG_SPHERICAL_MERCATOR = 3857;
    private final int EPSG_LONLAT = 4326;
    
    // attributes =========================================================================
    
    private ArrayList<AbstractLayer> layers = new ArrayList<AbstractLayer>();
    private SpatiaLiteManager spatialiteManager;
    
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
    
    public void redraw(Canvas canvas, Envelope rect)
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
        
        layers.clear();
    }
    
    /**
     * load layers from spatialite database
     * @param path
     */
    public void loadSpatialite(String path)
    {
    	spatialiteManager = new SpatiaLiteManager(path);
    	for(String[] values: spatialiteManager.getLayers())
    	{
    	    int srid = Integer.parseInt(values[2]);
    	    AbstractLayer newLayer = null;
    	    
    	    if(values[1].equals("POINT") || values[1].equals("MULTIPOINT"))
    	    {
    	        newLayer = new PointsLayer(values[0], srid, spatialiteManager);
    	    }
    	    else if(values[1].equals("LINESTRING") || values[1].equals("MULTILINESTRING"))
    	    {
    	        newLayer = new LinesLayer(values[0], srid, spatialiteManager);
    	    }
            else if(values[1].equals("POLYGON") || values[1].equals("MULTIPOLYGON"))
            {
                newLayer = new PolygonsLayer(values[0], srid, spatialiteManager);
            }
    	    
    		layers.add(newLayer);
    	}
    }
    
    // getter setter =======================================================================
    
    /**
     * @return the layers
     */
    public ArrayList<AbstractLayer> getLayers()
    {
        return layers;
    }

    // public methods =======================================================================
    public Coordinate lonLatToM(Coordinate input)
    {        
        return spatialiteManager.transformSRS(input, EPSG_LONLAT, EPSG_SPHERICAL_MERCATOR);
    }
       
    public Coordinate mToLonLat(Coordinate input)
    {
        return spatialiteManager.transformSRS(input, EPSG_SPHERICAL_MERCATOR, EPSG_LONLAT);
    }
}