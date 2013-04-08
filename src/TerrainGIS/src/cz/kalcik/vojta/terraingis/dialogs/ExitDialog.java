/**
 * 
 */
package cz.kalcik.vojta.terraingis.dialogs;

/**
 * @author jules
 *
 */
public class ExitDialog extends SimpleDialog
{
    @Override
    protected void execute()
    {
        getActivity().finish();
    }
}
