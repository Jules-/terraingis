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

package cz.kalcik.vojta.shapefilelib.files.shx;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.shapefilelib.files.ShapeFileReader;
import cz.kalcik.vojta.shapefilelib.files.shp.SHP_Header;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * Shape Index File Reader (*.shx).<br>
 * <br>
 * stores offset and length of record-items in a ShapeFile (*.shp).<br>
 * can be used for diretly accessing records.<br>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class SHX_File extends ShapeFileReader
{

    /** enable/disable general info-logging. */
    public static boolean LOG_INFO = false;
    /** enable/disable logging of the header, while loading. */
    public static boolean LOG_ONLOAD_HEADER = false;
    /** enable/disable logging of the content, while loading. */
    public static boolean LOG_ONLOAD_CONTENT = false;

    private SHP_Header header;
    private int[] SHX_shape_offsets;
    private int[] SHX_shape_content_lengths;

    public SHX_File(ShapeFile parent_shapefile, File file) throws Exception
    {
        super(parent_shapefile, file);
    }

    @Override
    public void read() throws Exception
    {
        ByteBuffer bb = getFileBytes();
        // READ HEADER
        header = new SHP_Header(parent_shapefile, file);
        header.read(bb);
        if (LOG_ONLOAD_HEADER)
            printHeader();

        // READ RECORDS
        bb.order(ByteOrder.BIG_ENDIAN);

        int number_of_bytes = bb.capacity() - bb.position();
        int number_of_ints = number_of_bytes / 4;
        int number_of_records = number_of_ints / 2;

        SHX_shape_offsets = new int[number_of_records];
        SHX_shape_content_lengths = new int[number_of_records];

        for (int i = 0; i < SHX_shape_offsets.length; i++)
        {
            SHX_shape_offsets[i] = bb.getInt();
            SHX_shape_content_lengths[i] = bb.getInt();
        }
        if (LOG_ONLOAD_CONTENT)
            printContent();

        if (LOG_INFO)
            System.out.printf("(ShapeFile) loaded File: \"%s\", records=%d\n",
                    file.getName(), SHX_shape_offsets.length);
    }

    /**
     * @param envelope
     * @param type
     * @param shapes
     * @return bytes for save SHX file
     * @throws IOException 
     */
    public void write(Envelope envelope, ShpShape.Type type, ArrayList<ShpShape> shapes)
            throws IOException
    {
        int countShapes = shapes.size();
        SHX_File shx = parent_shapefile.getFile_SHX();
        int lengthOfFile = (SHP_Header.HEADER_LENGTH_BYTES +
                countShapes * 2 * ShapeFile.SIZE_OF_INT) / 2;
        header = SHP_Header.getHeader(parent_shapefile, shx.getFile(),
                envelope, type, lengthOfFile); 

        int currentOffset = SHP_Header.HEADER_LENGTH_BYTES / 2;
        ByteBuffer buffer = ByteBuffer.allocate(lengthOfFile * 2);

        header.setBytes(buffer);
        
        buffer.order(ByteOrder.BIG_ENDIAN);
        for(int i=0; i < countShapes; i++)
        {
            buffer.putInt(currentOffset);
            ShpShape shape = shapes.get(i);
            buffer.putInt(shape.getContentLength());
            currentOffset += shape.getSizeOfRecord(); 
        }
        
        writeBytesToFile(buffer);       
    }
    
    public SHP_Header getHeader()
    {
        return header;
    }

    public void setHeader(SHP_Header header)
    {
        this.header = header; 
    }
    
    /**
     * get an array of offset-values (in bytes) that can be used for direct
     * access of *.shp-file record-.items.<br>
     * 
     * @return offset-values as int[] array.
     */
    public int[] getRecordOffsets()
    {
        return SHX_shape_offsets;
    }

    /**
     * get an array of length-values, which indicate the size (in bytes) of
     * *.shp-file record-.items.<br>
     * 
     * @return length/size-values as int[] array.
     */
    public int[] getRecordLenghts()
    {
        return SHX_shape_content_lengths;
    }

    @Override
    public void printHeader()
    {
        header.print();
    }

    @Override
    public void printContent()
    {
        System.out.printf(Locale.ENGLISH, "\n________________________< CONTENT >________________________\n");
        System.out.printf(Locale.ENGLISH, "  FILE: \"%s\"\n", file.getName());
        System.out.printf(Locale.ENGLISH, "\n");
        for(int i = 0; i < SHX_shape_offsets.length; i++){
          System.out.printf("  [%4d] offset(bytes): %8d; record_length(bytes): %8d\n", i, SHX_shape_offsets[i], SHX_shape_content_lengths[i]);
        }
        System.out.printf(Locale.ENGLISH, "________________________< /CONTENT>________________________\n");
    }
}
