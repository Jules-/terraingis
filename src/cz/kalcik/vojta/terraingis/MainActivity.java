package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

import android.content.Context;
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
        
        MapView map = (MapView) findViewById(R.id.map);
        
        ArrayList<String> params = new ArrayList<String>();
        params.add("+proj=merc");
        params.add("+a=6378137");
        params.add("+b=6378137");
        params.add("+lat_ts=0.0");
        params.add("+lon_0=0.0");
        params.add("+x_0=0.0");
        params.add("+y_0=0.0");
        params.add("+k=1.0");
        params.add("+units=m");
        params.add("+no_defs");

        
        Projection projection = ProjectionFactory.fromPROJ4Specification(params.toArray(new String[params.size()]));
        map.setProjection(projection);
        map.setLatLonPosition(15.67322, 49.27138);
        map.setZoom((float)0.25);
        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic((Context)this, tileSource);

        map.appendTileLayer(tileProvider, (Context)this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
