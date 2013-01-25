package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PolygonsLayer extends VectorLayer
{
    // public methods =========================================================
    public PolygonsLayer(String name, int srid,
                         SpatiaLiteManager spatialite)
    {
        this(null, name, srid, spatialite);
    }
    
    public PolygonsLayer(Paint paint, String name, int srid,
                         SpatiaLiteManager spatialite)
    {
        super(VectorLayerType.POLYGON, paint, name, srid, spatialite);
        
        this.mPaint.setStyle(Paint.Style.FILL);
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
            mDrawer.drawCanvasPathM(canvas, object, mPaint);
        }
    }
}
