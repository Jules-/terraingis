package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
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
}
