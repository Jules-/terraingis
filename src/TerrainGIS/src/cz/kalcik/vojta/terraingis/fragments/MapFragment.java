package cz.kalcik.vojta.terraingis.fragments;


import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.components.LonLatFormat;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.dialogs.InsertAttributesDialog;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer.AbstractLayerData;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;
import cz.kalcik.vojta.terraingis.location.AutoRecordService;
import cz.kalcik.vojta.terraingis.location.AutoRecordServiceConnection;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.MainActivity.ActivityMode;
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
    private static String MAP_FRAGMENT_DATA = "cz.kalcik.vojta.terraingis.MapFragmentData";
    
    // properties =========================================================
    public static class MapFragmentData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public boolean isRunAutoRecord;
        public String autoRecordLayerString;

        public MapFragmentData(boolean isRunAutoRecord)
        {
            this.isRunAutoRecord = isRunAutoRecord;
        }
    }
    
    private MapView mMap;
    private LayerManager mLayerManager = LayerManager.getInstance();
    private MainActivity mMainActivity;
    private VectorLayer mAutoRecordLayer = null;
    private Intent mServiceIntent;
    private AutoRecordServiceConnection mServiceConnection = new AutoRecordServiceConnection(this);
    private MapFragmentData data = new MapFragmentData(false);
    private Navigator mNavigator = Navigator.getInstance();
    
    private ImageButton mButtonRecordAuto;
    private ImageButton mButtonRecordEndObject;
    private ImageButton mButtonRecordPoint;
    private TextView mCoordinatesLocationText;
    private TextView mCoordinatesAddPointText;

    private Coordinate mLocationM = new Coordinate(0,0); // location from GPS or Wi-Fi
    private Coordinate mAddPointLocationM = null; // location of point for insert point to object
    private boolean mLocationValid = false;

    // public methods =====================================================
    /**
     * set visibility of tools in map
     */
    public void setMapTools()
    {
        setRecordButtons();
        setVisibilitiCoordinatesTexts();
    }
    
    /**
     * add recorded points
     * @param points
     */
    public void recordPointsAuto(ArrayList<Coordinate> points)
    {
        if(mAutoRecordLayer != null)
        {
            mAutoRecordLayer.addPoints(points, SpatiaLiteIO.EPSG_LONLAT);
        }
        
        setMapTools();
        mMap.invalidate();
    }

    /**
     * record one point
     * @param point
     */
    public void recordPointAuto(Coordinate point)
    {
        if(mAutoRecordLayer != null)
        {
            recordPoint(point, mAutoRecordLayer, SpatiaLiteIO.EPSG_LONLAT);
        }
    }
    
    /**
     * change position by location 
     * @return true if success
     */
    public synchronized void showLocation()
    {
        if(mLocationValid)
        {
            mNavigator.setPositionM(mLocationM.x, mLocationM.y);
            
            mMap.invalidate();
        }
        else
        {
            throw new RuntimeException(getString(R.string.location_fix_error));
        }
    }

    /**
     * start location service
     */
    public void startLocation()
    {
        setMapTools();
    }
    
    /**
     * stop location service
     */
    public void stopLocation()
    {
        setLocationValid(false);
        setMapTools();
        
        mMap.invalidate();
    }
    
    /**
     * set location from location services
     * @param location
     */
    public synchronized void setLonLatLocation(Coordinate location)
    {
        mLocationM = mLayerManager.lonLatWGS84ToM(location);
        mLocationValid = true;
        
        setCoordinatesLocationText();
        
        mMap.invalidate();
    }
    
    /**
     * set coordinates of clicked point
     * @param location
     */
    public void setCoordinatesAddPointM(Coordinate location)
    {
        mAddPointLocationM = location;
        
        setCoordinatesAddPointText();
        mMap.invalidate();
    }
    // getter, setter =====================================================
    
    /**
     * @return map
     */
    public MapView getMap()
    {
        return mMap;
    }

    /**
     * set if location is valid
     * @param value
     */
    public void setLocationValid(boolean value)
    {
        mLocationValid = value;
    }

    /**
     * if location is valid return location coordinates in meters
     * else return null
     * @return
     */
    public synchronized Coordinate getCoordinatesLocation()
    {
        if(mLocationValid)
        {
            return (Coordinate) mLocationM.clone();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return add point coordinates
     */
    public Coordinate getCoordinatesAddPoint()
    {
        return mAddPointLocationM;
    }
    // on methods =========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.map_layout, container, false);
        
        // record buttons
        mButtonRecordAuto = (ImageButton)myView.findViewById(R.id.button_record_auto);
        mButtonRecordAuto.setOnClickListener(autoRecordObjectHandler);
        mButtonRecordEndObject = (ImageButton)myView.findViewById(R.id.button_record_end_object);
        mButtonRecordEndObject.setOnClickListener(endObjectHandler);
        mButtonRecordPoint = (ImageButton)myView.findViewById(R.id.button_record_point);
        mButtonRecordPoint.setOnClickListener(addPointHandler);
        
        // coordinate text
        mCoordinatesLocationText = (TextView)myView.findViewById(R.id.textView_coordinates);
        mCoordinatesAddPointText = (TextView)myView.findViewById(R.id.textView_add_point_coordinates);
        
        // Map view state
        mMap = (MapView) myView.findViewById(R.id.map);
        mLayerManager.loadLayers((Context)mMainActivity, mMap);
        
        return myView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            // map view
            mMap.setData(savedInstanceState.getSerializable(MAP_VIEW_DATA));
            // layers data
            mLayerManager.setData((ArrayList<AbstractLayerData>)savedInstanceState.getSerializable(LAYERS_DATA));
            restoreData(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        super.onSaveInstanceState(outState);

        // Map view state
        outState.putSerializable(MAP_VIEW_DATA, mMap.getData());
        // Layers data
        outState.putSerializable(LAYERS_DATA, mLayerManager.getData());
        saveData(outState);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if(data.isRunAutoRecord)
        {
            bindAutoRecordService();
        }
        
        // disable old location
        mLocationValid = false;
        mMap.invalidate();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        
        if(data.isRunAutoRecord)
        {
            unbindAutoRecordService();
        }        
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        mMainActivity = (MainActivity)activity;
        mServiceIntent = new Intent(mMainActivity, AutoRecordService.class);
    }
    
    // private methods ========================================================

    /**
     * add point to layer
     * @param location
     */
    private void recordPoint(Coordinate location, VectorLayer layer, int srid)
    {
        layer.addPoint(location, srid);
        if(layer.getType() == VectorLayerType.POINT)
        {
            endObject(layer);
        }
        
        setMapTools();
        mMap.invalidate();
    }

    /**
     * start automatic recording of points
     */
    private void startAutoRecord()
    {
        mAutoRecordLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
        
        mMainActivity.startService(mServiceIntent);
        bindAutoRecordService();
        data.isRunAutoRecord = true;
        setMapTools();
    }
    
    /**
     * stop automatic recording of points
     */
    private void stopAutoRecord()
    {
        mMainActivity.stopService(mServiceIntent);
        mAutoRecordLayer = null;
        data.isRunAutoRecord = false;
        setMapTools();
    }
    
    /**
     * bind to auto record serice
     */
    private void bindAutoRecordService()
    {
        mMainActivity.bindService(mServiceIntent, mServiceConnection, 0);       
    }
    
    /**
     * unbind to auto record serice
     */
    private void unbindAutoRecordService()
    {
        mServiceConnection.unsetMapFragment();
        mMainActivity.unbindService(mServiceConnection);
    }
    
    /**
     * save data of MapFragment
     * @param savedInstanceState
     */
    private void saveData(Bundle outState)
    {
        // autorecordlayer
        if(mAutoRecordLayer != null)
        {
            data.autoRecordLayerString = mAutoRecordLayer.toString();
        }
        else
        {
            data.autoRecordLayerString = null;
        }
        
        outState.putSerializable(MAP_FRAGMENT_DATA, data);
    }
    
    /**
     * restore saved data
     * @param outState
     */
    private void restoreData(Bundle savedInstanceState)
    {
        data = (MapFragmentData)savedInstanceState.getSerializable(MAP_FRAGMENT_DATA);
        if(data.autoRecordLayerString != null)
        {
            mAutoRecordLayer = mLayerManager.getLayerByName(data.autoRecordLayerString);
        }
    }
    
    /**
     * end recorded object
     */
    private void endObject(VectorLayer layer)
    {
        InsertAttributesDialog dialog = new InsertAttributesDialog();
        dialog.setLayer(layer);
        mMainActivity.showDialog(dialog);
    }
    
    /**
     * @return text of current location
     */
    private synchronized String getCoordinatesLocationText()
    {
        if(mLocationValid)
        {
            Coordinate location = mLayerManager.mToLonLatWGS84(mLocationM);
            return LonLatFormat.getFormatDM(location);
        }
        else
        {
            return getString(R.string.location_fix_error);
        }
    }

    /**
     * @return text of current location
     */
    private String getCoordinatesAddPointText()
    {
        if(mAddPointLocationM != null)
        {
            Coordinate location = mLayerManager.mToLonLatWGS84(mAddPointLocationM);
            return LonLatFormat.getFormatDM(location);
        }
        else
        {
            return getString(R.string.not_selected_position);
        }
    }
    
    /**
     * set text of location coordinates
     */
    private void setCoordinatesLocationText()
    {        
        mCoordinatesLocationText.setText(getCoordinatesLocationText());
    }

    /**
     * set text of location coordinates
     */
    private void setCoordinatesAddPointText()
    {        
        mCoordinatesAddPointText.setText(getCoordinatesAddPointText());
    }
    
    /**
     * set visibility of record buttons
     */
    private void setRecordButtons()
    {
        boolean showObjectButton = false;
        boolean showPointButton = false;
        boolean showAutoButton = false;
        
        
        ActivityMode mode = mMainActivity.getActivityMode();
        AbstractLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayer();
        // recording
        if(mode == ActivityMode.RECORD)
        {
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
            if(data.isRunAutoRecord)
            {
                if(selectedLayer instanceof VectorLayer)
                {
                    showAutoButton = true;
                }
            }
        }
        else if(mode == ActivityMode.EDIT)
        {
            if(mMainActivity.isAddPointMode())
            {
                showPointButton = true;
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
            if(data.isRunAutoRecord)
            {
                mButtonRecordAuto.setContentDescription(getString(R.string.record_auto_stop));
                mButtonRecordAuto.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_auto_record_on));
            }
            else
            {
                mButtonRecordAuto.setContentDescription(getString(R.string.record_auto_start));
                mButtonRecordAuto.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_auto_record_off));
            }
        }
        else
        {
            mButtonRecordAuto.setVisibility(View.GONE);
        }
    }
    
    /**
     * set visibiliti of coordinate texts
     */
    private void setVisibilitiCoordinatesTexts()
    {       
        // coordinates location
        if(mMainActivity.getLocationWorker().isRunLocation())
        {
            if(mCoordinatesLocationText.getVisibility() != View.VISIBLE)
            {
                mCoordinatesLocationText.setVisibility(View.VISIBLE);
            }
            
            setCoordinatesLocationText();            
        }
        else
        {
            mCoordinatesLocationText.setVisibility(View.GONE);            
        }
        
        // coordinates add point
        if(mMainActivity.getActivityMode() == ActivityMode.EDIT &&
                mMainActivity.isAddPointMode())
        {
            if(mCoordinatesAddPointText.getVisibility() != View.VISIBLE)
            {
                mCoordinatesAddPointText.setVisibility(View.VISIBLE);
            }
            
            setCoordinatesAddPointText();            
        }
        else
        {
            mCoordinatesAddPointText.setVisibility(View.GONE);            
        }        
    }
    // handlers ===============================================================
    
    /**
     * add point
     */
    View.OnClickListener addPointHandler = new View.OnClickListener()
    {
        @Override
        public synchronized void onClick(View v)
        {
            VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();

            if(mMainActivity.getActivityMode() == ActivityMode.RECORD)
            {
                if(!mLocationValid)
                {
                    Toast.makeText(mMainActivity, R.string.location_fix_error, Toast.LENGTH_LONG).show();
                    return;
                }            
                
                recordPoint(mLocationM, selectedLayer, mLayerManager.getSrid());
            }
            else if(mMainActivity.getActivityMode() == ActivityMode.EDIT &&
                    mMainActivity.isAddPointMode())
            {
                if(mAddPointLocationM == null)
                {
                    Toast.makeText(mMainActivity, R.string.not_selected_position, Toast.LENGTH_LONG).show();
                    return;                    
                }
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
                endObject(selectedLayer);
                
                if(selectedLayer.equals(mAutoRecordLayer) && data.isRunAutoRecord)
                {
                    stopAutoRecord();
                }
            }
            catch (CreateObjectException e)
            {
                Toast.makeText(mMainActivity, R.string.end_object_error, Toast.LENGTH_LONG).show();
            }
            
            setMapTools();
            mMap.invalidate();
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
            if(data.isRunAutoRecord)
            {
                stopAutoRecord();
            }
            else
            {
                startAutoRecord();
            }
            
            setMapTools();
        }        
    };
    
    // classes =================================================================
}