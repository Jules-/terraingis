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
    
    public SpatialiteAbstractIterator(Stmt stmt)
    {
        this.mStmt = stmt;
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
