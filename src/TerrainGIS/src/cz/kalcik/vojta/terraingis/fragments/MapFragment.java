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
    private static String ADD_POINT_LAYER = "cz.kalcik.vojta.terraingis.AddPointLayer";
    private static String AUTO_RECORD_LAYER = "cz.kalcik.vojta.terraingis.AutoRecordLayer";
    private static int TIMER_TIME = 3000;
    
    // properties =========================================================
    private MapView map;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private MainActivity mMainActivity;
    private Button mButtonRecordAuto;
    private Button mButtonRecordEndObject;
    private Button mButtonRecordPoint;
    private VectorLayer mAddPointLayer = null;
    private VectorLayer mAutoRecordLayer = null;
    private Timer timer;
    

    // public methods =====================================================
    /**
     * set visibility of record buttons
     */
    public void changeRecordButtons()
    {
        boolean showObjectButton = false;
        boolean showPointButton = false;
        boolean showAutoButton = false;
        
        LocationWorker locationWorker = mMainActivity.getLocationWorker();
        
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
                        showAutoButton = true;
                        if (selectedVectorLayer.haveOpenedRecordObject())
                        {
                            showObjectButton = true;
                        }
                    }
                }
            }
            
            //run automatic recording
            if(locationWorker.isRunAutoRecord())
            {
                showAutoButton = true;
            }
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
            mButtonRecordAuto.setVisibility(View.VISIBLE);
            if(locationWorker.isRunAutoRecord())
            {
                mButtonRecordAuto.setText(R.string.record_auto_stop);
            }
            else
            {
                mButtonRecordAuto.setText(R.string.record_auto_start);
            }
        }
        else
        {
            mButtonRecordAuto.setVisibility(View.GONE);
        }
    }
    
    /**
     * add point to selected layer
     * @param location
     */
    public void recordPoint(Coordinate location, VectorLayer layer)
    {
        layer.addPoint(location);
        if(layer.getType() == VectorLayerType.POINT)
        {
            layer.endObject();
        }
        
        changeRecordButtons();
        map.invalidate();
    }
    
    /**
     * record manual one point
     * @param location
     */
    public void recordPointAdd(Coordinate location)
    {
        VectorLayer layer = mAddPointLayer;
        if(layer != null)
        {
            recordPoint(location, layer);
        }
    }

    /**
     * record automatic one point
     * @param location
     */
    public void recordPointAuto(Coordinate location)
    {
        VectorLayer layer = mAutoRecordLayer;
        if(layer != null)
        {
            recordPoint(location, layer);
        }
    }

    /**
     * add recorded points
     * @param points
     */
    public void recordPointsAuto(ArrayList<Coordinate> points)
    {
        VectorLayer layer = mAutoRecordLayer;
        if(layer != null)
        {
            layer.addPoints(points);
        }
        
        changeRecordButtons();
        map.invalidate();
    }
    
    /**
     * cancel timer for record point
     */
    public void cancelTimer()
    {
        if(timer != null)
        {
            timer.cancel();
            timer.purge();
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
            // recording layers
            String autoRecordString = savedInstanceState.getString(AUTO_RECORD_LAYER);
            mAutoRecordLayer = autoRecordString.isEmpty() ? null : mLayerManager.getLayerByName(autoRecordString);
            String addPointString = savedInstanceState.getString(ADD_POINT_LAYER);
            mAddPointLayer = addPointString.isEmpty() ? null : mLayerManager.getLayerByName(addPointString);
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // Map view state
        outState.putSerializable(MAP_VIEW_DATA, map.getData());
        // Layers data
        outState.putSerializable(LAYERS_DATA, mLayerManager.getData());
        // recording layers
        String autoRecordString = mAutoRecordLayer != null ? mAutoRecordLayer.toString() : "" ;
        outState.putString(AUTO_RECORD_LAYER, autoRecordString);
        String addPointString = mAddPointLayer != null ? mAddPointLayer.toString() : "" ;
        outState.putString(ADD_POINT_LAYER, addPointString);
        
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
            mAddPointLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
            if(!mMainActivity.getLocationWorker().recordPoint())
            {
                Toast.makeText(mMainActivity, R.string.record_point_busy_error, Toast.LENGTH_LONG).show();
            }
            // run check time
            else
            {
                cancelTimer();
         
                timer = new Timer();
                timer.schedule(new RecordPointFail(), TIMER_TIME);                
            }
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
                
                LocationWorker locationWorker = mMainActivity.getLocationWorker();
                if(locationWorker.equals(mAutoRecordLayer) && locationWorker.isRunAutoRecord())
                {
                    locationWorker.setRunAutoRecord(false);
                }
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
            LocationWorker locationWorker = mMainActivity.getLocationWorker();
            boolean runAutoRecord = !locationWorker.isRunAutoRecord();
            locationWorker.setRunAutoRecord(runAutoRecord);
            
            if(runAutoRecord)
            {
                mAutoRecordLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
            }
            else
            {
                mAutoRecordLayer = null;
            }
            
            changeRecordButtons();
        }        
    };
    
    // classes =================================================================
    /**
     * run when fail getting current point
     * @author jules
     *
     */
    class RecordPointFail extends TimerTask
    {        
        private RecordPointFailRunnable recordPointFailRunnable = new RecordPointFailRunnable();

        public void run()
        {
            mMainActivity.runOnUiThread(recordPointFailRunnable);
        }
    }
    
    /**
     * show message for fail getting current point
     * @author jules
     *
     */
    class RecordPointFailRunnable implements Runnable
    {
        public void run()
        {
            mMainActivity.getLocationWorker().stopRecordingPoint();
            Toast.makeText(mMainActivity, R.string.record_point_error, Toast.LENGTH_LONG).show();
        }
    };
}