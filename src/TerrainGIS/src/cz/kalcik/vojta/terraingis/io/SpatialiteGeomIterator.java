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
package cz.kalcik.vojta.terraingis.io;

import java.util.Iterator;

import jsqlite.Exception;
import jsqlite.Stmt;
import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

/**
 * @author jules
 * iterator of geometry
 */
public class SpatialiteGeomIterator extends SpatialiteAbstractIterator
    implements Iterator<Geometry>
{
    private WKBReader wkbReader = new WKBReader();
    
    public SpatialiteGeomIterator(Stmt stmt)
    {
        super(stmt);
    }
    
    @Override
    public Geometry next()
    {
        if(hasNext())
        {
            try
            {
                mIsNext = false;
                return wkbReader.read(mStmt.column_bytes(1));
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
}