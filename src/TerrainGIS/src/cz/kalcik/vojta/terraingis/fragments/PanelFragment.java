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
package cz.kalcik.vojta.terraingis.fragments;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public abstract class PanelFragment extends Fragment
{
    // constants ==============================================================
    private static final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.PanelFragmentSaveState";
    
    // attributes =============================================================
    protected MainActivity mMainActivity;
    protected ListBackgroundColors mBackgroundColors;
    protected boolean mAmIVisible = false;
    
    // public methods =========================================================
    
    public void switchToMe()
    {
        mAmIVisible = true;
        
        switchToMeChild();
    }
    
    // getter, setter =========================================================

    
    // on methods =============================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean(TAG_SAVESTATE))
            {
                switchToMe();
            }
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        // save state
        outState.putBoolean(TAG_SAVESTATE, mAmIVisible);
        
        super.onSaveInstanceState(outState);
    }
    
    // protected methods ======================================================
    protected void setCommon(View myView)
    {
        ImageButton buttonHide = (ImageButton)myView.findViewById(R.id.button_hide);
        buttonHide.setOnClickListener(hidePanelHandler);
        ImageButton buttonSwitchPanel = (ImageButton)myView.findViewById(R.id.button_switch_panel);
        buttonSwitchPanel.setOnClickListener(switchPanelHandler);
        
        mMainActivity = (MainActivity)getActivity();
        mBackgroundColors = new ListBackgroundColors(mMainActivity);
    }
    
    // protected abstract method ==============================================
    
    protected abstract void switchToMeChild();
    
    // private methods ========================================================
    
    // handlers ===============================================================

    /**
     * hide panel
     */
    protected View.OnClickListener hidePanelHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mMainActivity.hidePanel();
        }        
    };
    
    /**
     * switch panel
     */
    protected View.OnClickListener switchPanelHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(PanelFragment.this instanceof LayersFragment)
            {
                if(mMainActivity.getLayersFragment().getSelectedLayerIfVector() == null)
                {
                    Toast.makeText(mMainActivity, R.string.not_selected_vector_layer, Toast.LENGTH_LONG).show();
                    return;
                }
                
                mMainActivity.getAttributesFragment().switchToMe();
            }
            else
            {
                mMainActivity.getLayersFragment().switchToMe();
            }
        }        
    };
}
