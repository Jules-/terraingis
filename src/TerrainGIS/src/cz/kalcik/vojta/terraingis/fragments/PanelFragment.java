package cz.kalcik.vojta.terraingis.fragments;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import android.app.Fragment;
import android.view.View;
import android.widget.ImageButton;

public abstract class PanelFragment extends Fragment
{
    // attributes =============================================================
    protected MainActivity mMainActivity;
    protected ListBackgroundColors mBackgroundColors;
    
    // protected methods ======================================================
    protected void setCommon(View myView)
    {
        ImageButton buttonHide = (ImageButton)myView.findViewById(R.id.button_hide);
        buttonHide.setOnClickListener(hidePanelHandler);
        
        
        mBackgroundColors = new ListBackgroundColors(mMainActivity);
    }
    
    // handlers ===============================================================

    /**
     * hide panel
     */
    protected View.OnClickListener hidePanelHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mMainActivity.hideLayersFragment();
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
        }        
    };
}
