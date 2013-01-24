package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Paint;

public class LinesLayer extends PolyPointsLayer
{
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
}
