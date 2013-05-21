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
            builder.append(String.format("\"%s\" %s", column.name,
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
     * @param showPK
     * @param showFirstComa - if is set show first coma
     * @return
     */
    public String getComaNameColumns(boolean showPK, boolean showFirstComa)
    {
        StringBuilder builder = new StringBuilder();
        boolean firstLoop = true;
        
        for(Column column : mColumns)
        {
            if(showPK || !column.isPK)
            {
                if(!firstLoop || showFirstComa)
                {
                    builder.append(", ");
                }
                
                builder.append(String.format("\"%s\"", column.name));
                
                firstLoop = false;
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
     * return set part of command for SQL UPDATE
     * @param showPK
     * @return
     */
    public String getUpdateSQLArgs(boolean showPK)
    {
        StringBuilder builder = new StringBuilder();
        boolean firstLoop = true;
        
        for(Column column : mColumns)
        {
            if(showPK || !column.isPK)
            {
                if(!firstLoop)
                {
                    builder.append(", ");
                }
                
                builder.append(String.format("\"%s\"=?", column.name));
                
                firstLoop = false;
            }
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
