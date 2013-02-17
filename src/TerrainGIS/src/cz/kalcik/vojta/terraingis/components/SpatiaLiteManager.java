package cz.kalcik.vojta.terraingis.components;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

import android.R.bool;
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
    private WKBWriter mWKBWriter = new WKBWriter();
    
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
    public Envelope getEnvelopeLayer(String name)
    {
        try
        {
            String[] args = {name};
            
            db.exec("SELECT UpdateLayerStatistics('%q')", null, args);
            
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
     * create virtual shape
     * @param path
     * @param name
     * @param encoding
     * @param srid
     */
    public void createVirtualShape(String path, String name, String encoding, int srid)
    {
        try
        {
            // TODO virtual shape
            Stmt stmt = db.prepare(String.format("CREATE VIRTUAL TABLE '%s_virtual' USING VirtualShape('%s', '%s', %d)", name, path, encoding, srid));
//            stmt.bind(1, path);
//            stmt.bind(2, encoding);
//            stmt.bind(3, srid);
            stmt.step();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }        
    }
    
    /**
     * insert geometry to db
     * @param geom
     * @param name
     * @param column
     * @param srid
     */
    public void inserGeometry(Geometry geom, String name, String column, int inputSrid, int tableSrid)
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
}