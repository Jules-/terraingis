/**
 * 
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
