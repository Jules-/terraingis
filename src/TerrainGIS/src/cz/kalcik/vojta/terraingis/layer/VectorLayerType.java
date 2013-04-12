/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;

/**
 * @author jules
 *
 */
public enum VectorLayerType
{
    POINT, LINE, POLYGON;
    
    public String getSpatialiteType()
    {
        if(this == POINT)
        {
            return "POINT";
        }
        else if(this == LINE)
        {
            return "LINESTRING";
        }
        else if(this == POLYGON)
        {
            return "POLYGON";
        }
        
        return null;
    }
    
    /**
     * @param type
     * @return type of layer
     */
    public static VectorLayerType shapefileToSpatialite(ShpShape.Type type)
    {
        if(type.isTypeOfPoint() || type.isTypeOfMultiPoint())
        {
            return VectorLayerType.POINT;
        }
        else if(type.isTypeOfPolyLine())
        {
            return VectorLayerType.LINE;
        }
        else if(type.isTypeOfPolygon())
        {
            return VectorLayerType.POLYGON;
        }
        
        return null;
    }
    
    public static ShpShape.Type spatialiteToShapefile(VectorLayerType type)
    {
        if(type == VectorLayerType.POINT)
        {
            return ShpShape.Type.Point;
        }
        else if(type == VectorLayerType.LINE)
        {
            return ShpShape.Type.PolyLine;
        }
        else if(type == VectorLayerType.POLYGON)
        {
            return ShpShape.Type.Polygon;
        }
        
        return null;
    }
};
