package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class LinesLayer extends PolyPointsLayer
{
    // attributes =============================================================
    
    // public methods =========================================================
    public LinesLayer(String name, int srid,
                      SpatiaLiteIO spatialite)
    {
        super(VectorLayerType.LINE, name, srid, spatialite);
    }
}
