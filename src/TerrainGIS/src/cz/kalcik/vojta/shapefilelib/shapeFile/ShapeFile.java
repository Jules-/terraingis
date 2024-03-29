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

package cz.kalcik.vojta.shapefilelib.shapeFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field;
import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_File;
import cz.kalcik.vojta.shapefilelib.files.shp.SHP_File;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.files.shx.SHX_File;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * main class for loading/reading a "ShapeFile".<br>
 * a ShapeFile consists of at least the following files:<br>
 * 
 * <pre>
 * dBaseFile:      *.dbf
 * shapeFile:      *.shp
 * ShapeIndexFile: *.shx
 * </pre>
 * 
 * all this files must be located in the same given folder and start with the
 * same name.<br>
 * example: "my_shapefile.dbf", "my_shapefile.shp", "my_shapefile.shx"<br>
 * In each of the .shp, .shx, and .dbf files, the shapes in each file correspond
 * to each other in sequence. <br>
 * That is, the first record in the .shp file corresponds to the first record in
 * the .shx and .dbf files, and so on.<br>
 * <br>
 * 
 * <a href="http://en.wikipedia.org/wiki/Shapefile"
 * target=blank>http://en.wikipedia.org/wiki/Shapefile"</a><br>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class ShapeFile
{

    // library info
    public final static String _LIBRARY_NAME = "diewald_shapeFileReader";
    public final static String _LIBRARY_VERSION = "1.0";
    public final static String _LIBRARY_AUTHOR = "Thomas Diewald";
    public final static String _LIBRARY_LAST_EDIT = "2012.04.09";

    // constants
    public final static int SIZE_OF_DOUBLE = 8;
    public final static int SIZE_OF_INT = 4;
    public final static int SIZE_OF_MBR = 4 * SIZE_OF_DOUBLE;
    public final static int SIZE_OF_POINTXY = 2 * SIZE_OF_DOUBLE;

    // mandatory files
    private SHX_File shx_file; // index-File: contains offsets and content
                               // lengths of each record in the main shp-file.
    private DBF_File dbf_file; // dBASE-file: attribute format; columnar
                               // attributes for each shape, in dBase IV format.
    private SHP_File shp_file; // shape-File: contains geometry.
    
    private File qpj_file;
    private File cpg_file;

    /**
     * <pre>
     * init the ShapeFile, and load the following files:
     *   "path + filename.shx",
     *   "path + filename.dbf",
     *   "path + filename.shp"
     * </pre>
     * 
     * @param path
     * @param filename
     * @throws Exception
     */
    public ShapeFile(String path, String filename, String charset)
            throws Exception
    {

        // MAIN DIRECTORY
        File dir = new File(path);

        // GENERATE NEW READERS
        shx_file = new SHX_File(this, new File(dir, filename + ".shx"));
        dbf_file = new DBF_File(this, new File(dir, filename + ".dbf"), charset);
        shp_file = new SHP_File(this, new File(dir, filename + ".shp"));
        qpj_file = new File(dir, filename + ".qpj");
        cpg_file = new File(dir, filename + ".cpg");
    }

    /**
     * read shape file.
     * 
     * @return current instance
     * @throws Exception
     */
    public ShapeFile READ() throws Exception
    {
        shx_file.read();
        dbf_file.read();
        shp_file.read();
        return this;
    }
    
    /**
     * write projection to QPJ file
     * @param srsWKT
     * @throws IOException
     */
    public void writeQPJFile(String srsWKT) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(qpj_file));

        writer.write(srsWKT);
        writer.newLine();
        
        writer.close();
    }
    
    /**
     * write charset to cpg file
     * @param charset
     * @throws IOException
     */
    public void writeCPGFile(String charset) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(cpg_file));

        writer.write(charset);
        writer.newLine();
        
        writer.close();        
    }

    // ----------------------------------------------------------------------------
    // GET FILE READERS
    // ----------------------------------------------------------------------------
    /**
     * @return the Shape Index File (*.shx).
     */
    public SHX_File getFile_SHX()
    {
        return shx_file;
    }

    /**
     * @return the dBase File (*.dbf).
     */
    public DBF_File getFile_DBF()
    {
        return dbf_file;
    }

    /**
     * @return the Shape File (*.shp).
     */
    public SHP_File getFile_SHP()
    {
        return shp_file;
    }

    // ----------------------------------------------------------------------------
    // SIMPLE DATA ACCESS
    // ----------------------------------------------------------------------------

    // ----------------------------------------------------------------------------
    // SHAPE FILE
    // ----------------------------------------------------------------------------

    /**
     * get the number of shapes, contained in the shp-file.
     * 
     * @return number of Shapes
     */
    public int getSHP_shapeCount()
    {
        return shp_file.getShpShapes().size();
    }

    /**
     * get a list of all shapes. <br>
     * e.g. ArrayList<ShpPolygon> shape = shape_file.getShapes();
     * 
     * @param <T>
     *            Shape: ShpPolygon, ShpPolyLine, ShpPoint, or ShpMultiPoint
     * @return list of Shapes
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getSHP_shape()
    {
        return (ArrayList<T>) shp_file.getShpShapes();
    }

    /**
     * get a shapes by the given index.<br>
     * e.g. ShpPolygon shape = shape_file.getShape(0);
     * 
     * @param <T>
     *            Shape: ShpPolygon, ShpPolyLine, ShpPoint, or ShpMultiPoint
     * @param index
     * @return shape
     */
    @SuppressWarnings("unchecked")
    public <T> T getSHP_shape(int index)
    {
        return (T) getSHP_shape().get(index);
    }

    /**
     * get the shapeType of the shapes contained in the file.
     * 
     * @return shapeType
     */
    public ShpShape.Type getSHP_shapeType()
    {
        return shp_file.getHeader().getShapeType();
    }

    /**
     * same as:
     * {@link cz.kalcik.vojta.shapefilelib.files.shp.SHP_Header#getBoundingBox()}
     * 
     * @return boundingbox-values
     */
    public double[][] getSHP_boundingBox()
    {
        return shp_file.getHeader().getBoundingBox();
    }

    // ----------------------------------------------------------------------------
    // DATA BASE FILE
    // ----------------------------------------------------------------------------

    // DATA BASE FIELD
    public int getDBF_fieldCount()
    {
        return dbf_file.getFields().length;
    }

    public DBF_Field[] getDBF_field()
    {
        return dbf_file.getFields();
    }

    public DBF_Field getDBF_field(int index)
    {
        return dbf_file.getFields()[index];
    }

    // DATA BASE RECORD
    /**
     * get the number of data-base records, contained in the dbf-file (same as
     * number of shapes).
     * 
     * @return number of data-base records.
     */
    public int getDBF_recordCount()
    {
        return dbf_file.getContent().length;
    }

    /**
     * same as:
     * {@link cz.kalcik.vojta.shapefilelib.files.dbf.DBF_File#getContent()}
     * 
     * @return 2D-String-Table.
     */
    public String[][] getDBF_record()
    {
        return dbf_file.getContent();
    }

    public String[] getDBF_record(int index)
    {
        return dbf_file.getContent()[index];
    }

    public String getDBF_record(int row, int col)
    {
        return dbf_file.getContent()[row][col];
    }

}