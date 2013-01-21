package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
    public PointsLayer(String name, int srid)
    {
        this(null, name, srid);
    }
    
	public PointsLayer(Paint paint, String name, int srid)
	{
	    super(VectorLayerType.POINT, paint, name, srid);
	}
}
