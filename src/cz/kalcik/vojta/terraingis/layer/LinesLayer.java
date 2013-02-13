package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Paint;

public class LinesLayer extends VectorLayer
{
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
        
        this.mPaint.setStyle(Paint.Style.STROKE);
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
            mDrawer.drawLinesM(canvas, iter.next(), mPaint);
        }
    }
}
