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
        int latDegrees = (int) coordinate.y;
        int lonDegrees = (int) coordinate.x;
        double latMinutes = getSubvalue(coordinate.y);
        double lonMinutes = getSubvalue(coordinate.x);
        
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
