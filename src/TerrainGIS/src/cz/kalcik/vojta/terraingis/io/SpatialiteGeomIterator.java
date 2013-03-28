/**
 * 
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
public class SpatialiteGeomIterator extends SpatialiteAbstractIterator implements Iterator<Geometry>
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