package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class LinesLayer extends PolyPointsLayer
{
    // constants ==============================================================
    public static final int MIN_POINTS = 2;
    
    // attributes =============================================================
    
    // public methods =========================================================
    public LinesLayer(String name, int srid,
                      SpatiaLiteIO spatialite, MapFragment mapFragment)
    {
        super(VectorLayerType.LINE, name, srid, spatialite, mapFragment);
    }

    @Override
    protected int getMinCountPoints()
    {
        return MIN_POINTS;
    }
}
