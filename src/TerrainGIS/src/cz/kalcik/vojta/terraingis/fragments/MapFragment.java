package cz.kalcik.vojta.terraingis.fragments;


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
import cz.kalcik.vojta.terraingis.components.LocationWorker.LocationTask;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
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
    private static int TIMER_TIME = 3000;
    
    // properties =========================================================
    private MapView map;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private MainActivity mMainActivity;
    private Button mButtonRecordAuto;
    private Button mButtonRecordEndObject;
    private Button mButtonRecordPoint;
    private VectorLayer mAddPointSelectedLayer;
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
            LocationWorker locationWorker = mMainActivity.getLocationWorker();
            
            mButtonRecordAuto.setVisibility(View.VISIBLE);
            if(locationWorker.getCurrentTask() == LocationTask.AUTO_RECORD)
            {
                mButtonRecordAuto.setText(R.string.record_auto_stop);
            }
            else if(locationWorker.getCurrentTask() == LocationTask.RECORD_POINT)
            {
                if(locationWorker.getNextTask() == LocationTask.AUTO_RECORD)
                {
                    mButtonRecordAuto.setText(R.string.record_auto_stop);
                }
                else if(locationWorker.getNextTask() == LocationTask.IDLE)
                {
                    mButtonRecordAuto.setText(R.string.record_auto_start);
                }
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
    
    public void recordPointAdd(Coordinate location)
    {
        VectorLayer layer = mAddPointSelectedLayer;
        recordPoint(location, layer);
    }

    public void recordPointAuto(Coordinate location)
    {
        VectorLayer layer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
        recordPoint(location, layer);
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
    
    // handlers ===============================================================
    
    /**
     * add point
     */
    View.OnClickListener addPointHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mAddPointSelectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
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
                if(locationWorker.getCurrentTask() == LocationTask.AUTO_RECORD)
                {
                    locationWorker.setCurrentTask(LocationTask.IDLE);
                }
                else if(locationWorker.getCurrentTask() == LocationTask.RECORD_POINT)
                {
                    cancelTimer();
                    locationWorker.setNextTask(LocationTask.IDLE);
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
            if(locationWorker.getCurrentTask() == LocationTask.AUTO_RECORD)
            {
                locationWorker.setCurrentTask(LocationTask.IDLE);
            }
            else if(locationWorker.getCurrentTask() == LocationTask.IDLE)
            {
                locationWorker.setCurrentTask(LocationTask.AUTO_RECORD);
            }
            else if(locationWorker.getCurrentTask() == LocationTask.RECORD_POINT)
            {
                if(locationWorker.getNextTask() == LocationTask.AUTO_RECORD)
                {
                    locationWorker.setNextTask(LocationTask.IDLE);
                }
                else if(locationWorker.getNextTask() == LocationTask.IDLE)
                {
                    locationWorker.setNextTask(LocationTask.AUTO_RECORD);
                }
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
            mMainActivity.getLocationWorker().setCurrentToNextLocationTask();
            Toast.makeText(mMainActivity, R.string.record_point_error, Toast.LENGTH_LONG).show();
        }
    };
}