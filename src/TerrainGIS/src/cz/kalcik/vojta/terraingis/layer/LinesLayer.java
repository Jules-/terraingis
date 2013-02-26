package cz.kalcik.vojta.terraingis.layer;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Paint;

public class LinesLayer extends VectorLayer
{
    // attributes =============================================================
    
    // public methods =========================================================
    public LinesLayer(String name, int srid,
                      SpatiaLiteManager spatialite)
    {
        this(DefaultPaints.getLine(), name, srid, spatialite);
    }
    
    public LinesLayer(Paint paint, String name, int srid,
                      SpatiaLiteManager spatialite)
    {
        super(VectorLayerType.LINE, paint, name, srid, spatialite);
        
        mPaint.setStyle(Paint.Style.STROKE);
        
        mNotSavedPaint = new Paint(mPaint);
        setDashedPath(mNotSavedPaint);
    }
    
    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Envelope rect)
    {
        Iterator<Geometry> iter = getObjects(rect);

        while(iter.hasNext())
        {
            mDrawer.drawCanvasPathM(canvas, iter.next().getCoordinates(), mPaint);
        }
        
        if(data.mRecordedPoints.size() > 0)
        {
            
            mDrawer.drawCanvasPathM(canvas,
                    data.mRecordedPoints.toArray(new Coordinate[data.mRecordedPoints.size()]),
                    mNotSavedPaint);
        }
    }
}
