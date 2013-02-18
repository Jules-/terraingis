/**
 * 
 */
package cz.kalcik.vojta.terraingis.exception;

/**
 * @author jules
 * class for problem with creating object
 */
public class CreateObjectException extends TerrainGISException
{
    private static final long serialVersionUID = 1L;

    public CreateObjectException(String m)
    {
        super(m);
    }

}
