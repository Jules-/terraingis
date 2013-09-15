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
import java.util.Locale;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * @author jules
 *
 */
public abstract class ShpPolyVertices extends ShpShape
{
    // SHAPE RECORD CONTENT
    protected double[][] SHP_bbox = new double[3][2]; // [x, y, z][min, max]
    protected double[] SHP_range_m = new double[2]; // [min, max]

    protected int SHP_num_parts, SHP_num_points;
    protected int[] SHP_parts;
    protected double[][] SHP_xyz_points; // [number of points][x,y,z]
    protected double[] SHP_m_values; // [number of points][m-value]

    protected double[][][] parts = null; // [number of parts][vertices][x, y,
                                       // z, w]
    
    protected ShpPolyVertices(Type shape_type)
    {
        super(shape_type);
    }

    @Override
    protected void setBytesRecord(ByteBuffer buffer)
    {
        buffer.putDouble(SHP_bbox[0][0]); // x-min
        buffer.putDouble(SHP_bbox[1][0]); // y-min
        buffer.putDouble(SHP_bbox[0][1]); // x-max
        buffer.putDouble(SHP_bbox[1][1]); // y-max
        buffer.putInt(SHP_num_parts);
        buffer.putInt(SHP_num_points);
        
        for(int i=0; i < SHP_num_parts; i++)
        {
            buffer.putInt(SHP_parts[i]);
        }
        
        for(int i=0; i < SHP_num_points; i++)
        {
            buffer.putDouble(SHP_xyz_points[i][0]);
            buffer.putDouble(SHP_xyz_points[i][1]);
        }
        
        // if SHAPE-TYPE: 13
        if (shape_type.hasZvalues())
        {
            throw new RuntimeException("Unimplemented");
        }

        // if SHAPE-TYPE: 13 | 23
        if (shape_type.hasMvalues())
        {
            throw new RuntimeException("Unimplemented");
        }
    }

    @Override
    protected int sizeOfObject()
    {
        int size = ShapeFile.SIZE_OF_MBR + 2 * ShapeFile.SIZE_OF_INT +
                SHP_num_parts * ShapeFile.SIZE_OF_INT +
                SHP_num_points * ShapeFile.SIZE_OF_POINTXY;
                
        // if SHAPE-TYPE: 15
        if (shape_type.hasZvalues())
        {
            size += 2 * ShapeFile.SIZE_OF_DOUBLE +
                    SHP_num_points * ShapeFile.SIZE_OF_DOUBLE;
        }

        // if SHAPE-TYPE: 15 | 25
        if (shape_type.hasMvalues())
        {
            size += 2 * ShapeFile.SIZE_OF_DOUBLE +
                    SHP_num_points * ShapeFile.SIZE_OF_DOUBLE;
        }        
        
        return size;
    }
    

    @Override
    public void loadFromJTS(Geometry geom)
    {
        Envelope envelope = geom.getEnvelopeInternal();
        
        SHP_bbox[0][0] = envelope.getMinX();
        SHP_bbox[1][0] = envelope.getMinY();
        SHP_bbox[0][1] = envelope.getMaxX();
        SHP_bbox[1][1] = envelope.getMaxY();
        
        SHP_num_parts = geom.getNumGeometries();
        SHP_num_points = geom.getNumPoints();
        
        // parts
        int partPosition = 0;
        SHP_parts = new int[SHP_num_parts];
        for(int i=0; i < SHP_num_parts; i++)
        {
            SHP_parts[i] = partPosition;
            partPosition += geom.getGeometryN(i).getNumPoints();
        }
        
        //points
        int countDimensions = shape_type.hasZvalues() ? 3 : 2; 
        SHP_xyz_points = new double[SHP_num_points][countDimensions];
        Coordinate[] points = geom.getCoordinates();
        
        for(int i=0; i < SHP_num_points; i++)
        {
            SHP_xyz_points[i][0] = points[i].x;
            SHP_xyz_points[i][1] = points[i].y;
            
            // if SHAPE-TYPE: 15
            if (shape_type.hasZvalues())
            {
                SHP_xyz_points[i][2] = points[i].z;
            }
        }
        
        computeLengthOfContent();
    }
    
    /**
     * get the number of points(vertices).
     * 
     * @return the number of points(vertices).
     */
    public int getNumberOfPoints()
    {
        return SHP_num_points;
    }

    /**
     * get the number of parts(Polygons)
     * 
     * @return the number of parts(Polygons).
     */
    public int getNumberOfParts()
    {
        return SHP_num_parts;
    }

    /**
     * get an array of all points(vertices).
     * 
     * @return an array of all points(vertices).
     */
    public double[][] getPoints()
    {
        return SHP_xyz_points;
    }
    
    @Override
    public void print()
    {
        System.out.printf(Locale.ENGLISH, "   _ _ _ _ _ \n");
        System.out.printf(Locale.ENGLISH, "  / SHAPE   \\_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n");
        System.out.printf(Locale.ENGLISH, "  |                                                    \\\n");
        System.out.printf(Locale.ENGLISH, "  |  <RECORD HEADER>\n");
        System.out.printf(Locale.ENGLISH, "  |    SHP_record_number       = %d\n", SHP_record_number);
        System.out.printf(Locale.ENGLISH, "  |    SHP_content_length      = %d bytes  (check: start/end/size = %d/%d/%d)\n", SHP_content_length*2, position_start, position_end, content_length);
        System.out.printf(Locale.ENGLISH, "  |\n");
        System.out.printf(Locale.ENGLISH, "  |  <RECORD CONTENT>\n");
        System.out.printf(Locale.ENGLISH, "  |    shape_type              = %s (%d)\n", shape_type, shape_type.ID() );
        System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: xmin, xmax    = %+7.3f, %+7.3f\n", SHP_bbox[0][0], SHP_bbox[0][1]);
        System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: ymin, ymax    = %+7.3f, %+7.3f\n", SHP_bbox[1][0], SHP_bbox[1][1]);
        System.out.printf(Locale.ENGLISH, "  |    SHP_bbox: zmin, zmax    = %+7.3f, %+7.3f\n", SHP_bbox[2][0], SHP_bbox[2][1]);
        System.out.printf(Locale.ENGLISH, "  |    SHP_measure: mmin, mmax = %+7.3f, %+7.3f\n", SHP_range_m[0], SHP_range_m[1]);
        System.out.printf(Locale.ENGLISH, "  |    SHP_num_parts           = %d\n", SHP_num_parts  );
        System.out.printf(Locale.ENGLISH, "  |    SHP_num_points          = %d\n", SHP_num_points );
//        for(int i = 0; i < SHP_num_parts; i++){
//          System.out.printf(Locale.ENGLISH, "  |     part_idx[%d] = %d\n", i, SHP_parts[i] );
//        }
    //    
        System.out.printf(Locale.ENGLISH, "  \\_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ /\n");
    }

    /**
     * get the BoundingBox..<br>
     * data storage: [x, y, z][min, max] <br>
     * 
     * @return 2d-array (double), dim-size:[3][2]
     */
    public double[][] getBoundingBox()
    {
        return SHP_bbox;
    }

    /**
     * get range of Measure-Values.<br>
     * data storage: [min, max] <br>
     * 
     * @return 1d-array (double), dim-size:[2]
     */
    public double[] getMeasureRange()
    {
        return SHP_range_m;
    }

    /**
     * generates a list of polygons or polylines, and returns a 3d-double array.<br>
     * [number of poly*][number of points per poly*][x, y, z, m].
     * 
     * @return 3d-double array.
     */
    public double[][][] getPointsAs3DArray()
    {
        // if the method was called before, we already have the array.
        if (parts != null)
        {
            return parts;
        }

        int[] indices = new int[SHP_num_parts + 1]; // generate new indices
                                                    // array
        System.arraycopy(SHP_parts, 0, indices, 0, SHP_num_parts); // copy start
                                                                   // indices
        indices[indices.length - 1] = SHP_num_points; // and add last index

        parts = new double[SHP_num_parts][][];
        for (int i = 0; i < indices.length - 1; i++)
        {
            int from = indices[i]; // start index
            int to = indices[i + 1]; // end-index + 1
            int size = to - from;
            parts[i] = new double[size][4];
            for (int j = from, idx = 0; j < to; j++, idx++)
            {
                parts[i][idx][0] = SHP_xyz_points[j][0]; // copy of x-value
                parts[i][idx][1] = SHP_xyz_points[j][1]; // copy of y-value
                parts[i][idx][2] = SHP_xyz_points[j][2]; // copy of z-value
                if (shape_type.hasMvalues())
                {
                    parts[i][idx][3] = SHP_m_values[j]; // copy of m-value
                }
            }
        }
        return parts;
    }

    /**
     * get the Measure Values as an Array.
     * 
     * @return measure-values. (size=.getNumberOfPoints()).
     */
    public double[] getMeasureValues()
    {
        return SHP_m_values;
    }
}