package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.terraingis.components.LocationWorker;
import cz.kalcik.vojta.terraingis.components.Settings;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLine;
import cz.kalcik.vojta.terraingis.layer.VectorPolygon;
import cz.kalcik.vojta.terraingis.view.MapView;

public class MainActivity extends FragmentActivity
{
    // constants ==========================================================
    private static String LOCATION_WORKER_DATA = "LocationWorkerData";
    private static String MAP_VIEW_DATA = "MapViewData";
    
    // properties =========================================================
    private MenuItem menuRunLocation;
    private MenuItem menuShowLocation;
    private MapView map;
    private LocationWorker locationWorker;
    private HideActionBarTask hideActionBarTask = new HideActionBarTask();
    private Timer timer;
    private Settings settings = Settings.getInstance();
    
    // public methods =====================================================
    
    /**
     * show ActionBar
     */
    public void showActionBar()
    {
        getActionBar().show();
        runTimerActionBar();
    }
    
    /**
     * return height of ActionBar
     * @return
     */
    public int getActionBarHeight()
    {
        return getActionBar().getHeight();
    }
    
    // on methods =========================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //initLocation();
        
        map = (MapView) findViewById(R.id.map);
        map.setMainActivity(this);
        locationWorker = new LocationWorker(this, map);
        
        createTestingMap();

        runTimerActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        
        menuRunLocation = menu.findItem(R.id.menu_location);
        menuShowLocation = menu.findItem(R.id.menu_show_location);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(menuRunLocation.getItemId() == id)
        {
            item.setChecked(!item.isChecked());
            
            if(item.isChecked())
            {
                locationWorker.start();
            }
            else
            {
                locationWorker.stop();
            }
        }
        else if(menuShowLocation.getItemId() == id)
        {
            if(!map.showLocation())
            {
                Toast.makeText(this, R.string.warning_location_fix, Toast.LENGTH_LONG).show();
            }
        }
     
        return true;
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        locationWorker.pause();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        locationWorker.resume();
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        // gps state
        outState.putSerializable(LOCATION_WORKER_DATA, locationWorker.getData());
        
        // Map view state
        outState.putSerializable(MAP_VIEW_DATA, map.getData());
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        
        // GPS state
        locationWorker.setData(savedInstanceState.getSerializable(LOCATION_WORKER_DATA));
        menuRunLocation.setChecked(locationWorker.isRunLocation());
        
        // Map view state
        map.setData(savedInstanceState.getSerializable(MAP_VIEW_DATA));
    }
    
    // private methods ========================================================
    
    private void createTestingMap()
    {
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

        // tiles layer
        Projection projection = ProjectionFactory.fromPROJ4Specification(params.toArray(new String[params.size()]));
        map.setProjection(projection);
        map.setLonLatPosition(15.67322, 49.27138);
        map.setZoom(1.195);
        
        final ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
        MapTileProviderBase tileProvider = new MapTileProviderBasic((Context)this, tileSource);

        map.addTilesLayer(tileProvider, (Context)this);
        
        // line layer
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
        paint.setAntiAlias(true);
        
        VectorLayer layer = new VectorLayer(VectorLayer.VectorLayerType.LINE, paint);
        layer.addObject(vectorLine1);
        layer.addObject(vectorLine2);
        
        map.addLayer(layer);
        
        // polygon layer
        points.clear();
        
        points.add(new Point2D.Double(15.673885610694882, 49.271763586552332));
        points.add(new Point2D.Double(15.674051907653805, 49.27174958602086));
        points.add(new Point2D.Double(15.674073365325924, 49.271826588894797));
        points.add(new Point2D.Double(15.674282577629086, 49.271805588122916));
        points.add(new Point2D.Double(15.674341586227413, 49.272026095781783));
        points.add(new Point2D.Double(15.674127009506222, 49.272050596571916));
        points.add(new Point2D.Double(15.674127009506222, 49.271987594515537));
        points.add(new Point2D.Double(15.674084094161984, 49.271994594747994));
        points.add(new Point2D.Double(15.674057272071835, 49.27187909078539));
        points.add(new Point2D.Double(15.673912432785031, 49.271893091280106));
        
        VectorPolygon vectorPolygon1 = new VectorPolygon();
        vectorPolygon1.addLonLatPoints(points);
        
        points.clear();

        points.add(new Point2D.Double(15.673375990982052, 49.271333068392011));
        points.add(new Point2D.Double(15.67343499958038, 49.271364569848217));
        points.add(new Point2D.Double(15.67342963516235, 49.271399571442629));
        points.add(new Point2D.Double(15.673381355400082, 49.271420572387349));
        points.add(new Point2D.Double(15.673375990982052, 49.271462574249988));
        points.add(new Point2D.Double(15.673252609367367, 49.271455573942035));
        points.add(new Point2D.Double(15.673252609367367, 49.271420572387349));
        points.add(new Point2D.Double(15.673054125900265, 49.27141707223052));
        points.add(new Point2D.Double(15.673054125900265, 49.271399571442629));
        points.add(new Point2D.Double(15.672984388465878, 49.271399571442629));
        points.add(new Point2D.Double(15.672995117301937, 49.27134006871735));
        points.add(new Point2D.Double(15.673059490318295, 49.27134006871735));
        points.add(new Point2D.Double(15.673059490318295, 49.271291066419117));
        points.add(new Point2D.Double(15.673161414260861, 49.271294566584892));
        points.add(new Point2D.Double(15.67316677867889, 49.271322567902139));

        VectorPolygon vectorPolygon2 = new VectorPolygon();
        vectorPolygon2.addLonLatPoints(points);
        
        Paint paint2 = new Paint();
        paint2.setStrokeWidth(1);
        paint2.setColor(Color.rgb(0, 0, 255));
        paint2.setAntiAlias(true);
        
        VectorLayer layer2 = new VectorLayer(VectorLayer.VectorLayerType.POLYGON, paint2);
        layer2.addObject(vectorPolygon1);
        layer2.addObject(vectorPolygon2);
        
        map.addLayer(layer2);
    }
    
    private void runTimerActionBar()
    {
        if(timer != null)
        {
            timer.cancel();
            timer = null;
        }
        
        timer = new Timer();
        timer.schedule(hideActionBarTask, settings.getTimeHideActionBar());
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
            getActionBar().hide();
        }
    };
}
