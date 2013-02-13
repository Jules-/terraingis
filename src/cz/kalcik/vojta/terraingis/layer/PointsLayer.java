package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
    // constants ==============================================================
    private final float RADIUS = 4;
    
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteManager spatialite)
    {
        this(DefaultPaints.getPoint(), name, srid, spatialite);
    }
    
	public PointsLayer(Paint paint, String name, int srid,
                       SpatiaLiteManager spatialite)
	{
	    super(VectorLayerType.POINT, paint, name, srid, spatialite);
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
            mDrawer.drawCircleM(canvas, mPaint, iter.next().getCoordinate(), RADIUS);
        }
    }
}
