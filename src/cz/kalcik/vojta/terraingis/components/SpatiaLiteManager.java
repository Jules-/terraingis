package cz.kalcik.vojta.terraingis.components;

import java.io.File;

import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Exception;

/**
 * class work with SpatiLite database
 * @author jules
 *
 */
public class SpatiaLiteManager
{    
    // singleton code =====================================================================
   
    private static SpatiaLiteManager instance = new SpatiaLiteManager();
    
    private SpatiaLiteManager() { }
    
    public static SpatiaLiteManager getInstance()
    {
        return instance;
    }
    
    // attributes =========================================================================
    private Database db;
    
    // public methods ======================================================================

    public void open(String path)
    {
        File spatialDbFile = new File(path);
        
        db = new Database();
        try
        {
            db.open(spatialDbFile.getAbsolutePath(), Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    // getter setter =======================================================================


    // public methods =======================================================================

}