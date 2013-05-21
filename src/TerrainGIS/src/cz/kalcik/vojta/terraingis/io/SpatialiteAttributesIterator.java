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

import android.util.Log;

import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * @author jules
 * iterator of attributes
 */
public class SpatialiteAttributesIterator extends SpatialiteAbstractIterator
        implements Iterator<String[]>
{
    private String[] values;
    
    public SpatialiteAttributesIterator(Stmt stmt, int count)
    {
        super(stmt);
        values = new String[count];
    }

    @Override
    public String[] next()
    {
        if(hasNext())
        {
            try
            {
                mIsNext = false;
                
                int count = values.length;
                
                for(int i=0; i < count; i++)
                {
                    values[i] = mStmt.column_string(i+1);
                }
                
                return values;
            }
            catch (Exception e)
            {
                Log.e("TerrainGIS", e.getMessage());
            }
        }
        
        return null;
    }
}
