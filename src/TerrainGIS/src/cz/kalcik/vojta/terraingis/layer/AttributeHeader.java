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
    
    // public methods =========================================================
    public void addColumn(String name, AttributeType type, boolean isPK)
    {
        mColumns.add(new Column(name, type, isPK));
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
            if(column.isPK)
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
        
        for(Column column : mColumns)
        {
            if(showPK || !column.isPK)
            {
                builder.append(", ");
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
        
        int size = showPK ? mColumns.size() : mColumns.size() - countPKColumns();
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

    /**
     * @return count of columns without primary keys
     */
    public int getCountColumnsWithoutPK()
    {
        return mColumns.size() - countPKColumns();
    }
    
    /**
     * @param index
     * @return type of column with index
     */
    public AttributeType getColumnType(int index)
    {
        return mColumns.get(index).type;
    }

    /**
     * @param index
     * @return true if column is PK
     */
    public boolean isColumnPK(int index)
    {
        return mColumns.get(index).isPK;
    }
    
    /**
     * @param index
     * @return column with index
     */
    public Column getColumn(int index)
    {
        return mColumns.get(index);
    }
    // getter, setter =========================================================
        
    /**
     * @return the mColumns
     */
    public ArrayList<Column> getColumns()
    {
        return mColumns;
    }

    // private methods ========================================================
    /**
     * @return count of primary keys
     */
    private int countPKColumns()
    {
        int count = 0;
        
        for(Column column : mColumns)
        {
            if(column.isPK)
            {
                count++;
            }
        }
        
        return count;
    }
    
    // classes ================================================================
    public class Column
    {
        public String name;
        public AttributeType type;
        public boolean isPK;
        
        public Column(String name, AttributeType type, boolean isPK)
        {
            this.name = name;
            this.type = type;
            this.isPK = isPK;
        }
    }
}
