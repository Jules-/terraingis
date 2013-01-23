package cz.kalcik.vojta.terraingis.components;

import java.io.File;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import android.util.Log;

import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * class work with SpatiLite database
 * @author jules
 *
 */
public class SpatiaLiteManager
{    
    // attributes =========================================================================
    private Database db;
    private WKBReader wkbReader = new WKBReader();
    
    // public methods ======================================================================
    /**
     * constructor
     * @param path
     */
    public SpatiaLiteManager(String path)
    {
    	open(path);
    }

    /**
     * get all layers in spatialite database
     * @return [name, type, srid]
     */
    public ArrayList<String[]> getLayers()
    {
    	ArrayList<String[]> list = new ArrayList<String[]>();
    	
    	String query = "SELECT f_table_name, type, srid FROM geometry_columns";
        try
        {
    		Stmt stmt = db.prepare(query);
    		while(stmt.step())
    		{
    			String[] values = new String[3];
    			values[0] = stmt.column_string(0);
    			values[1] = stmt.column_string(1);
    			values[2] = stmt.column_string(2);
    			list.add(values);
    		}
    		stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    	
    	return list;
    }
    
    /**
     * check if layer have index
     * @param name
     * @return
     */
    public boolean indexEnabled(String name)
    {
        Stmt stmt;
        try
        {
            stmt = db.prepare("SELECT spatial_index_enabled FROM geometry_columns WHERE f_table_name = ?");
            stmt.bind(1, name);
            if(stmt.step())
            {
                return (stmt.column_int(0) == 1);
            }
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        return false;
    }
    
    /**
     * return envelope of layer
     * @param name
     * @return
     */
    public Envelope getEnvelopeLayer(String name)
    {
        try
        {
            String[] args = {name};
            
            db.exec("SELECT UpdateLayerStatistics(?)", null, args);
            
            Stmt stmt;
            stmt = db.prepare("SELECT extent_min_x, extent_max_x, extent_min_y, extent_max_y " +
                              "from LAYER_STATISTICS where table_name=?");
            stmt.bind(1, name);
            if(stmt.step())
            {
                return new Envelope(stmt.column_double(0),
                                    stmt.column_double(1),
                                    stmt.column_double(2),
                                    stmt.column_double(3));
            }
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return null;        
    }
    
    /**
     * transform coordinates between two srid
     * @param point
     * @param from
     * @param to
     * @return
     */
    public Coordinate transformSRS(Coordinate point, int from, int to)
    {
        try
        {            
            Stmt stmt = db.prepare("SELECT AsBinary(Transform(MakePoint(?, ?, ?), ?))");
            
            stmt.bind(1, point.x);
            stmt.bind(2, point.y);
            stmt.bind(3, from);
            stmt.bind(4, to);
            if(stmt.step())
            {
                return wkbReader.read(stmt.column_bytes(0)).getCoordinate();
            }
        }
        catch (ParseException e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return null;         
    }
    // private methods =======================================================================
    /**
     * open spatialite databse
     * @param path
     */
    private void open(String path)
    {
        File spatialDbFile = new File(path);
        
        db = new Database();
        try
        {
            db.open(spatialDbFile.getAbsolutePath(), Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    }
}