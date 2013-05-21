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
    
    /**
     * @return values without PK
     */
    public String[] getValuesWithoutPK()
    {
        String[] result = new String[mHeader.getCountColumnsWithoutPK()];
        int resultIndex = 0;
        
        int count = getCountValues();
        for(int i=0; i<count; i++)
        {
            if(!isColumnPK(i))
            {
                result[resultIndex] = mValues[i];
                resultIndex++;
            }
        }        
        
        return result;
    }
    // private methods =======================================================================
}
