/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
