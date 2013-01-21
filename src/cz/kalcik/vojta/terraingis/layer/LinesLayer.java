package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Paint;

public class LinesLayer extends PolyPointsLayer
{
    public LinesLayer(String name, int srid)
    {
        this(null, name, srid);
    }
    
    public LinesLayer(Paint paint, String name, int srid)
    {
        super(VectorLayerType.LINE, paint, name, srid);
        
        this.paint.setStyle(Paint.Style.STROKE);
    }        
}
