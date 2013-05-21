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
package cz.kalcik.vojta.terraingis.io;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author jules
 *
 */
public class ShapeFileRecord
{
    private Geometry geometry;
    private String[] attributes;
    
    /**
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return geometry;
    }
    /**
     * @param geometry the geometry to set
     */
    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }
    /**
     * @return the attributes
     */
    public String[] getAttributes()
    {
        return attributes;
    }
    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(String[] attributes)
    {
        this.attributes = attributes;
    }
}
