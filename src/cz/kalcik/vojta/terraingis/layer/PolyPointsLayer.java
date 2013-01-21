package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Paint;

public abstract class PolyPointsLayer extends VectorLayer
{

    public PolyPointsLayer(VectorLayerType type, Paint paint, String name, int srid)
    {
        super(type, paint, name, srid);
    }
}
