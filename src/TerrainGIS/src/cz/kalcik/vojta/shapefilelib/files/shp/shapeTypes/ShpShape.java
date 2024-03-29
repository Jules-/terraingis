/**
 * shapefilelib
 * based on Thomas Diewald's diewald_shapeFileReader
 *                                 http://thomasdiewald.com/blog/?p=1382
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

package cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * Base class for Shapes.
 * 
 * @author thomas diewald (2012)
 * 
 */
public abstract class ShpShape
{
    protected ShpShape.Type shape_type;

    // RECORD HEADER
    protected int SHP_record_number, SHP_content_length, SHP_shape_type;
    protected int position_start, position_end, content_length; // for checking

    protected ShpShape(ShpShape.Type shape_type)
    {
        this.shape_type = shape_type;
    }

    /**
     * read the shape-data from the bytebuffer (buffer-position has to be
     * defined before).<br>
     * 
     * @param bb
     *            byte-buffer
     * @return current Shape-instance
     * @throws Exception
     */
    public ShpShape read(ByteBuffer bb) throws Exception
    {
        // 1) READ RECORD HEADER
        readRecordHeader(bb);

        // 2) READ RECORD CONTENT
        position_start = bb.position();

        // 2.1) check Shape Type
        bb.order(ByteOrder.LITTLE_ENDIAN);
        SHP_shape_type = bb.getInt();
        try
        {
            ShpShape.Type shape_type_tmp = ShpShape.Type.byID(SHP_shape_type);
            if (shape_type_tmp == shape_type)
            {
                readRecordContent(bb);
            }
            else if (shape_type_tmp == ShpShape.Type.NullShape)
            {
                ;
            }
            else if (shape_type_tmp != shape_type)
            {
                throw new Exception("(Shape) shape_type = " + shape_type_tmp
                        + ", but expected " + shape_type);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        position_end = bb.position();
        content_length = position_end - position_start;
        if (content_length != SHP_content_length * 2)
            throw new Exception("(Shape) content_length = " + content_length
                    + ", but expected " + SHP_content_length * 2);

        // if( SHP_File.__PRINT_ON_LOAD)
        // print();

        return this;
    }
    
    public void setBytes(ByteBuffer buffer)
    {      
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        buffer.putInt(SHP_record_number);
        buffer.putInt(SHP_content_length);
        
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putInt(shape_type.ID());
        
        setBytesRecord(buffer);
    }

    // getter, setter ==========================================================================
    /**
     * get the record number of the shape.
     * 
     * @return record number
     */
    public int getRecordNumber()
    {
        return SHP_record_number;
    }

    /**
     * set record number
     * @param recordNumber
     */
    public void setRecordNumber(int recordNumber)
    {
        SHP_record_number = recordNumber;
    }
    
    /**
     * get the Type of the Shape.
     * 
     * @return ShpShape.Type
     */
    public ShpShape.Type getShapeType()
    {
        return shape_type;
    }
    
    /**
     * @return size of record with header in 16 bit words
     */
    public int getSizeOfRecord()
    {
        return SHP_content_length + (2 * ShapeFile.SIZE_OF_INT) / 2;
    }    
    
    /**
     * @return content length in 16 bit words
     */
    public int getContentLength()
    {
        return SHP_content_length;
    } 
    // protected methods =======================================================================    
    
    protected void readRecordHeader(ByteBuffer bb)
    {
        bb.order(ByteOrder.BIG_ENDIAN);
        SHP_record_number = bb.getInt();
        SHP_content_length = bb.getInt();
    }
    
    /**
     * compute content length in 16 bit words
     */
    protected void computeLengthOfContent()
    {
        SHP_content_length = (sizeOfObject() + ShapeFile.SIZE_OF_INT) / 2;
    }
    
    // abstract protected methods =============================================================
    
    protected abstract void readRecordContent(ByteBuffer bb);

    /**
     * set bytes of record to bytebuffer
     * @param bb
     */
    protected abstract void setBytesRecord(ByteBuffer bb);
    
    /**
     * @return size of record in bytes
     */
    protected abstract int sizeOfObject();

    /**
     * load shape from geometry
     * @param geom
     */
    public abstract void loadFromJTS(Geometry geom);

    public abstract void print();

    // ----------------------------------------------------------------------------
    // Shape Type
    // ----------------------------------------------------------------------------
    public static enum Type
    {

        // Null_Shape ( 0 ),
        //
        // Point ( 1 ),
        // PolyLine ( 3 ),
        // Polygon ( 5 ),
        // MultiPoint ( 8 ),
        //
        // PointZ ( 11 ),
        // PolyLineZ ( 13 ),
        // PolygonZ ( 15 ),
        // MultiPointZ ( 18 ),
        //
        // PointM ( 21 ),
        // PolyLineM ( 23 ),
        // PolygonM ( 25 ),
        // MultiPointM ( 28 ),
        //
        // MultiPatch ( 31 )

        /** ID= 0 */
        NullShape(0, false, false),

        /** ID= 1 */
        Point(1, false, false),
        /** ID=11 */
        PointZ(11, true, true),
        /** ID=21 */
        PointM(21, false, true),

        /** ID= 3 */
        PolyLine(3, false, false),
        /** ID=13 */
        PolyLineZ(13, true, true),
        /** ID=23 */
        PolyLineM(23, false, true),

        /** ID= 5 */
        Polygon(5, false, false),
        /** ID=15 */
        PolygonZ(15, true, true),
        /** ID=25 */
        PolygonM(25, false, true),

        /** ID= 8 */
        MultiPoint(8, false, false),
        /** ID=18 */
        MultiPointZ(18, true, true),
        /** ID=28 */
        MultiPointM(28, false, true),

        /** ID=31 */
        MultiPatch(31, true, true);

        private int ID;
        private boolean has_z_values;
        private boolean has_m_values;

        private Type(int ID, boolean has_z_values, boolean has_m_values)
        {
            this.has_z_values = has_z_values;
            this.has_m_values = has_m_values;
            this.ID = ID;
        }

        public int ID()
        {
            return this.ID;
        }

        public static ShpShape.Type byID(int ID) throws Exception
        {
            for (ShpShape.Type st : ShpShape.Type.values())
                if (st.ID == ID)
                    return st;
            throw new Exception("ShapeType: " + ID + " does not exist");
        }

        public boolean hasZvalues()
        {
            return has_z_values;
        }

        public boolean hasMvalues()
        {
            return has_m_values;
        }

        public boolean isTypeOfPolygon()
        {
            return (this == Type.Polygon | this == Type.PolygonM | this == Type.PolygonZ);
        }

        public boolean isTypeOfPolyLine()
        {
            return (this == Type.PolyLine | this == Type.PolyLineM | this == Type.PolyLineZ);
        }

        public boolean isTypeOfPoint()
        {
            return (this == Type.Point | this == Type.PointM | this == Type.PointZ);
        }

        public boolean isTypeOfMultiPoint()
        {
            return (this == Type.MultiPoint | this == Type.MultiPointM | this == Type.MultiPointZ);
        }
    }

}
