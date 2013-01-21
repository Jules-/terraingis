package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Paint;

public class PolygonsLayer extends PolyPointsLayer
{

    public PolygonsLayer(String name, int srid)
    {
        this(null, name, srid);
    }
    
    public PolygonsLayer(Paint paint, String name, int srid)
    {
        super(VectorLayerType.POLYGON, paint, name, srid);
        
        this.paint.setStyle(Paint.Style.FILL);
    }
}
