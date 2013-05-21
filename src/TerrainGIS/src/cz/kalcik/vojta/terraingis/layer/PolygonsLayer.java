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

import jsqlite.Exception;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

public class PolygonsLayer extends PolyVerticesLayer
{
    //constants ===============================================================
    public static final int MIN_POINTS = 3;
    
    // public methods =========================================================
    public PolygonsLayer(String name, int srid,
                         SpatiaLiteIO spatialite, MapFragment mapFragment)
                                 throws Exception
    {
        super(VectorLayerType.POLYGON, name, srid, spatialite, mapFragment);
    }
    
    @Override
    protected int getMinCountPoints()
    {
        return MIN_POINTS;
    }
}
