package cz.kalcik.vojta.terraingis.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

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
    // constants ==========================================================================
    public static final int EPSG_SPHERICAL_MERCATOR = 3857;
    public static final int EPSG_LONLAT = 4326;
    public static final String GEOMETRY_COLUMN_NAME = "Geometry";
    
    // attributes =========================================================================
    private Database db;
    private WKBReader wkbReader = new WKBReader();
    private WKBWriter mWKBWriter = new WKBWriter();
    private String mPath;
    
    // public methods ======================================================================
    /**
     * constructor
     * @param path
     */
    public SpatiaLiteManager(String path)
    {
        mPath = path;
    	open(path);
    }

    /**
     * close and open db
     * @throws Exception 
     */
    public void reopen() throws Exception
    {
        db.close();
        open(mPath);        
    }
    
    /**
     * get all layers in spatialite database
     * @return list of Layers
     */
    public ArrayList<Layer> getLayers()
    {
    	ArrayList<Layer> list = new ArrayList<Layer>();
    	
    	String query = "SELECT f_table_name, type, srid FROM geometry_columns";
        try
        {
    		Stmt stmt = db.prepare(query);
    		while(stmt.step())
    		{
    			Layer layer = new Layer();
    			layer.name = stmt.column_string(0);
    			layer.type = stmt.column_string(1);
    			layer.srid = stmt.column_int(2);
    			list.add(layer);
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
        try
        {
            Stmt stmt = db.prepare("SELECT spatial_index_enabled FROM geometry_columns WHERE f_table_name = ?");
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
    public Envelope getEnvelopeLayer(String name, String column, boolean useRTree)
    {
        try
        {
            String[] args = {name};
            
            Stmt stmt;

            if(useRTree)
            {
                // TODO use idx_table_column_node
                String cmd = String.format("SELECT MIN(xmin) As xmin, MAX(xmax) As xmax, " +
                        "MIN(ymin) As ymin, MAX(ymax) As ymax "+
                        "FROM 'idx_%s_%s'", name, column);
                stmt = db.prepare(cmd);
            }
            else
            {
                db.exec("SELECT UpdateLayerStatistics('%q')", null, args);
                
                stmt = db.prepare("SELECT extent_min_x, extent_max_x, extent_min_y, extent_max_y " +
                                  "from LAYER_STATISTICS where table_name=?");
                stmt.bind(1, name);
            }
            
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
     * transform coordinates between two srs
     * @param points
     * @param from
     * @param to
     * @return
     */
    public ArrayList<Coordinate> transformSRS(ArrayList<Coordinate> points, int from, int to)
    {
        if(from == to)
        {
            return (ArrayList<Coordinate>)points.clone();
        }
        
        try
        {            
            Stmt stmt = db.prepare("SELECT AsBinary(Transform(MakePoint(?, ?, ?), ?))");
            
            ArrayList<Coordinate> result = new ArrayList<Coordinate>();
            for(Coordinate point: points)
            {
                stmt.bind(1, point.x);
                stmt.bind(2, point.y);
                stmt.bind(3, from);
                stmt.bind(4, to);
                if(stmt.step())
                {
                    result.add(wkbReader.read(stmt.column_bytes(0)).getCoordinate());
                }
                
                stmt.clear_bindings();
                stmt.reset();
            }
            return result;
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
 
    /**
     * transform coordinates between two srs
     * @param point
     * @param from
     * @param to
     * @return
     */
    public Coordinate transformSRS(Coordinate point, int from, int to)
    {
        if(from == to)
        {
            return (Coordinate)point.clone();
        }
        
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
    
    /**
     * transform envelop coordinates between two srs
     * @param envelope
     * @param from
     * @param to
     * @return
     */
    public Envelope transformSRSEnvelope(Envelope envelope, int from, int to)
    {
        if(from == to)
        {
            return new Envelope(envelope.getMinX(), envelope.getMaxX(),
                                envelope.getMinY(), envelope.getMaxY());
        }
        
        try
        {            
            Stmt stmt = db.prepare("SELECT AsBinary(Transform(BuildMbr(?, ?, ?, ?, ?), ?))");
            
            stmt.bind(1, envelope.getMinX());
            stmt.bind(2, envelope.getMinY());
            stmt.bind(3, envelope.getMaxX());
            stmt.bind(4, envelope.getMaxY());
            stmt.bind(5, from);
            stmt.bind(6, to);
            
            if(stmt.step())
            {
                return wkbReader.read(stmt.column_bytes(0)).getEnvelope().getEnvelopeInternal();
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
    
    /**
     * return objects from envelope
     * @param envelope
     * @param name
     * @param column
     * @param inputSrid
     * @param outputSrid
     * @param useRTree
     * @return
     */
    public Iterator<Geometry> getObjects(Envelope envelope, String name, String column,
                                          int inputSrid, int outputSrid, boolean useRTree)
    {
        try
        {
            String cmd;
            Stmt stmt;
            if(useRTree)
            {
                cmd = String.format("SELECT AsBinary(Transform(%s, ?)) FROM '%s' WHERE "+
                        "ROWID IN (SELECT pkid FROM 'idx_%s_%s' WHERE pkid MATCH RTreeIntersects(?, ?, ?, ?))",
                        column, name, name, column);
                
                stmt = db.prepare(cmd);
                
                if(inputSrid != outputSrid)
                {
                    envelope = transformSRSEnvelope(envelope, outputSrid, inputSrid);
                }
            }
            else
            {
                cmd = String.format("SELECT AsBinary(Transform(%s, ?)) FROM '%s' WHERE "+
                        "MbrIntersects(BuildMBR(?, ?, ?, ?), Transform(%s, ?)) = 1", column, name, column);           
                stmt = db.prepare(cmd);
                stmt.bind(6, outputSrid);
            }
            
            stmt.bind(1, outputSrid);
            stmt.bind(2, envelope.getMinX());
            stmt.bind(3, envelope.getMinY());
            stmt.bind(4, envelope.getMaxX());
            stmt.bind(5, envelope.getMaxY());
            
            return new GeomIterator(stmt);
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * get name of column with geometry
     * @param name
     * @return
     */
    public String getColumnGeom(String name)
    {
        try
        {
            Stmt stmt = db.prepare("SELECT f_geometry_column FROM geometry_columns WHERE f_table_name = ?");
            stmt.bind(1, name);
            if(stmt.step())
            {
                return stmt.column_string(0);
            }
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return null;        
    }
    
    /**
     * insert geometry to db
     * @param geom
     * @param name
     * @param column
     * @param srid
     */
    public void inserGeometry(Geometry geom, String name, String column, int inputSrid, int tableSrid, boolean reopen)
    {
        try
        {
            String value;
            
            if(inputSrid == tableSrid)
            {
                value = String.format("GeomFromWKB(?, %d)", inputSrid);
            }
            else
            {
                value = String.format("Transform(GeomFromWKB(?, %d), %d)", inputSrid, tableSrid);
            }
            
            Stmt stmt = db.prepare(String.format("INSERT INTO '%s' ('%s') VALUES (%s)",
                                   name, column, value));
            stmt.bind(1, mWKBWriter.write(geom));
            stmt.step();
            
            if(reopen)
            {
                reopen();
            }
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }    
    }
    
    /**
     * create layer in spatialite db
     * @param name
     * @param geometryColumn
     * @param type
     * @param srid
     */
    public boolean createEmptyLayer(String name, String geometryColumn, String type, int srid)
    {
        String[] argsTable = {name};
        String[] argsGeom = {name, geometryColumn, Integer.toString(srid), type};
        String[] argsIndex = {name, geometryColumn};
        
        try
        {
            db.exec("CREATE TABLE '%q' (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)", null, argsTable);
            db.exec("SELECT AddGeometryColumn('%q', '%q', %q, '%q', 'XY')", null, argsGeom);
            db.exec("SELECT CreateSpatialIndex('%q', '%q')", null, argsIndex);
            reopen();
            
            return true;
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
            
            return false;
        }
    }
    
    /**
     * remove layer from spatialite db
     * @param name
     * @param geometryColumn
     */
    public void removeLayer(String name, String geometryColumn)
    {
        String[] argsTable = {name};
        String[] argsGeom = {name, geometryColumn};
        
        try
        {
            db.exec("SELECT DiscardGeometryColumn('%q', '%q')", null, argsGeom);
            reopen();
            db.exec("DROP TABLE '%q'", null, argsTable);            
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    }
    // private methods =======================================================================
    /**
     * open spatialite database
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
    // classes ===============================================================================
    /**
     * Iterator returned from db
     * @author jules
     *
     */
    class GeomIterator implements Iterator<Geometry>
    {
        private Stmt mStmt;
        private boolean mHasNext = false;
        private boolean mIsNext = false;
        
        public GeomIterator(Stmt stmt)
        {
            this.mStmt = stmt;
        }
        
        @Override
        public boolean hasNext()
        {
            if(!mIsNext)
            {
                try
                {
                    mHasNext = mStmt.step();
                }
                catch (Exception e)
                {
                    Log.e("TerrainGIS", e.getMessage());
                }
                
                mIsNext = true;
            }
            return mHasNext;
        }

        @Override
        public Geometry next()
        {
            if(hasNext())
            {
                try
                {
                    mIsNext = false;
                    return wkbReader.read(mStmt.column_bytes(0));
                }
                catch (ParseException e)
                {
                    Log.e("TerrainGIS", e.getMessage());
                }
                catch (Exception e)
                {
                    Log.e("TerrainGIS", e.getMessage());
                }
            }
            
            return null;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }    
    }
    
    /**
     * class for result layer values
     * @author jules
     *
     */
    public class Layer
    {
        public String name;
        public String type;
        public int srid;
    }
}