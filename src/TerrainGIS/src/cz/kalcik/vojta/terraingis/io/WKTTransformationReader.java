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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jules
 *
 */
public class WKTTransformationReader
{
    private String mNameCS = null;
    
    public WKTTransformationReader(String text)
    {
        parseText(text);
    }
    
    // getter, setter ======================================================
    /**
     * @return name of CS
     */
    public String getNameCS()
    {
        return mNameCS;
    }    
    
    // private methods =====================================================
    /**
     * check name of CS
     * @param text
     */
    private void parseText(String text)
    {
        // TODO parsing!!!
        
        Pattern pattern = null;
        if(text.startsWith("PROJCS"))
        {
            pattern = Pattern.compile("PROJCS\\[\"(.+?)\"");
        }
        else if(text.startsWith("GEOGCS"))
        {
            pattern = Pattern.compile("GEOGCS\\[\"(.+?)\"");
        }
        else
        {
            return;
        }
        
        Matcher matcher = pattern.matcher(text);
        while (matcher.find())
        {
            mNameCS = matcher.group(1);
            return;
        }
    }
}
