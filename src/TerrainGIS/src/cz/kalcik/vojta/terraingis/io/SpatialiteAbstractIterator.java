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

import jsqlite.Exception;
import jsqlite.Stmt;
import android.util.Log;

/**
 * @author jules
 *
 */
public abstract class SpatialiteAbstractIterator
{
    protected Stmt mStmt;
    protected boolean mHasNext = false;
    protected boolean mIsNext = false;
    protected String lastROWID = null;
    
    public SpatialiteAbstractIterator(Stmt stmt)
    {
        this.mStmt = stmt;
    }

    /**
     * @return the lastROWID
     */
    public String getLastROWID()
    {
        return lastROWID;
    }
    
    public boolean hasNext()
    {
        if(!mIsNext)
        {
            try
            {
                mHasNext = mStmt.step();
                if(!mHasNext)
                {
                    mStmt.close();
                }
                else
                {
                    lastROWID = mStmt.column_string(0);
                }
            }
            catch (Exception e)
            {
                Log.e("TerrainGIS", e.getMessage());
            }
            
            mIsNext = true;
        }
        return mHasNext;
    }
    
    public void remove()
    {
        throw new UnsupportedOperationException();
    }  
}
