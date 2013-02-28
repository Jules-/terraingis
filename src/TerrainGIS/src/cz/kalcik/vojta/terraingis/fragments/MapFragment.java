package cz.kalcik.vojta.terraingis.fragments;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.components.LocationWorker;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer.AbstractLayerData;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;

/**
 * @author jules
 * fragment with MapView
 */
public class MapFragment extends Fragment
{
    // constants ==========================================================
    private static String MAP_VIEW_DATA = "cz.kalcik.vojta.terraingis.MapViewData";
    private static String LAYERS_DATA = "cz.kalcik.vojta.terraingis.LayersData";
    
    // properties =========================================================
    private MapView map;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private MainActivity mMainActivity;
    private Button mButtonRecordAuto;
    private Button mButtonRecordEndObject;
    private Button mButtonRecordPoint;
    

    // public methods =====================================================
    /**
     * set visibility of record buttons
     */
    public void changeRecordButtons()
    {
        boolean showObjectButton = false;
        boolean showPointButton = false;
        boolean showAutoButton = false;
        
        if(mMainActivity.isRecordMode())
        {
            AbstractLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayer();
            // is selected layer
            if(selectedLayer != null)
            {
                if(selectedLayer instanceof VectorLayer)
                {
                    VectorLayer selectedVectorLayer = (VectorLayer)selectedLayer;
                    VectorLayerType type = selectedVectorLayer.getType();
                    // point button
                    showPointButton = true;
    
                    // object button
                    if(type == VectorLayerType.LINE || type == VectorLayerType.POLYGON)
                    {
//                        showAutoButton = true;
                        if (selectedVectorLayer.haveOpenedRecordObject())
                        {
                            showObjectButton = true;
                        }
                    }
                }
            }
            
            //run automatic recording
//            if(locationWorker.isRunAutoRecord())
//            {
//                showAutoButton = true;
//            }
        }
        
        if(showObjectButton)
        {
            mButtonRecordEndObject.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRecordEndObject.setVisibility(View.GONE);
        }
        
        if(showPointButton)
        {
            mButtonRecordPoint.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRecordPoint.setVisibility(View.GONE);
        }
        
        if(showAutoButton)
        {
//            mButtonRecordAuto.setVisibility(View.VISIBLE);
//            if(locationWorker.isRunAutoRecord())
//            {
//                mButtonRecordAuto.setText(R.string.record_auto_stop);
//            }
//            else
//            {
//                mButtonRecordAuto.setText(R.string.record_auto_start);
//            }
        }
        else
        {
            mButtonRecordAuto.setVisibility(View.GONE);
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
        mButtonRecordAuto = (Button)myView.findViewById(R.id.button_record_auto);
        mButtonRecordAuto.setOnClickListener(autoRecordObjectHandler);
        mButtonRecordEndObject = (Button)myView.findViewById(R.id.button_record_end_object);
        mButtonRecordEndObject.setOnClickListener(endObjectHandler);
        mButtonRecordPoint = (Button)myView.findViewById(R.id.button_record_point);
        mButtonRecordPoint.setOnClickListener(addPointHandler);
        
        // Map view state
        map = (MapView) myView.findViewById(R.id.map);
        mLayerManager.loadLayers((Context)mMainActivity, map);
        
        return myView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            // map view
            map.setData(savedInstanceState.getSerializable(MAP_VIEW_DATA));
            // layers data
            mLayerManager.setData((ArrayList<AbstractLayerData>)savedInstanceState.getSerializable(LAYERS_DATA));
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // Map view state
        outState.putSerializable(MAP_VIEW_DATA, map.getData());
        // Layers data
        outState.putSerializable(LAYERS_DATA, mLayerManager.getData());
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        mMainActivity = (MainActivity)activity;
    }
    
    // private methods ========================================================
    
    // handlers ===============================================================
    
    /**
     * add point
     */
    View.OnClickListener addPointHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
            Coordinate location = map.getLocation();
            if(location == null)
            {
                Toast.makeText(mMainActivity, R.string.location_fix_error, Toast.LENGTH_LONG).show();
                return;
            }            
            
            selectedLayer.addPoint(location, mLayerManager.getSrid());
            if(selectedLayer.getType() == VectorLayerType.POINT)
            {
                selectedLayer.endObject();
            }
            
            changeRecordButtons();
            map.invalidate();
        }        
    };
    
    /**
     * end object
     */
    View.OnClickListener endObjectHandler = new View.OnClickListener()
    {
        @Override
        public synchronized void onClick(View v)
        {
            VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();

            try
            {
                selectedLayer.endObject();
                
//                LocationWorker locationWorker = mMainActivity.getLocationWorker();
//                if(locationWorker.equals(mAutoRecordLayer) && locationWorker.isRunAutoRecord())
//                {
//                    locationWorker.setRunAutoRecord(false);
//                }
            }
            catch (CreateObjectException e)
            {
                Toast.makeText(mMainActivity, R.string.end_object_error, Toast.LENGTH_LONG).show();
            }
            
            changeRecordButtons();
            map.invalidate();
        }        
    };

    /**
     * start/stop automatic record points
     */
    View.OnClickListener autoRecordObjectHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
//            LocationWorker locationWorker = mMainActivity.getLocationWorker();
//            boolean runAutoRecord = !locationWorker.isRunAutoRecord();
//            locationWorker.setRunAutoRecord(runAutoRecord);
//            
//            if(runAutoRecord)
//            {
//                mAutoRecordLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
//            }
//            else
//            {
//                mAutoRecordLayer = null;
//            }
//            
//            changeRecordButtons();
        }        
    };
    
    // classes =================================================================
}