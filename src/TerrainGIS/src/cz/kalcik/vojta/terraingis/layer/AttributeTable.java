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
    public static String DATETIME_COLUMN = "datetime";
    public static AttributeType DATETIME_TYPE = AttributeType.TEXT;
    
    /**
     * Data type of attribute
     * @author jules
     *
     */
    public enum AttributeType
    {
        TEXT, INTEGER, REAL;
        
        public static AttributeType getType(String spatialiteType)
        {
            if(spatialiteType.equals("TEXT"))
            {
                return TEXT;
            }
            else if(spatialiteType.equals("INTEGER"))
            {
                return INTEGER;
            }
            else if(spatialiteType.equals("REAL"))
            {
                return REAL;
            }
            else
            {
                return null;
            }
        }
    };
    // attributes =============================================================
    private ArrayList<Column> mColumns = new ArrayList<AttributeTable.Column>();
    int pkIndex;
    
    // public methods =========================================================
    public void addColumn(String name, AttributeType type, boolean isPK)
    {
        if(isPK)
        {
            pkIndex = mColumns.size();
        }
        mColumns.add(new Column(name, type));
    }
    
    /**
     * crate SQL definition of columns
     * @return
     */
    public String createSQLColumns()
    {
        StringBuilder builder = new StringBuilder("(");
        
        int size = mColumns.size();
        for(int i=0; i < size; i++)
        {
            Column column = mColumns.get(i);
            builder.append(String.format("'%s' %s", column.name,
                    column.type));
            if(i == pkIndex)
            {
                builder.append(" PRIMARY KEY AUTOINCREMENT");
            }
            
            if(i < size-1)
            {
                builder.append(", ");
            }
        }
        builder.append(")");
        
        return builder.toString();
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
