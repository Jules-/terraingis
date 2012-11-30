package cz.kalcik.vojta.terraingis.exception;

public class TerrainGISException extends RuntimeException
{
    public TerrainGISException(String m)
    {
        super("TerrainGIS: " + m);
    }    
}