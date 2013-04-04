package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class PolygonsLayer extends PolyPointsLayer
{
    //constants ===============================================================
    
    // public methods =========================================================
    public PolygonsLayer(String name, int srid,
                         SpatiaLiteIO spatialite, MapFragment mapFragment)
    {
        super(VectorLayerType.POLYGON, name, srid, spatialite, mapFragment);
    }
}
