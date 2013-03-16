/**
 * 
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
                    values[i] = mStmt.column_string(i);
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
