/**
 * 
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
