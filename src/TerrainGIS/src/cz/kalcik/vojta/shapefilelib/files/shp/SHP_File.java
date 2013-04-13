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

package cz.kalcik.vojta.shapefilelib.files.shp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;

import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.shapefilelib.files.ShapeFileReader;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpMultiPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolyLine;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolygon;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.files.shx.SHX_File;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

//http://webhelp.esri.com/arcgisdesktop/9.2/index.cfm?TopicName=Shapefile_file_extensions
/**
 * Shape File Reader (*.shp).<br>
 * <br>
 * contains geometry/shapes of a certain type (ShpPoint, ShpMultiPoint,
 * ShpPolygon, ShpPolyLine).<br>
 * 
 * @author thomas diewald (2012)
 */
public class SHP_File extends ShapeFileReader
{

    /** enable/disable general info-logging. */
    public static boolean LOG_INFO = false;
    /** enable/disable logging of the header, while loading. */
    public static boolean LOG_ONLOAD_HEADER = false;
    /** enable/disable logging of the content, while loading. */
    public static boolean LOG_ONLOAD_CONTENT = false;

    private SHP_Header header;
    private ArrayList<ShpShape> shapes = new ArrayList<ShpShape>(); // works
                                                                    // independent
                                                                    // of any
                                                                    // *.shx
                                                                    // file.

    public SHP_File(ShapeFile parent_shapefile, File file) throws Exception
    {
        super(parent_shapefile, file);
    }

    @Override
    public void read() throws Exception
    {
        // READ HEADER
        header = new SHP_Header(parent_shapefile, file);
        header.read(bb);

        if (LOG_ONLOAD_HEADER)
            printHeader();

        ShpShape.Type shape_type = header.getShapeType();

        // READ CONTENT (depends on the Shape.Type)
        if (shape_type == ShpShape.Type.NullShape)
        {
            ;// TODO: handle NullShapes
        }
        else if (shape_type.isTypeOfPolygon())
        {
            while (bb.position() != bb.capacity())
                shapes.add(new ShpPolygon(shape_type).read(bb));
        }
        else if (shape_type.isTypeOfPolyLine())
        {
            while (bb.position() != bb.capacity())
                shapes.add(new ShpPolyLine(shape_type).read(bb));
        }
        else if (shape_type.isTypeOfPoint())
        {
            while (bb.position() != bb.capacity())
                shapes.add(new ShpPoint(shape_type).read(bb));
        }
        else if (shape_type.isTypeOfMultiPoint())
        {
            while (bb.position() != bb.capacity())
                shapes.add(new ShpMultiPoint(shape_type).read(bb));
        }
        else if (shape_type == ShpShape.Type.MultiPatch)
        {
            System.err
                    .println("(ShapeFile) Shape.Type.MultiPatch not supported at the moment.");
        }

        if (LOG_ONLOAD_CONTENT)
            printContent();

        if (LOG_INFO)
            // System.out.println("(ShapeFile) loaded *.shp-File: \""+file.getName()+"\",  shapes="+shapes.size()+"("+shape_type+")");
            System.out
                    .printf("(ShapeFile) loaded File: \"%s\", records=%d (%s-Shapes)\n",
                            file.getName(), shapes.size(), shape_type);
    }

    /**
     * @param envelope
     * @param type
     * @return bytes for save SHP file
     * @throws IOException 
     */
    public void write(Envelope envelope, ShpShape.Type type) throws IOException
    {
        int lengthOfFIle = getLengthOfFile();
        header = SHP_Header.getHeader(parent_shapefile, file, envelope, type, lengthOfFIle);
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthOfFIle * 2);
        
        byteBuffer.put(header.getBytes());
        for(ShpShape shape: shapes)
        {
            byteBuffer.put(shape.getBytes());
        }
        
        writeBytesToFile(byteBuffer);
    }
    
    // getter, setter ================================================================
    public SHP_Header getHeader()
    {
        return header;
    }

    /**
     * get the shapes of the file as an ArrayList.<br>
     * 
     * <pre>
     * elements can be of type (proper casting!):
     * ShpPoint
     * ShpMultiPoint
     * ShpPolygon
     * ShpPolyLine
     * </pre>
     * 
     * @return ArrayList with elements of type: ShpShape
     */
    public ArrayList<ShpShape> getShpShapes()
    {
        return shapes;
    }

    /**
     * set shapes
     * @param shapes
     */
    public void setShpShapes(ArrayList<ShpShape> shapes)
    {
        this.shapes = shapes;
    }
    
    @Override
    public void printHeader()
    {
        header.print();
    }

    @Override
    public void printContent()
    {
        System.out.printf(Locale.ENGLISH, "\n");
        System.out.printf(Locale.ENGLISH, "________________________< CONTENT >________________________\n");
        System.out.printf(Locale.ENGLISH, "  FILE: \"%s\"\n", file.getName());
        System.out.printf(Locale.ENGLISH, "\n");
        for( ShpShape shape: shapes )
        {
            shape.print();
        }
        System.out.printf(Locale.ENGLISH, "\n");
        System.out.printf(Locale.ENGLISH, "________________________< /CONTENT >________________________\n");
    }
    
    // private methods ================================================================================
    
    /**
     * @return length of file in 16 bit words
     */
    private int getLengthOfFile()
    {
        int size = SHP_Header.HEADER_LENGTH_BYTES / 2;
        
        for(ShpShape shape: shapes)
        {
            size += shape.getSizeOfRecord();
        }
        
        return size;
    }
}
