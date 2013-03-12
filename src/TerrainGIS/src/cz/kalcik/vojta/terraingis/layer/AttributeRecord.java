/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

/**
 * @author jules
 *
 */
public class AttributeRecord
{
    // attributes ===========================================================================
    private AttributeHeader mHeader;
    private String[] mValues;
    
    // public methods =======================================================================
    public AttributeRecord(AttributeHeader header, String[] values)
    {
        mHeader = header;
        assert values.length == header.getCountColumns() : "Count columns and values must be same!";
        mValues = values;
    }
    
    /**
     * @param index
     * @return type of value in column with index
     */
    public AttributeType getColumnType(int index)
    {
        return mHeader.getColumnType(index);
    }
   
    /**
     * @param index
     * @return true if column is PK
     */
    public boolean isColumnPK(int index)
    {
        return mHeader.isColumnPK(index);
    }
    
    /**
     * @return count of values
     */
    public int getCountValues()
    {
        return mValues.length;
    }
    
    /**
     * trim values
     */
    public void trimValues()
    {
        int count = getCountValues();
        for(int i=0; i<count; i++)
        {
            mValues[i] = mValues[i].trim();
        }
    }
    
    // getter setter =========================================================================
    
    /**
     * @return the mValues
     */
    public String[] getValues()
    {
        return mValues;
    }
    
    // private methods =======================================================================
}
