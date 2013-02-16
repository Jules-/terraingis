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
import android.widget.Toast;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;
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
        boolean showObjectButton = false;
        boolean showPointButton = false;
        
        if(mMainActivity.isRecordMode())
        {
            AbstractLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayer();
            if(selectedLayer != null)
            {
                if(selectedLayer instanceof VectorLayer)
                {
                    VectorLayer selectedVectorLayer = (VectorLayer)selectedLayer;
                    VectorLayerType type = selectedVectorLayer.getType();
                    // point button
                    showPointButton = true;
    
                    // object button
                    if((type == VectorLayerType.LINE ||
                            type == VectorLayerType.POLYGON) &&
                        selectedVectorLayer.haveOpenedRecordObject())
                   {
                       showObjectButton = true;
                   }
                }
            }
        }
        
        if(showObjectButton)
        {
            mButtonRecordObject.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRecordObject.setVisibility(View.GONE);
        }
        
        if(showPointButton)
        {
            mButtonRecordPoint.setVisibility(View.VISIBLE);
        }
        else
        {
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
        mButtonRecordPoint.setOnClickListener(addPointHandler);
        
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
    
    // handlers ===============================================================
    
    /**
     * add point
     */
    View.OnClickListener addPointHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Coordinate location = mMainActivity.getLocationWorker().getLocation();
            if(location == null)
            {
                Toast.makeText((Context)mMainActivity, R.string.record_point_error, Toast.LENGTH_LONG).show();
                return;
            }
            
            VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
            
            selectedLayer.addPoint(location);
            if(selectedLayer.getType() == VectorLayerType.POINT)
            {
                selectedLayer.endObject();
            }
            
            changeRecordButtons();
        }        
    };
}