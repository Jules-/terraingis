package cz.kalcik.vojta.terraingis.layer;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.R.color;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PolygonsLayer extends VectorLayer
{
    //constants ===============================================================
    private final static int TRANCPARENCE_NOTSAVED = 64; 
    
    // public methods =========================================================
    public PolygonsLayer(String name, int srid,
                         SpatiaLiteManager spatialite)
    {
        this(DefaultPaints.getPolygon(), name, srid, spatialite);
    }
    
    public PolygonsLayer(Paint paint, String name, int srid,
                         SpatiaLiteManager spatialite)
    {
        super(VectorLayerType.POLYGON, paint, name, srid, spatialite);
        
        mPaint.setStyle(Paint.Style.FILL);
        
        mNotSavedPaint = new Paint(mPaint);
        mNotSavedPaint.setAlpha(TRANCPARENCE_NOTSAVED);
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
        
        if(mRecordedPoints.size() > 0)
        {
            mDrawer.drawCanvasPathM(canvas,
                                    mRecordedPoints.toArray(new Coordinate[mRecordedPoints.size()]),
                                    mNotSavedPaint);
        }
    }
}
