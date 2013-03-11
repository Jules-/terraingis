/**
 * 
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
