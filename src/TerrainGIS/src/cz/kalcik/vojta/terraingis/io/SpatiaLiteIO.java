package cz.kalcik.vojta.terraingis.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import cz.kalcik.vojta.terraingis.layer.AttributeRecord;

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
public class SpatiaLiteIO
{    
    // constants ==========================================================================
    public static final int EPSG_SPHERICAL_MERCATOR = 3857;
    public static final int EPSG_LONLAT = 4326;
    public static final String GEOMETRY_COLUMN_NAME = "Geometry";
    public static final String ID_COLUMN_NAME = "id";
    public static final AttributeType ID_COLUMN_TYPE = AttributeType.INTEGER;
    
    // attributes =========================================================================
    private Database db;
    private WKBReader wkbReader = new WKBReader();
    private WKBWriter mWKBWriter = new WKBWriter();
    
    // public methods ======================================================================
    /**
     * constructor
     * @param path
     */
    public SpatiaLiteIO(String path)
    {
    	open(path);
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
     * get layer from spatialite db by name
     * @return list of Layers
     */
    public Layer getLayer(String name)
    {
        Layer result = null;
        
        String query = "SELECT f_table_name, type, srid FROM geometry_columns " +
        		"WHERE f_table_name = ?";
        try
        {
            Stmt stmt = db.prepare(query);
            stmt.bind(1, name);
            
            if(stmt.step())
            {
                result = new Layer();
                result.name = stmt.column_string(0);
                result.type = stmt.column_string(1);
                result.srid = stmt.column_int(2);
            }
            
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * check if layer have index
     * @param name
     * @return
     */
    public boolean indexEnabled(String name)
    {
        boolean result = false;
        
        Stmt stmt;
        try
        {
            stmt = db.prepare("SELECT spatial_index_enabled FROM geometry_columns WHERE f_table_name = ?");
            stmt.bind(1, name);
            if(stmt.step())
            {
                result = (stmt.column_int(0) == 1);
            }
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * return envelope of layer
     * @param name
     * @return
     */
    public Envelope getEnvelopeLayer(String name, String column, boolean useRTree)
    {
        Envelope result = null;
        
        try
        {
            String[] args = {name};
            
            Stmt stmt;

            if(useRTree)
            {
                // TODO use idx_table_column_node
                String cmd = String.format("SELECT MIN(xmin) As xmin, MAX(xmax) As xmax, " +
                        "MIN(ymin) As ymin, MAX(ymax) As ymax "+
                        "FROM \"idx_%s_%s\"", name, column);
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
                result = new Envelope(stmt.column_double(0),
                        stmt.column_double(1),
                        stmt.column_double(2),
                        stmt.column_double(3));
            }
            
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;        
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
            
            stmt.close();
            
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
        Coordinate result = null;
        
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
                result = wkbReader.read(stmt.column_bytes(0)).getCoordinate();
            }
            stmt.close();
        }
        catch (ParseException e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;         
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
        Envelope result = null;
        
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
                result = wkbReader.read(stmt.column_bytes(0)).getEnvelope().getEnvelopeInternal();
            }
            
            stmt.close();
        }
        catch (ParseException e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;
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
                cmd = String.format("SELECT AsBinary(Transform(%s, ?)) FROM \"%s\" WHERE "+
                        "ROWID IN (SELECT pkid FROM \"idx_%s_%s\" WHERE pkid MATCH RTreeIntersects(?, ?, ?, ?))",
                        column, name, name, column);
                
                stmt = db.prepare(cmd);
                
                if(inputSrid != outputSrid)
                {
                    envelope = transformSRSEnvelope(envelope, outputSrid, inputSrid);
                }
            }
            else
            {
                cmd = String.format("SELECT AsBinary(Transform(%s, ?)) FROM \"%s\" WHERE "+
                        "MbrIntersects(BuildMBR(?, ?, ?, ?), Transform(%s, ?)) = 1", column, name, column);           
                stmt = db.prepare(cmd);
                stmt.bind(6, outputSrid);
            }
            
            stmt.bind(1, outputSrid);
            stmt.bind(2, envelope.getMinX());
            stmt.bind(3, envelope.getMinY());
            stmt.bind(4, envelope.getMaxX());
            stmt.bind(5, envelope.getMaxY());
            
            return new SpatialiteGeomIterator(stmt);
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
        String result = null;
        
        try
        {
            Stmt stmt = db.prepare("SELECT f_geometry_column FROM geometry_columns WHERE f_table_name = ?");
            stmt.bind(1, name);
            if(stmt.step())
            {
                result = stmt.column_string(0);
            }
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;        
    }
    
    /**
     * insert geometry to db
     * @param geom
     * @param name
     * @param column
     * @param srid
     */
    public void insertObject(Geometry geom, String name, String column, int inputSrid, int tableSrid,
            AttributeHeader header, AttributeRecord attributesValues, boolean usePK)
    {
        try
        {
            Stmt stmt = prepareInsert(name, column, inputSrid, tableSrid, header, usePK);
            insertObject(stmt, geom, attributesValues, usePK);
            
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }    
    }

    /**
     * insert geometry (can be reused)
     * @param stmt
     * @param geom
     */
    public void insertObject(Stmt stmt, Geometry geom, AttributeRecord attributes, boolean usePK)
    {
        try
        {
            stmt.bind(1, mWKBWriter.write(geom));
            String[] values = usePK ? attributes.getValues() : attributes.getValues();
            int count = values.length;
            int bindIndex = 2;
            for (int i = 0; i < count; i++)
            {
                if(!usePK && attributes.isColumnPK(i))
                {
                    continue;
                }
                
                AttributeType type = attributes.getColumnType(i);
                if(values[i] == null || type == AttributeType.TEXT)
                {
                    stmt.bind(bindIndex, values[i]);
                }
                else if(type == AttributeType.INTEGER)
                {
                    stmt.bind(bindIndex, Integer.parseInt(values[i]));
                }
                else if(type == AttributeType.REAL)
                {
                    stmt.bind(bindIndex, Double.parseDouble(values[i]));
                }
                
                bindIndex++;
            }
            stmt.step();
            
            stmt.clear_bindings();
            stmt.reset();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }    
    }
    
    /**
     * create compiled SQL for insert more queries
     * @param name
     * @param column
     * @param inputSrid
     * @param tableSrid
     * @return
     * @throws Exception
     */
    public Stmt prepareInsert(String name, String column, int inputSrid, int tableSrid,
            AttributeHeader header, boolean usePK) throws Exception
    {
        String geomDefinition;
        
        if(inputSrid == tableSrid)
        {
            geomDefinition = String.format(Locale.UK, "GeomFromWKB(?, %d)", inputSrid);
        }
        else
        {
            geomDefinition = String.format(Locale.UK, "Transform(GeomFromWKB(?, %d), %d)", inputSrid, tableSrid);
        }
        

        String query = String.format("INSERT INTO \"%s\" (\"%s\"%s) VALUES (%s%s)",
                name, column, header.getComaNameColumns(usePK, true),
                geomDefinition, header.getInsertSQLArgs(usePK));
        return db.prepare(query);
    }
    
    /**
     * create layer in spatialite db
     * @param name
     * @param geometryColumn
     * @param type
     * @param srid
     */
    public void createEmptyLayer(String name, String type, String columns, int srid)
    {
        String[] argsTable = {name};
        String[] argsGeom = {name, GEOMETRY_COLUMN_NAME, Integer.toString(srid), type};
        String[] argsIndex = {name, GEOMETRY_COLUMN_NAME};
        
        try
        {
            String query = "CREATE TABLE \"%q\" " + columns;
            db.exec(query, null, argsTable);
            db.exec("SELECT AddGeometryColumn('%q', '%q', %q, '%q', 'XY')", null, argsGeom);
            db.exec("SELECT CreateSpatialIndex('%q', '%q')", null, argsIndex);
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    }
    
    /**
     * remove layer from spatialite db
     * @param name
     * @param geometryColumn
     */
    public void removeLayer(String name, String geometryColumn, boolean hasIndex)
    {
        String[] argsTable = {name};
        String[] argsGeom = {name, geometryColumn};
        
        try
        {
            if(hasIndex)
            {
                db.exec("SELECT DisableSpatialIndex('%q', '%q');", null, argsGeom);
                db.exec("DROP TABLE \"idx_%q_%q\";", null, argsGeom);
            }
            db.exec("SELECT DiscardGeometryColumn('%q', '%q');", null, argsGeom);
            db.exec("DROP TABLE \"%q\";", null, argsTable);
            db.exec("VACUUM;", null, argsTable);
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    }
    
    /**
     * @param name of table
     * @return attribute table
     */
    public AttributeHeader getAttributeTable(String name)
    {
        AttributeHeader result = new AttributeHeader();

        try
        {
            Stmt stmt = db.prepare(String.format("PRAGMA table_info('%s');", name));
            
            while(stmt.step())
            {
                //  0  |  1   |   2  |    3    |     4      | 5
                // cid | name | type | notnull | dflt_value | pk
                
                boolean isPK = (stmt.column_int(5) == 1);
                AttributeType type = AttributeType.getTypeSpatialite(stmt.column_string(2));
                if(type != null)
                {
                    String nameColumn = stmt.column_string(1);
                    
                    result.addColumn(nameColumn, type, isPK);
                }
            }
            
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Initialize spatialite DB
     */
    public void initDB()
    {
        try
        {
            db.exec("SELECT InitSpatialMetaData();", null, null);
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }        
    }
    
    /**
     * @param filter names
     * @return SRS names
     */
    public ArrayList<SpatialiteSRS> findSRSByName(String name)
    {
        ArrayList<SpatialiteSRS> result = new ArrayList<SpatialiteSRS>();
        
        try
        {
            Stmt stmt = db.prepare("SELECT srid, ref_sys_name FROM spatial_ref_sys " +
            		"WHERE ref_sys_name LIKE ? ORDER BY ref_sys_name");
            
            stmt.bind(1, "%"+name+"%");
            
            while(stmt.step())
            {
                SpatialiteSRS item = new SpatialiteSRS();
                item.srid = stmt.column_int(0);
                item.name = stmt.column_string(1);
                result.add(item);
            }
            
            stmt.close();
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * get all attributes of table
     * @param name
     * @param header
     * @return
     */
    public Iterator<String[]> getAttributes(String name, AttributeHeader header)
    {
        try
        {
            String query = String.format("SELECT ROWID, %s FROM \"%s\"",
                    header.getComaNameColumns(true, false), name);
            Stmt stmt = db.prepare(query);
            
            return new SpatialiteAttributesIterator(stmt, header.getCountColumns());
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
        
        return null;        
    }
    
    /**
     * update attributes with new names
     * @param name
     * @param values
     * @param rowid
     * @throws Exception 
     */
    public void updateAttributes(String name, String setString, String[] values, int rowid)
            throws Exception
    {
        String query = String.format("UPDATE \"%s\" SET %s WHERE ROWID=?",
                name, setString);
        Stmt stmt = db.prepare(query);
        
        int count = values.length;
        for(int i=0; i < count;i++)
        {
            stmt.bind(i+1, values[i]);
        }
        
        stmt.bind(count+1, rowid);
        stmt.step();
        
        stmt.close();
    }
    
    /**
     * remove object by ROWID
     * @param name
     * @param rowid
     * @throws Exception
     */
    public void removeObject(String name, int rowid) throws Exception
    {
        String query = String.format("DELETE FROM \"%s\" WHERE ROWID=?",
                name);
        Stmt stmt = db.prepare(query);
                
        stmt.bind(1, rowid);
        stmt.step();
        
        stmt.close();        
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
            File file = new File(path);
            boolean exist = file.exists();
            
            db.open(spatialDbFile.getAbsolutePath(), Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
            db.exec("PRAGMA temp_store = 2;", null, null); // not use temporery files
            
            if(!exist)
            {
                initDB();
            }
        }
        catch (Exception e)
        {
            Log.e("TerrainGIS", e.getMessage());
        }
    }
    // classes ===============================================================================
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
    
    /**
     * @author jules
     * 
     * class for return srid
     */
    public class SpatialiteSRS
    {
        public String name;
        public int srid;
        
        @Override
        public String toString()
        {
            return String.format(Locale.UK, "%s (EPSG: %d)", name, srid);
        }
    }
}