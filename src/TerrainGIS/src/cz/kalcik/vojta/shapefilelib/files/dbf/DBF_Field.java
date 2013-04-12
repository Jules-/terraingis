/**
 * diewald_shapeFileReader.
 * 
 * a Java Library for reading ESRI-shapeFiles (*.shp, *.dfb, *.shx).
 * 
 * 
 * Copyright (c) 2012 Thomas Diewald
 *
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package cz.kalcik.vojta.shapefilelib.files.dbf;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * class DBF_Field.<br>
 * <br>
 * information about type, length, name (can be useful for converting the
 * values)<br>
 * of a field of a *.dbf-file table (2D-String-Table).<br>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class DBF_Field
{
    /** field-length in bytes (used for reading from the bytebuffer). */
    public static final int FIELD_LENGTH = 32;
    public static final int FIELD_NAME_LENGTH = 10;
    public static final int FIELD_TYPE_POSITION = 11;
    
    @SuppressWarnings("unused")
    private DBF_File parent_dbasefile;
    private int index = 0;
    private String DBF_field_name = "";
    private char DBF_field_type;
    @SuppressWarnings("unused")
    private int DBF_field_displacement;
    private int DBF_field_length;
    @SuppressWarnings("unused")
    private byte DBF_field_flag;
    @SuppressWarnings("unused")
    private int DBF_autoincr_next;
    @SuppressWarnings("unused")
    private byte DBF_autoincr_step;

    /**
     * create new Field
     * 
     * @param parent_dbasefile
     *            parent dbf-file
     * @param bb
     *            ByteBuffer to read from
     * @param index
     *            field index
     */
    public DBF_Field(DBF_File parent_dbasefile, int index)
    {
        this.parent_dbasefile = parent_dbasefile;
        this.index = index;
    }

    public void readData(ByteBuffer bb, String charset)
    {
        byte[] string_tmp = new byte[11]; // 0-11
        bb.get(string_tmp);
        try
        {
            DBF_field_name = new String(string_tmp, charset); // 0-terminated
                                                                   // String
            DBF_field_name = DBF_field_name.substring(0,
                    DBF_field_name.indexOf('\0')); // get proper name
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        DBF_field_type = (char) bb.get();
        DBF_field_displacement = bb.getInt();
        DBF_field_length = bb.get() & 0xFF; // so we get values from 0-255.
        DBF_field_flag = bb.get();
        DBF_autoincr_next = bb.getInt();
        DBF_autoincr_step = bb.get();
    }
    
    /**
     * @param charset
     * @return field like ByteBuffer
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getFieldBytes(String charset) throws UnsupportedEncodingException
    {
        ByteBuffer result = ByteBuffer.allocate(FIELD_LENGTH);
        result.order(ByteOrder.LITTLE_ENDIAN);
        
        result.put(parent_dbasefile.getBytes(DBF_field_name,
                FIELD_NAME_LENGTH, charset));
        
        result.position(FIELD_TYPE_POSITION);
        result.putChar(DBF_field_type);
        result.position(result.position()+4);
        result.put((byte)DBF_field_length);
        
        return result;
    }

    /**
     * get DBF_field_length bytes for record
     * @param value
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public byte[] getValueBytes(String value, String charset) throws UnsupportedEncodingException
    {
        byte[] result = new byte[DBF_field_length];
        Arrays.fill(result, (byte) ' ');
        
        byte[] valueBytes = parent_dbasefile.getBytes(value, DBF_field_length, charset);
        int firstIndex = 0;
        int lengthSubArray = valueBytes.length;
        // character
        if(DBF_field_type == FieldType.C.ID())
        {
            firstIndex = 0;
        }
        // numeric
        else if(DBF_field_type == FieldType.N.ID())
        {
            firstIndex = result.length - valueBytes.length;
        }
        
        for(int i=0; i < lengthSubArray; i++)
        {
            result[firstIndex+i] = valueBytes[i];
        }
        
        return result;
    }    
    
    /**
     * print the fields' data.
     */
    public void print()
    {
        DBF_Field field = this;
        String name = field.getName();
        int length = field.getLength();
        char type = field.getType();
        String type_name = DBF_Field.FieldType.byID(type).longName();
        System.out
                .printf("  DBF_Field[%d]: name: %-10s; length(chars): %3d; type: %1c(=%s)\n",
                        index, name, length, type, type_name);
    }

    /**
     * get the name of the field.
     * 
     * @return field name
     */
    public String getName()
    {
        return DBF_field_name;
    }

    
    public void setName(String name)
    {
        DBF_field_name = new String(name);
    }
    
    /**
     * Type Information can be used for convert the values from the *.dbf record
     * content.
     * 
     * <pre>
     * TYPES: (see DBF_Field.FieldType...)
     * C (Character) 
     * D (Date)      
     * N (Numeric)   
     * L (Logical)   
     * M (Memo)
     * </pre>
     */
    public char getType()
    {
        return DBF_field_type;
    }

    public void setType(char type)
    {
        DBF_field_type = type;
    }
    
    /**
     * get the length of the field. (number of chars).
     * 
     * @return length of the field.
     */
    public int getLength()
    {
        return DBF_field_length;
    }

    public void getLength(int length)
    {
        DBF_field_length = length;
    }
    
    /**
     * 
     * @return the index of the field, starting at 0. (... column of the
     *         2D-String-table).
     */
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
    
    /**
     * information about the field-type. (can be used for converting the
     * db-value)
     * 
     * @author thomas diewald (2012)
     * 
     */
    public enum FieldType
    {

        /** ID=C, name/datatype="Character" */
        C('C', "Character"),
        /** ID=D, name/datatype="Date" */
        D('D', "Date"),
        /** ID=N, name/datatype="Numeric" */
        N('N', "Numeric"),
        /** ID=L, name/datatype="Logical" */
        L('L', "Logical"),
        /** ID=M, name/datatype="Memo" */
        M('M', "Memo"),
        /** ID=\0, name/datatype="Undefined" */
        UNDEFINED('\0', "Undefined");

        private String name_long;
        private char ID;

        private FieldType(char ID, String name_long)
        {
            this.ID = ID;
            this.name_long = name_long;
        }

        /**
         * find a FieldType by a given char
         * 
         * @param ID
         * @return FieldType
         */
        public static FieldType byID(char ID)
        {
            for (FieldType type : FieldType.values())
            {
                if (type.ID == ID)
                    return type;
            }
            return UNDEFINED;
        }

        /**
         * get long name of the field.
         * 
         * @return long name of the field.
         */
        public String longName()
        {
            return name_long;
        }

        /**
         * get the ID of the field as a char.
         * 
         * @return ID of the field as a char
         */
        public char ID()
        {
            return ID;
        }
    }
}
