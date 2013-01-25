package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteManager spatialite)
    {
        this(null, name, srid, spatialite);
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

    }
}
