package cz.kalcik.vojta.terraingis.fragments;


import java.io.Serializable;
import java.util.ArrayList;

import jsqlite.Exception;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.LonLatFormat;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.dialogs.InsertAttributesDialog;
import cz.kalcik.vojta.terraingis.dialogs.RemoveEditedObjectDialog;
import cz.kalcik.vojta.terraingis.dialogs.RemoveObjectDialog;
import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AbstractLayer.AbstractLayerData;
import cz.kalcik.vojta.terraingis.layer.VectorLayerType;
import cz.kalcik.vojta.terraingis.location.AutoRecordService;
import cz.kalcik.vojta.terraingis.location.AutoRecordServiceConnection;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.MainActivity.ActivityMode;
import cz.kalcik.vojta.terraingis.MainActivity.AddPointMode;
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
    private ImageButton mButtonAddPoint;
    private ImageButton mButtonBack;
    private ImageButton mButtonRemove;
    private ImageButton mButtonMovePoint;
    
    private TextView mCoordinatesLocationText;
    private TextView mCoordinatesAddPointText;

    private Coordinate mLocationM = new Coordinate(0,0); // location from GPS or Wi-Fi
    private Coordinate mManualLocationM = null; // location of point for insert point to object
    private boolean mLocationValid = false;

    // public methods =====================================================
    /**
     * set visibility of tools in map
     */
    public void setMapTools()
    {
        setMapButtons();
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
            try
            {
                mAutoRecordLayer.addPointsToEdited(points, SpatiaLiteIO.EPSG_LONLAT);
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return;
            }
            catch (ParseException e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return;
            }
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
            recordInsertPoint(point, mAutoRecordLayer, SpatiaLiteIO.EPSG_LONLAT, true);
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
        try
        {
            mLocationM = mLayerManager.lonLatWGS84ToM(location);
        }
        catch (Exception e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
            return;
        }
        catch (ParseException e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
            return;
        }
        
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
        mManualLocationM = location;
        
        setCoordinatesAddPointText();
        mMap.invalidate();
    }
    
    /**
     * end recorded object
     */
    public void endNewObject(VectorLayer layer)
    {
        InsertAttributesDialog dialog = new InsertAttributesDialog();
        dialog.setLayer(layer);
        mMainActivity.showDialog(dialog);
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
        return mManualLocationM;
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
        mButtonRecordEndObject.setOnClickListener(endRecordedObjectHandler);
        mButtonAddPoint = (ImageButton)myView.findViewById(R.id.button_add_point);
        mButtonAddPoint.setOnClickListener(addPointHandler);
        mButtonMovePoint = (ImageButton)myView.findViewById(R.id.button_move_point);
        mButtonMovePoint.setOnClickListener(movePointHandler);
        mButtonBack = (ImageButton)myView.findViewById(R.id.button_back);
        mButtonBack.setOnClickListener(backHandler);
        mButtonRemove = (ImageButton)myView.findViewById(R.id.button_remove);
        mButtonRemove.setOnClickListener(removeHandler);
        
        // coordinate text
        mCoordinatesLocationText = (TextView)myView.findViewById(R.id.textView_coordinates);
        mCoordinatesAddPointText = (TextView)myView.findViewById(R.id.textView_add_point_coordinates);
        
        // Map view state
        mMap = (MapView) myView.findViewById(R.id.map);
        mLayerManager.loadLayers(mMainActivity, this, mMap);
        
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
    private void recordInsertPoint(Coordinate location, VectorLayer layer, int srid, boolean addToEnd)
    {
        try
        {
            layer.addPointToEdited(location, srid, addToEnd);
        }
        catch (Exception e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
            return;
        }
        catch (ParseException e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(layer.getType() == VectorLayerType.POINT)
        {
            endNewObject(layer);
        }
    }
    
    /**
     * start automatic recording of points
     */
    private void startAutoRecord()
    {
        if(!mMainActivity.getLocationWorker().hasGPSDevice())
        {
            Toast.makeText(getActivity(), R.string.device_no_gps_error, Toast.LENGTH_LONG).show();
            return;
        }        
        
        mAutoRecordLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
        
        if(mAutoRecordLayer != null)
        {
            mMainActivity.startService(mServiceIntent);
            bindAutoRecordService();
            data.isRunAutoRecord = true;
            setMapTools();
        }
    }
    
    /**
     * stop automatic recording of points
     */
    public void stopAutoRecord()
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
     * @return text of current location
     */
    private synchronized String getCoordinatesLocationText()
    {
        if(mLocationValid)
        {
            try
            {
                Coordinate location = mLayerManager.mToLonLatWGS84(mLocationM);
                return LonLatFormat.getFormatDM(location);
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return getString(R.string.location_fix_error);
            }
            catch (ParseException e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return getString(R.string.location_fix_error);
            }
            
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
        if(mManualLocationM != null)
        {
            try
            {
                Coordinate location = mLayerManager.mToLonLatWGS84(mManualLocationM);
                return LonLatFormat.getFormatDM(location);
            }
            catch (Exception e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return getString(R.string.not_selected_position);
            }
            catch (ParseException e)
            {
                Toast.makeText(mMainActivity, R.string.database_error,
                        Toast.LENGTH_LONG).show();
                return getString(R.string.not_selected_position);
            }
            
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
    private void setMapButtons()
    {
        boolean showEndObjectButton = false;
        boolean showAutoButton = false;
        boolean showAddPointButton = false;
        boolean showMovePointButton = false;
        boolean showBackButton = false;
        boolean showRemoveButton = false;
        
        
        ActivityMode mode = mMainActivity.getActivityMode();
        VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();

        // is selected layer
        if(selectedLayer != null)
        {
            VectorLayerType type = selectedLayer.getType();

            // editing
            if(mode == ActivityMode.EDIT)
            {
                boolean isRunLocation = mMainActivity.getLocationWorker().isRunLocation();
                
                if(isRunLocation || mMainActivity.getAddPointMode() != AddPointMode.NONE)
                {
                    // add point button
                    if(!(type == VectorLayerType.POINT && selectedLayer.hasOpenedEditedObject()))
                    {
                        showAddPointButton = true;
                    }
        
                    // move point button
                    if(selectedLayer.hasEditedObjectSelectedVertex())
                    {
                        showMovePointButton = true;
                    }
                }
                               
                //run automatic recording
                if(data.isRunAutoRecord)
                {
                    showAutoButton = true;
                }
                
                if(type == VectorLayerType.LINE || type == VectorLayerType.POLYGON)
                {
                    // end, auto record object button
                    if(isRunLocation)
                    {
                        showAutoButton = true;
                    }
                }
                
                if (selectedLayer.hasOpenedEditedObject())
                {
                    // end object button
                    if(selectedLayer.hasEditedObjectEnoughPoints())
                    {
                        showEndObjectButton = true;
                    }
                    
                    // back button
                    if(!selectedLayer.isEditedObjectNew())
                    {
                        showBackButton = true;
                    }
                    
                    // remove butoon
                    showRemoveButton = true;
                }
            }
        }
        
        // end object button
        if(showEndObjectButton)
        {
            mButtonRecordEndObject.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRecordEndObject.setVisibility(View.GONE);
        }
        
        // add point button
        if(showAddPointButton)
        {
            mButtonAddPoint.setVisibility(View.VISIBLE);
            
            if(mMainActivity.getAddPointMode() == AddPointMode.TOPOLOGY_POINT)
            {
                mButtonAddPoint.setContentDescription(getString(R.string.button_record_topology_point));
                mButtonAddPoint.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_add_topology_point));
            }
            else
            {
                mButtonAddPoint.setContentDescription(getString(R.string.button_add_point));
                mButtonAddPoint.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_add_point));                
            }
        }
        else
        {
            mButtonAddPoint.setVisibility(View.GONE);
        }

        // move point button
        if(showMovePointButton)
        {
            mButtonMovePoint.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonMovePoint.setVisibility(View.GONE);
        }
        
        // auto record button
        if(showAutoButton)
        {
            mButtonRecordAuto.setVisibility(View.VISIBLE);
            if(data.isRunAutoRecord)
            {
                mButtonRecordAuto.setContentDescription(getString(R.string.button_record_auto_stop));
                mButtonRecordAuto.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_auto_record_on));
            }
            else
            {
                mButtonRecordAuto.setContentDescription(getString(R.string.button_record_auto_start));
                mButtonRecordAuto.setImageDrawable(
                        getResources().getDrawable(R.drawable.button_auto_record_off));
            }
        }
        else
        {
            mButtonRecordAuto.setVisibility(View.GONE);
        }
        
        // back button
        if(showBackButton)
        {
            mButtonBack.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonBack.setVisibility(View.GONE);
        }
        
        // remove button
        if(showRemoveButton)
        {
            mButtonRemove.setVisibility(View.VISIBLE);
        }
        else
        {
            mButtonRemove.setVisibility(View.GONE);
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
        if(mMainActivity.getAddPointMode() != AddPointMode.NONE)
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
    
    /**
     * update existed object
     * @param layer
     */
    private void endSavedObject(VectorLayer layer)
    {
        try
        {
            layer.updateEditedObject();
            mMap.invalidate();
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(mMainActivity, R.string.database_error,
                    Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * @return coordinates for point by application mode
     */
    private Coordinate getCoordinateByMode()
    {
        if(mMainActivity.getAddPointMode() != AddPointMode.NONE)
        {
            if(mManualLocationM == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_position, Toast.LENGTH_LONG).show();
                return null;                    
            }
            
            return mManualLocationM;
        }
        else
        {
            // new point from GPS
            if(!mLocationValid)
            {
                Toast.makeText(mMainActivity, R.string.location_fix_error, Toast.LENGTH_LONG).show();
                return null;
            }
             
            return mLocationM;
        }        
    }
    
    /**
     * stop automatic recording if is recorded object in layer
     * @param layer
     */
    private void stopAutoRecordIfLayer(VectorLayer layer)
    {
        if(layer.equals(mAutoRecordLayer) && data.isRunAutoRecord)
        {
            stopAutoRecord();
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
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                Coordinate coordinates = getCoordinateByMode();
                
                if(coordinates != null)
                {
                    recordInsertPoint(coordinates, selectedLayer, mLayerManager.getSrid(), false);
                
                    setMapTools();
                    mMap.invalidate();
                }
            }
        }        
    };

    /**
     * move point
     */
    View.OnClickListener movePointHandler = new View.OnClickListener()
    {
        @Override
        public synchronized void onClick(View v)
        {
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                Coordinate coordinates = getCoordinateByMode();
                
                if(coordinates != null)
                {
                    try
                    {
                        selectedLayer.setPositionSelectedVertex((Coordinate) coordinates.clone());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(mMainActivity, R.string.end_object_error, Toast.LENGTH_LONG).show();
                    }
                    
                    setMapTools();
                    mMap.invalidate();
                }
            }
        }        
    };
    
    /**
     * end object
     */
    View.OnClickListener endRecordedObjectHandler = new View.OnClickListener()
    {
        @Override
        public synchronized void onClick(View v)
        {
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                try
                {                   
                    stopAutoRecordIfLayer(selectedLayer);
                    
                    if(selectedLayer.isEditedObjectNew())
                    {
                        endNewObject(selectedLayer);
                    }
                    else
                    {
                        endSavedObject(selectedLayer);
                    }
                }
                catch (CreateObjectException e)
                {
                    Toast.makeText(mMainActivity, R.string.end_object_error, Toast.LENGTH_LONG).show();
                }
                
                setMapTools();
                mMap.invalidate();
            }
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

    /**
     * return state of object back
     */
    View.OnClickListener backHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                selectedLayer.cancelNotSavedEditedChanges();

                setMapTools();
                mMap.invalidate();         
            }
        }        
    };
    
    /**
     * remove object or point
     */
    View.OnClickListener removeHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            VectorLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
            if(selectedLayer != null)
            {
                try
                {
                    if(!selectedLayer.hasEditedObjectSelectedVertex() ||
                            selectedLayer.editedObjectHasLastSelectedVertex())
                    {
                        stopAutoRecordIfLayer(selectedLayer);
                        
                        RemoveEditedObjectDialog dialog = new RemoveEditedObjectDialog();
                        dialog.setMessage(getResources().getString(R.string.confirm_remove_object_message));
                        mMainActivity.showDialog(dialog);                        
                    }
                    else
                    {
                        selectedLayer.removeSelectedEdited();
                        mMainActivity.getAttributesFragment().reload();
                        
                        setMapTools();
                        mMap.invalidate();
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }        
    };    
    // classes =================================================================
}