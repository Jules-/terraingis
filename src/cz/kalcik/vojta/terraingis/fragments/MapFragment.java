package cz.kalcik.vojta.terraingis.fragments;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLine;
import cz.kalcik.vojta.terraingis.layer.VectorPolygon;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.R.id;
import cz.kalcik.vojta.terraingis.R.layout;

/**
 * fragment with MapView
 * @author jules
 *
 */
public class MapFragment extends Fragment
{
    // constants ==========================================================
    private static String MAP_VIEW_DATA = "MapViewData";
    
    // properties =========================================================
    private MapView map;
    private LayerManager layerManager = LayerManager.getInstance();
    private Activity attachActivity;

    // public methods =====================================================
    
    // getter, setter =====================================================
    
    public MapView getMap()
    {
        return map;
    }
    
    // on methods =========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.map_layout, container, false);
        
        // Map view state
        map = (MapView) myView.findViewById(R.id.map);
        createTestingMap();
        
        return myView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            map.setData(savedInstanceState.getSerializable(MAP_VIEW_DATA));
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // Map view state
        outState.putSerializable(MAP_VIEW_DATA, map.getData());
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        attachActivity = activity;
    }
    
    // private methods ========================================================
    
    private void createTestingMap()
    {
        layerManager.loadSpatialite(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TerrainGIS/propojky.sqlite");
         
        // tiles layer        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic((Context)attachActivity, tileSource);

        layerManager.addTilesLayer(tileProvider, (Context)attachActivity, map);
    }
}