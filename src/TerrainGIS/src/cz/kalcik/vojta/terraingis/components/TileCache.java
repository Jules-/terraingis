/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import cz.kalcik.vojta.terraingis.view.MapView;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author jules
 *
 */
public class TileCache
{
    // constants ==========================================================================
    private static final String CACHE_TABLE_NAME = "cache_tiles";
    private static final String CREATE_CACHE_TABLE = 
            "CREATE TABLE cache_tiles (" +
            "'zoom' INTEGER NOT NULL, " +
            "'x' INTEGER NOT NULL, " +
            "'y' INTEGER NOT NULL, " +
            "'datetime' INTEGER NOT NULL, " +
            "'data' BLOB NOT NULL, " +
            "PRIMARY KEY (zoom, x, y))";
    private static final String[] CACHE_SELECT_COLUMNS = {"datetime", "x", "y", "data"};
    private static final String CACHE_CONDITION = "x >= ? AND x <= ? AND y >= ? AND y <= ? AND zoom = ?";
    private static final long TIME_EXPIRATION = 60*60*24*3; // 3 days
    
    // singleton code =====================================================================
    
    private static TileCache instance = new TileCache();
    
    private TileCache() { }
    
    public static TileCache getInstance()
    {
        return instance;
    }
    
    // attributes ==========================================================================
    
    private ArrayList<Tile> mRequests = new ArrayList<TileCache.Tile>();
    private SQLiteDatabase mDB;
    private ArrayList<Tile> memoryCache = new ArrayList<TileCache.Tile>();
    private Resources mResources;
    private Rect mLastRect = new Rect();
    private boolean mDatabaseChanged = true;
    
    // public methods ======================================================================
    
    /**
     * open cache file
     * @param file
     */
    public void open(File file, Resources resources)
    {
        mResources = resources;
        boolean exists = file.exists();
        
        mDB = SQLiteDatabase.openOrCreateDatabase(file, null);

        if(!exists)
        {
            mDB.execSQL(CREATE_CACHE_TABLE);
        }
    }
    
    /**
     * return tiles cached in db
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @param zoom
     * @return
     */
    public ArrayList<Tile> getTiles(int minX, int maxX, int minY, int maxY, int zoom)
    {
        if(checkMemoryCache(minX, maxX, minY, maxY))
        {
            return memoryCache;
        }
        
        String[] args = {Integer.toString(minX), Integer.toString(maxX), Integer.toString(minY),
                Integer.toString(maxY), Integer.toString(zoom)};
        int sizeX = maxX-minX+1;
        int sizeY = maxY-minY+1;
        boolean[][] cachedTiles = new boolean[sizeX][sizeY];
        ArrayList<Tile> result = new ArrayList<TileCache.Tile>();
        
        synchronized(this)
        {
            Cursor cur = mDB.query(CACHE_TABLE_NAME, CACHE_SELECT_COLUMNS, CACHE_CONDITION, args, null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                Tile tile = new Tile(); 
                tile.x = cur.getInt(cur.getColumnIndex("x"));
                tile.y = cur.getInt(cur.getColumnIndex("y"));
                byte[] data = cur.getBlob(cur.getColumnIndex("data"));
                tile.imageTile = new BitmapDrawable(mResources,
                        BitmapFactory.decodeByteArray(data, 0, data.length));                        
                result.add(tile);
                
                long datetime = cur.getLong(cur.getColumnIndex("datetime"));
                long unixTime = System.currentTimeMillis() / 1000L;
                if(unixTime - datetime < TIME_EXPIRATION)
                {
                    cachedTiles[tile.x-minX][tile.y-minY] = true;
                }
            }
            
            mRequests.clear();
            
            for(int ix=0; ix < sizeX; ix++)
            {
                for(int iy=0; iy < sizeY; iy++)
                {
                    if(!cachedTiles[ix][iy])
                    {
                        Tile tile = new Tile();
                        tile.x = minX+ix;
                        tile.y = minY+iy;
                        tile.zoom = zoom;
                        
                        mRequests.add(tile);
                    }
                }            
            }
            
            mDatabaseChanged = false;
        }
        
        mLastRect.set(minX, minY, maxX, maxY);
        memoryCache = result;
        
        return result;
    }
    
    /**
     * @return one not cached tile
     */
    public synchronized Tile getOneRequest()
    {
        int size = mRequests.size();
        if(size == 0)
        {
            return null;
        }
        
        return mRequests.remove(size-1);
    }
    
    /**
     * @param tile
     */
    public synchronized void insertTile(Tile tile)
    {
        ContentValues values = new ContentValues();
        values.put("data", tile.downladedData);
        values.put("datetime", getCurrentTime());
      
        String[] args =
            {Integer.toString(tile.x), Integer.toString(tile.y), Integer.toString(tile.zoom)};
        int affected = mDB.update(CACHE_TABLE_NAME, values, "x = ? AND y = ? AND zoom = ?", args);
        
        if(affected == 0)
        {
            values.put("x", tile.x);
            values.put("y", tile.y);
            values.put("zoom", tile.zoom);
            mDB.insert(CACHE_TABLE_NAME, null, values);
        }
        
        mDatabaseChanged = true;
    }
    
    // getter, setter ======================================================================
    /**
     * @return true if are there incomplete request
     */
    public synchronized boolean hasIncompletedRequest()
    {
        return mRequests.size() > 0;
    }
    
    // private methods =====================================================================
    /**
     * @return current time (unixepoch) in seconds
     */
    private long getCurrentTime()
    {
        return System.currentTimeMillis() / 1000L;
    }
    
    /**
     * @return true if all tiles are in cache
     */
    private boolean checkMemoryCache(int minX, int maxX, int minY, int maxY)
    {
        return (mLastRect.left == minX && mLastRect.right == maxX &&
                mLastRect.top == minY && mLastRect.bottom == maxY && !mDatabaseChanged);
    }
    // classes =============================================================================
    public class Tile
    {
        public int x;
        public int y;
        public int zoom;
        public byte[] downladedData;
        public Drawable imageTile;
    }
}
