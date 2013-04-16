/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

import cz.kalcik.vojta.terraingis.components.TileCache.Tile;

/**
 * @author jules
 * 
 */
public class TileDownloader extends Thread
{
    // constnts =====================================================================
    private static final String TILE_URL = "http://%c.tile.openstreetmap.org/%d/%d/%d.png";
    private static final char[] URL_CHARS = {'a', 'b', 'c'};
    // attributes ===================================================================
    private TileCache mTileCache = TileCache.getInstance();
    private int mCharsIndex = 0;
    
    // public methods ===============================================================    
    public TileDownloader()
    {
        super();
    }

    /**
     * method for processing tiles
     */
    public void run()
    {
        boolean hasTiles = true;
        
        while(hasTiles)
        {
            Tile tile = mTileCache.getOneRequest();
            
            if(tile != null)
            {
                tile.downladedData = downloadTile(tile.x, tile.y, tile.zoom);
                if(tile.downladedData != null)
                {
                    mTileCache.insertTile(tile);
                }
            }
            else
            {
                hasTiles = false;
            }
        }
    }
    
    // private methods ==============================================================
    /**
     * download png tile
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private byte[] downloadTile(int x, int y, int zoom)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        URL url;
        String urlString = String.format(Locale.UK, TILE_URL, URL_CHARS[mCharsIndex], zoom, x, y);
        mCharsIndex = (mCharsIndex + 1) % URL_CHARS.length;
        byte[] byteChunk = new byte[4096];

        byte[] result = null;
        try
        {
            url = new URL(urlString);
            inputStream = url.openStream();
            
            int n;       
            while ( (n = inputStream.read(byteChunk)) > 0 )
            {
                outputStream.write(byteChunk, 0, n);
            }
            
            inputStream.close();
            
            result = outputStream.toByteArray();
        }
        catch (IOException e)
        {
            Log.e("TerrainGIS", "Can not download tile");
        }
        
        return result;
    }
}
