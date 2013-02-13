package cz.kalcik.vojta.terraingis.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;

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
    private MainActivity mMainActivity;
    private Button mButtonRecordObject;
    private Button mButtonRecordPoint;

    // public methods =====================================================
    /**
     * set visibility of record buttons
     */
    public void changeRecordButtons()
    {
        if(mMainActivity.isRecordMode())
        {
            AbstractLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayer();
            if(selectedLayer == null)
            {
                mButtonRecordObject.setVisibility(View.GONE);
                mButtonRecordPoint.setVisibility(View.GONE);                
            }
            else
            {
                mButtonRecordObject.setVisibility(View.VISIBLE);
                mButtonRecordPoint.setVisibility(View.VISIBLE);                
            }
        }
        else
        {
            mButtonRecordObject.setVisibility(View.GONE);
            mButtonRecordPoint.setVisibility(View.GONE);
        }
    }
    
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
        
        // record buttons
        mButtonRecordObject = (Button)myView.findViewById(R.id.button_record_object);
        mButtonRecordPoint = (Button)myView.findViewById(R.id.button_record_point);
        
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
        
        mMainActivity = (MainActivity)activity;
    }
    
    // private methods ========================================================
    
    private void createTestingMap()
    {
        layerManager.loadSpatialite(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TerrainGIS/db.sqlite");
         
        // tiles layer        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic((Context)mMainActivity, tileSource);

        layerManager.addTilesLayer(tileProvider, (Context)mMainActivity, map);
    }
}