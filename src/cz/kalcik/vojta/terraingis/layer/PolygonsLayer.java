package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Paint;

public class PolygonsLayer extends PolyPointsLayer
{

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
}
