/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

/**
 * @author jules
 *
 */
public class AttributeHeader
{
    // constants ==============================================================
    public static String DATETIME_COLUMN = "datetime";
    public static AttributeType DATETIME_TYPE = AttributeType.TEXT;
    
    // attributes =============================================================
    private ArrayList<Column> mColumns = new ArrayList<AttributeHeader.Column>();
    int pkIndex = -1;
    
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
    
    /**
     * create SQL definition columns for insert attributes
     * begin with coma
     * @param showPK
     * @return
     */
    public String getInsertSQLColumns(boolean showPK)
    {
        StringBuilder builder = new StringBuilder();
        
        int size = mColumns.size();
        for(int i=0; i < size; i++)
        {
            if(showPK || i != pkIndex)
            {
                builder.append(", ");
                Column column = mColumns.get(i);
                builder.append(String.format("'%s'", column.name));
            }
        }
        
        return builder.toString();
    }
    
    /**
     * create question marks for SQL query
     * begin with coma
     * @param showPK
     * @return
     */
    public String getInsertSQLArgs(boolean showPK)
    {
        StringBuilder builder = new StringBuilder();
        
        int size = showPK || pkIndex >= 0 ? mColumns.size() : mColumns.size() - 1;
        for(int i=0; i < size; i++)
        {
            builder.append(", ?");
        }
        
        return builder.toString();
    }
    
    /**
     * @return count of columns
     */
    public int getCountColumns()
    {
        return mColumns.size();
    }
    
    public AttributeType getColumnType(int index)
    {
        return mColumns.get(index).type;
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
    public class Column
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
