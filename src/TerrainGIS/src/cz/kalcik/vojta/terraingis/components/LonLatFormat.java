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
package cz.kalcik.vojta.terraingis.components;

import java.util.Locale;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author jules
 * class for formated latitude longitude coordinates
 */
public class LonLatFormat
{
    private final static String NORTH_CHAR = "N";
    private final static String SOUTH_CHAR = "S";
    private final static String WEST_CHAR = "W";
    private final static String EAST_CHAR = "E";
    private final static String DM_FORMAT = "%s%d° %.4f'  %s%d° %.4f'";
    
    /**
     * @param coordinate
     * @return string of coordinates in format degrees and minutes
     */
    public static String getFormatDM(Coordinate coordinate)
    {
        String latChar = getLatitudeHemisphere(coordinate);
        String lonChar = getLongitudeHemisphere(coordinate);
        int latDegrees = (int) Math.abs(coordinate.y);
        int lonDegrees = (int) Math.abs(coordinate.x);
        double latMinutes = getSubvalue(Math.abs(coordinate.y));
        double lonMinutes = getSubvalue(Math.abs(coordinate.x));
        
        return String.format(Locale.UK, DM_FORMAT, latChar, latDegrees, latMinutes,
                lonChar, lonDegrees, lonMinutes);
    }
    
    // private static methods ======================================================
    /**
     * @param value
     * @return from decimal part of sixty
     */
    private static double getSubvalue(double value)
    {
        return (value % 1) * 60; 
    }
    
    /**
     * @param coordinate
     * @return char of longitude hemispher
     */
    private static String getLongitudeHemisphere(Coordinate coordinate)
    {
        return coordinate.x >= 0 ? EAST_CHAR : WEST_CHAR;
    }
    
    /**
     * @param coordinate
     * @return char of latitude hemisphere
     */
    private static String getLatitudeHemisphere(Coordinate coordinate)
    {
        return coordinate.y >= 0 ? NORTH_CHAR : SOUTH_CHAR;
    }
}
