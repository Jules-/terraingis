package cz.kalcik.vojta.terraingis;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import cz.kalcik.vojta.terraingis.view.MapView;

public class MainActivity extends FragmentActivity
{   
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//        MapView map = (MapView) findViewById(R.id.map);
//        map.setTileSource(TileSourceFactory.MAPNIK);
//        map.setBuiltInZoomControls(true);
//        map.setMultiTouchControls(true);
//        map.getController().setZoom(16);
//        map.getController().setCenter(new GeoPoint(0, 0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
