/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.components.TileCache;
import cz.kalcik.vojta.terraingis.dialogs.ExitDialog;
import cz.kalcik.vojta.terraingis.fragments.AttributesFragment;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.location.LocationWorker;
import cz.kalcik.vojta.terraingis.view.MapView;
import cz.kalcik.vojta.terraingis.R;

public class MainActivity extends AbstractActivity
{
    // constants ==========================================================
    private static final String LOCATION_WORKER_DATA = "LocationWorkerData";
    private static final String MAIN_ACTIVITY_DATA = "MainActivityData";
    private static final String SHOWN_LAYERS = "ShownLayers";
    private static final int SMALL_WIDTH_BOUNDARY_DPI = 450; // min width for hidding icons in actionbar
    
    public enum ActivityMode {EXPLORE, EDIT};
    public enum AddPointMode {NONE, ANY_POINT, TOPOLOGY_POINT};
    // properties =========================================================
    public static class MainActivityData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public ActivityMode activityMode;
        public AddPointMode addPointMode = AddPointMode.NONE;

        public MainActivityData(ActivityMode activityMode)
        {
            this.activityMode = activityMode;
        }
    }
    
    private MenuItem mMenuRunLocation;
    private MenuItem mMenuShowLocation;
    private MenuItem mMenuEdit;
    private MenuItem mMenuAddPoint;
    private MenuItem mMenuTopology;
    private MenuItem mMenuSettings;
    
    private Timer timer;
    private LocationWorker mLocationWorker;
    private Settings mSettings = Settings.getInstance();
    private MapFragment mMapFragment;
    private LayersFragment mLayersFragment;
    private AttributesFragment mAttributesFragment;
    private LinearLayout mMapLayout;
    private LinearLayout mPanelLayout;
    private LinearLayout mLayersLayout;
    private LinearLayout mAttributesLayout;
    private View mDividerLayout;
    private MainActivityData data = new MainActivityData(ActivityMode.EXPLORE);
    private ConnectivityManager mConnectivityManager;
    
    private boolean isLoadedMenu = false;
    
    // public methods =====================================================
    
    /**
     * show ActionBar
     */
    public void showActionBar()
    {
        getActionBar().show();
        if(mSettings.isHideActionBar())
        {
            runTimerActionBar();
        }
    }
    
    /**
     * hide layers fragment
     */
    public void hidePanel()
    {
        if(mPanelLayout.isShown())
        {
            mPanelLayout.setVisibility(View.GONE);
            mDividerLayout.setVisibility(View.GONE);
            if(!mMapLayout.isShown())
            {
                mMapLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * show layers fragment
     */
    public void showPanel()
    {
        if(!mPanelLayout.isShown())
        {           
            hideActionBarNow();
            mPanelLayout.setVisibility(View.VISIBLE);
            mDividerLayout.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * @return if can be selected object
     */
    public boolean canSelectObject()
    {
        return data.activityMode == ActivityMode.EXPLORE;
    }
    
    /**
     * @return true if network is available
     */
    public boolean isNetworkAvailable()
    {
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    // getter, setter =====================================================
    
    /**
     * check if LayersFragment is hidden
     * @return
     */
    public boolean isHiddenLayersFragment()
    {
        return !mPanelLayout.isShown();
    }
    
    /**
     * @return activity mode
     */
    public ActivityMode getActivityMode()
    {
        return data.activityMode;
    }
    
    /**
     * @return LayersFragment
     */
    public LayersFragment getLayersFragment()
    {
        return mLayersFragment;
    }

    /**
     * @return AttributesFragment
     */
    public AttributesFragment getAttributesFragment()
    {
        return mAttributesFragment;
    }
    
    /**
     * @return MapFragment
     */
    public MapFragment getMapFragment()
    {
        return mMapFragment;
    }
    
    /**
     * @return layers layout
     */
    public LinearLayout getLayersLayout()
    {
        return mLayersLayout;
    }

    /**
     * @return attributes layout
     */
    public LinearLayout getAttributesLayout()
    {
        return mAttributesLayout;
    }
    
    /**
     * @return the mLocationWorker
     */
    public LocationWorker getLocationWorker()
    {
        return mLocationWorker;
    }
       
    /**
     * @return add point mode
     */
    public AddPointMode getAddPointMode()
    {
        return data.addPointMode;
    }
    
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try
        {
            checkAppDirectory();
        }
        catch (IOException e)
        {
            Toast.makeText(this, R.string.app_directory_create_error, Toast.LENGTH_LONG).show();
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ConvertUnits.setDensity(getResources().getDisplayMetrics().density);

        getActionBar().setHomeButtonEnabled(true);
        
        mMapLayout = (LinearLayout)findViewById(R.id.map_layout);
        mPanelLayout = (LinearLayout)findViewById(R.id.panel_layout);
        mLayersLayout = (LinearLayout)findViewById(R.id.layers_layout);
        mAttributesLayout = (LinearLayout)findViewById(R.id.attributes_layout);
        mDividerLayout = findViewById(R.id.divider_layout);
        
        mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        mLayersFragment = (LayersFragment)getFragmentManager().findFragmentById(R.id.layers_fragment);
        mAttributesFragment = (AttributesFragment)getFragmentManager().findFragmentById(R.id.attributes_fragment);
        
        mLocationWorker = new LocationWorker(this);
        MapView map = mMapFragment.getMap();
        map.setMainActivity(this);
        
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        
        // tile cache

        try
        {
            TileCache.getInstance().open(TILE_CACHE_FILE, getResources(), map);
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Toast.makeText(this, R.string.tilecache_open_error, Toast.LENGTH_LONG).show();
            finish();
        }
        
        if(mSettings.isHideActionBar())
        {
            runTimerActionBar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        
        mMenuRunLocation = menu.findItem(R.id.menu_location);
        mMenuShowLocation = menu.findItem(R.id.menu_show_location);
        mMenuEdit = menu.findItem(R.id.menu_edit);
        mMenuAddPoint = menu.findItem(R.id.menu_add_point);
        mMenuTopology = menu.findItem(R.id.menu_topology);
        mMenuSettings = menu.findItem(R.id.menu_settings);
        
        isLoadedMenu = true;
        
        setActionBarIcons();
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        // stop/start location
        if(mMenuRunLocation.getItemId() == id)
        {
            if(mLocationWorker.isRunLocation())
            {
                stopLocation();
            }
            else
            {
                startLocation();
            }
        }
        // show location
        else if(mMenuShowLocation.getItemId() == id)
        {
            try
            {
                mMapFragment.showLocation();
            }
            catch(RuntimeException e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        // edit
        else if(mMenuEdit.getItemId() == id)
        {
            if(data.activityMode == ActivityMode.EDIT)
            {
                changeMode(ActivityMode.EXPLORE);
            }
            else
            {
                changeMode(ActivityMode.EDIT);
            }
        }
        // create topology
        else if(mMenuTopology.getItemId() == id)
        {
            if(data.addPointMode == AddPointMode.TOPOLOGY_POINT)
            {
                setAddPointMode(AddPointMode.NONE);
            }
            else
            {
                setAddPointMode(AddPointMode.TOPOLOGY_POINT);
            }
        }
        // add point mode
        else if(mMenuAddPoint.getItemId() == id)
        {
            if(data.addPointMode == AddPointMode.ANY_POINT)
            {
                setAddPointMode(AddPointMode.NONE);
            }
            else
            {
                setAddPointMode(AddPointMode.ANY_POINT);
            }
        }
        // settings
        else if(mMenuSettings.getItemId() == id)
        {
            Intent i = new Intent(this, SettingsActivity.class);      
            this.startActivity(i);            
        }
        // hide icon
        else if(android.R.id.home == id)
        {
            hideActionBarNow();
        }        
        
        setActionBarIcons();
        mMapFragment.setMapTools();
     
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        mLocationWorker.pause();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        mLocationWorker.resume();
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        // Shown layers
        outState.putBoolean(SHOWN_LAYERS, mPanelLayout.isShown());
        
        // gps state
        outState.putSerializable(LOCATION_WORKER_DATA, mLocationWorker.getData());
        
        //MainActivity state
        outState.putSerializable(MAIN_ACTIVITY_DATA, data);
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        
        // Shown layers
        if(savedInstanceState.getBoolean(SHOWN_LAYERS))
        {
            showPanel();
        }
        
        // GPS state
        mLocationWorker.setData(savedInstanceState.getSerializable(LOCATION_WORKER_DATA));
        
        //MainActivity state
        data = (MainActivityData) savedInstanceState.getSerializable(MAIN_ACTIVITY_DATA);
        
        mMapFragment.setMapTools();
        
        setActionBarIcons();
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(mPanelLayout.isShown())
            {
                hidePanel();
            }
            else
            {
                ExitDialog dialog = new ExitDialog();
                dialog.setMessage(getString(R.string.exit_message));
                showDialog(dialog);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    // private methods ========================================================

    /**
     * timer for hidding ActionBar
     */
    private void runTimerActionBar()
    {
        cancelTimer();
        
        timer = new Timer();
        timer.schedule(new HideActionBarTask(), mSettings.getTimeHideActionBar());
    }
    
    /**
     * cancel timer for hidding action bar
     */
    private void cancelTimer()
    {
        if(timer != null)
        {
            timer.cancel();
            timer.purge();
        }        
    }
    
    /**
     * set icons in ActionBar by location
     */
    private void setActionBarIcons()
    {
        // check small display
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int showIcon = (ConvertUnits.px2dp(size.x) <= SMALL_WIDTH_BOUNDARY_DPI)
                ? MenuItem.SHOW_AS_ACTION_IF_ROOM
                : MenuItem.SHOW_AS_ACTION_ALWAYS;
        
        // if menu is not loaded yet
        if(!isLoadedMenu)
        {
            return;
        }
        
        // settings icon
        mMenuSettings.setShowAsAction(showIcon);
        
        // location icons
        boolean isRunGPS = mLocationWorker.isRunLocation();
        if(isRunGPS)
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_gps_on));
        }
        else
        {
            mMenuRunLocation.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_gps_off));
        }
        mMenuRunLocation.setChecked(isRunGPS);        
        mMenuShowLocation.setVisible(isRunGPS);
        mMenuRunLocation.setShowAsAction(showIcon);
        mMenuShowLocation.setShowAsAction(showIcon);
        
        // record icon
        boolean isEditMode = (data.activityMode == ActivityMode.EDIT);
        mMenuEdit.setChecked(isEditMode);
        mMenuAddPoint.setVisible(isEditMode);
        mMenuTopology.setVisible(isEditMode);
        if(isEditMode)
        {
            mMenuEdit.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_edit_on));
            
            // add point icon
            boolean isAnyPointMode = (data.addPointMode == AddPointMode.ANY_POINT);
            mMenuAddPoint.setChecked(isAnyPointMode);
            if(isAnyPointMode)
            {
                mMenuAddPoint.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_add_point_on));
            }
            else
            {
                mMenuAddPoint.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_add_point_off));
            }
            
            // topology icon
            boolean isTopologyMode = (data.addPointMode == AddPointMode.TOPOLOGY_POINT);
            mMenuTopology.setChecked(isTopologyMode);
            if(isTopologyMode)
            {
                mMenuTopology.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_topology_on));
            }
            else
            {
                mMenuTopology.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_topology_off));
            }
        }
        else
        {
            mMenuEdit.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_edit_off));
        }
    }

    /**
     * start location service
     */
    private void startLocation()
    {
        mLocationWorker.start();
        
        mMapFragment.startLocation();
    }
    
    /**
     * stop location service
     */
    private void stopLocation()
    {
        mLocationWorker.stop();
        
        mMapFragment.stopLocation();        
    }
    
    private void changeMode(ActivityMode mode)
    {
        // old mode
        getLayersFragment().removeSelectedObject();
        setAddPointMode(AddPointMode.NONE);
        
        // change of mode
        data.activityMode = mode;
        
        // new mode
    }
    
    /**
     * make directory of application when it doesn't exist
     */
    private void checkAppDirectory() throws IOException
    {
        if (!APP_DIRECTORY.exists())
        {
            if(!APP_DIRECTORY.mkdir())
            {
                throw new IOException();
            }
        }
    }
    
    /**
     * hide action bar
     */
    private void hideActionBar()
    {
        getActionBar().hide();
    }
    
    /**
     * hide now actionBar
     */
    private void hideActionBarNow()
    {
        cancelTimer();
        hideActionBar();        
    }
    
    /**
     * change add point mode
     * @param state
     */
    private void setAddPointMode(AddPointMode mode)
    {
        data.addPointMode = mode;
        mMapFragment.setCoordinatesAddPointM(null);
    }
    // classes =================================================================
    /**
     * task for hidding action bar
     * @author jules
     *
     */
    class HideActionBarTask extends TimerTask
    {
        private HideActionBarRunnable hideActionBarRunnable = new HideActionBarRunnable();
        
        public void run()
        {
            MainActivity.this.runOnUiThread(hideActionBarRunnable);
        }
     }
    
    /**
     * runnable hidding action bar
     * @author jules
     *
     */
    class HideActionBarRunnable implements Runnable
    {
        public void run()
        {
            hideActionBar();
        }
    };
}
