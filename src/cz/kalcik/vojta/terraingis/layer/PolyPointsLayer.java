package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.components.SpatiaLiteManager;
import android.graphics.Paint;

public abstract class PolyPointsLayer extends VectorLayer
{

    public PolyPointsLayer(VectorLayerType type, Paint paint, String name, int srid,
                           SpatiaLiteManager spatialite)
    {
        super(type, paint, name, srid, spatialite);
    }
}
