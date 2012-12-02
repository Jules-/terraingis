package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLine;
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
        map.setZoom((float)1);
        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic((Context)this, tileSource);

        map.addTilesLayer(tileProvider, (Context)this);
               
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        points.add(new Point2D.Double(15.672464039916989, 49.270704905801416));
        points.add(new Point2D.Double(15.672528412933346, 49.271236934470515));
        points.add(new Point2D.Double(15.672871735687252, 49.271824959482032));
        points.add(new Point2D.Double(15.673343804473873, 49.272272973833871));
        points.add(new Point2D.Double(15.673472550506588, 49.27281898582396));
        points.add(new Point2D.Double(15.673300889129635, 49.273280991249003));
        points.add(new Point2D.Double(15.672635701293942, 49.273588992461789));
        points.add(new Point2D.Double(15.671627190704342, 49.273952991415683));
        
        VectorLine vectorLine1 = new VectorLine();
        vectorLine1.addLonLatPoints(points);
        
        points.clear();
               
        points.add(new Point2D.Double(15.668666031951901, 49.273210990705174));
        points.add(new Point2D.Double(15.669138100738522, 49.273000988477641));
        points.add(new Point2D.Double(15.670532849426266, 49.272524980118732));
        points.add(new Point2D.Double(15.672056344146725, 49.272020966261607));
        points.add(new Point2D.Double(15.672828820343014, 49.271810958967976));
        
        VectorLine vectorLine2 = new VectorLine();
        vectorLine2.addLonLatPoints(points);
        
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStyle(Style.STROKE);
        
        VectorLayer layer = new VectorLayer(VectorLayer.LayerType.LINE, paint);
        layer.addObject(vectorLine1);
        layer.addObject(vectorLine2);
        
        map.addLayer(layer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
