/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
