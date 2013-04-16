package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jsqlite.Exception;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer.AbstractLayerData;
import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    public static final double SPHERICAL_MERCATOR_DIST = 20037508.342789;
    
    // attributes =========================================================================
    private ArrayList<AbstractLayer> layers = new ArrayList<AbstractLayer>();
    private SpatiaLiteIO spatialiteManager = null;
    private int srid = SpatiaLiteIO.EPSG_SPHERICAL_MERCATOR;

    // public methods ======================================================================
    
    public void redraw(Canvas canvas, Envelope rect, Context context)
    {
        int size = layers.size();
        
        for(int i=size-1; i >= 0; i--)
        {
            AbstractLayer layer = layers.get(i);
            
            if(layer.isVisible())
            {
                try
                {
                    layer.draw(canvas, rect);
                }
                catch (Exception e)
                {
                    Toast.makeText(context, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
                catch (ParseException e)
                {
                    Toast.makeText(context, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
   
    public void detach()
    {
        for(AbstractLayer layer: layers)
        {
            layer.detach();
        }
        
        layers.clear();
        VectorLayerPaints.resetColors();
    }

    /**
     * load layers from spatialite database
     * @param path
     * @throws Exception 
     */
    public void loadSpatialite(MapFragment mapFragment) throws Exception
    {
        if(spatialiteManager == null)
        {
            spatialiteManager = new SpatiaLiteIO(MainActivity.DB_FILE.getAbsolutePath());
        }
        
        Map<String, Boolean> mapLayers = mapOfNames();
        
        for(SpatiaLiteIO.Layer layer: spatialiteManager.getLayers())
        {
            // is layer loaded? 
            if(mapLayers.containsKey(layer.name))
            {
                mapLayers.put(layer.name, true);
                
                continue;
            }
            
            AbstractLayer newLayer = createVectorLayer(layer, spatialiteManager, mapFragment);
            
            mapLayers.put(newLayer.toString(), true);
            
            layers.add(newLayer);
        }
        
        int size = layers.size();
        
        // remove layers
        for(int i=size-1; i >= 0; i--)
        {
            AbstractLayer layer = layers.get(i);
            
            if(layer instanceof VectorLayer && !mapLayers.get(layer.toString()))
            {
                layer.detach();
                layers.remove(i);
            }
        }
    }
    
    /**
     * load tile layer and spatialite
     * @param context
     * @param map
     */
    public void loadLayers(Context context, MapFragment mapFragment,MapView map)
    {
        try
        {
            loadSpatialite(mapFragment);
        }
        catch (Exception e)
        {
            Toast.makeText(context, R.string.database_error,
                    Toast.LENGTH_LONG).show();
            Log.d("TerrainGIS", e.getMessage());
        }
        addTilesLayer(context, map);
    }

    /**
     * convert lon lat coordinates to meters
     * @param input
     * @return
     * @throws ParseException 
     * @throws Exception 
     */
    public Coordinate lonLatWGS84ToM(Coordinate input)
            throws Exception, ParseException
    {        
        return spatialiteManager.transformSRS(input, SpatiaLiteIO.EPSG_LONLAT, srid);
    }
    
    /**
     * convert meters coordinates to lon lat
     * @param input
     * @return
     * @throws ParseException 
     * @throws Exception 
     */
    public Coordinate mToLonLatWGS84(Coordinate input)
            throws Exception, ParseException
    {
        return spatialiteManager.transformSRS(input, srid, SpatiaLiteIO.EPSG_LONLAT);
    }
    
    /**
     * check if is layer with same name
     * @param name
     * @return
     */
    public boolean hasLayer(String name)
    {
        for(AbstractLayer layer: layers)
        {
            if(layer.toString().equals(name))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return data for serialization
     */
    public ArrayList<AbstractLayerData> getData()
    {
        ArrayList<AbstractLayerData> result = new ArrayList<AbstractLayerData>();
        
        for(AbstractLayer layer: layers)
        {
            result.add(layer.getData());
        }
        
        return result;
    }
    
    /**
     * set data to layers and sort by previous order
     * @param data
     */
    public void setData(ArrayList<AbstractLayerData> data)
    {
        Map<String, AbstractLayer> mapLayers = new TreeMap<String, AbstractLayer>();
        
        for(AbstractLayer layer: layers)
        {
            mapLayers.put(layer.toString(), layer);
        }
        
        layers.clear();
        
        for(AbstractLayerData dataLayer: data)
        {
            AbstractLayer layer = mapLayers.get(dataLayer.name);
            layer.setData(dataLayer);
            layers.add(layer);
        }
    }
    
    /**
     * find layer with name
     * @param name
     * @return
     */
    public VectorLayer getLayerByName(String name)
    {
        for(AbstractLayer layer: layers)
        {
            if(layer.toString().equals(name) && layer instanceof VectorLayer)
            {
                return (VectorLayer)layer;
            }
        }
        
        return null;
    }
    
    /**
     * close database
     * @throws Exception
     */
    public void closeDatabase() throws Exception
    {
        spatialiteManager.close();
    }
    // getter setter =======================================================================
    
    /**
     * @return the layers
     */
    public ArrayList<AbstractLayer> getLayers()
    {
        return layers;
    }

    /**
     * @return the srid
     */
    public int getSrid()
    {
        return srid;
    }
    
    /**
     * @return the spatialiteManager
     */
    public SpatiaLiteIO getSpatialiteIO()
    {
        return spatialiteManager;
    }

    // public static method ==================================================================
    /**
     * create VectorLayer by layer attributes from spatialite
     * @param layer
     * @param spatialite
     * @return
     * @throws Exception 
     */
    public static VectorLayer createVectorLayer(Layer layer, SpatiaLiteIO spatialite, MapFragment mapFragment)
            throws Exception
    {
        VectorLayer result = null;
        
        if(layer.type.equals("POINT") || layer.type.equals("MULTIPOINT"))
        {
            result = new PointsLayer(layer.name, layer.srid, spatialite, mapFragment);
        }
        else if(layer.type.equals("LINESTRING") || layer.type.equals("MULTILINESTRING"))
        {
            result = new LinesLayer(layer.name, layer.srid, spatialite, mapFragment);
        }
        else if(layer.type.equals("POLYGON") || layer.type.equals("MULTIPOLYGON"))
        {
            result = new PolygonsLayer(layer.name, layer.srid, spatialite, mapFragment);
        }
        
        return result;
    }
    
    // private methods =======================================================================
    /**
     * remove all TilesLayers
     */
    private void removeTilesLayers()
    {
        int size = layers.size();
        
        for(int i=size-1; i >= 0; i--)
        {
            AbstractLayer layer = layers.get(i);
            
            if(layer instanceof TilesLayer)
            {
                layer.detach();
                
                layers.remove(i);
            }
        }
    }
    
    /**
     * create tiles layer
     * @param aContext
     * @param map
     */
    private void addTilesLayer(Context context, MapView map)
    {
        removeTilesLayers();
        
        layers.add(new TilesLayer());
    }
    
    /**
     * create map, which key is name and value is false 
     * @return
     */
    private Map<String, Boolean> mapOfNames()
    {
        Map<String, Boolean> result = new TreeMap<String, Boolean>();
        
        for(AbstractLayer layer: layers)
        {
            result.put(layer.toString(), false);
        }
        
        return result;
    }
}