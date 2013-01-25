package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

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
        this(null, name, srid, spatialite);
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
        ArrayList<Geometry> objects = getObjects(rect);
        for(Geometry object: objects)
        {
            mDrawer.drawLinesM(canvas, object, mPaint);
        }
    }
}
