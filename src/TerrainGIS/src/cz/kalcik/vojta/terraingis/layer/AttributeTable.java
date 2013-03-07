/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

/**
 * @author jules
 *
 */
public class AttributeTable
{
    // constants ==============================================================
    public enum AttributeType
    {
        TEXT, NUMBER, DATETIME;
        
        public static AttributeType getType(String spatialiteType)
        {
            if(spatialiteType.equals("TEXT"))
            {
                return TEXT;
            }
            else if(spatialiteType.equals("INTEGER") || spatialiteType.equals("REAL"))
            {
                return NUMBER;
            }
            else
            {
                return null;
            }
        }
    };
    // attributes =============================================================
    private ArrayList<Column> mColumns = new ArrayList<AttributeTable.Column>();
    // public methods =========================================================
    public void addColumn(String name, AttributeType type)
    {
        mColumns.add(new Column(name, type));
    }
    
    // getter, setter =========================================================
        
    /**
     * @return the mColumns
     */
    public ArrayList<Column> getColumns()
    {
        return mColumns;
    }

    // classes ================================================================
    class Column
    {
        public String name;
        public AttributeType type;
        
        public Column(String name, AttributeType type)
        {
            this.name = name;
            this.type = type;
        }
    }
}
