/**
 * 
 */
package cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes;

import java.nio.ByteBuffer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * @author jules
 *
 */
public abstract class ShpPolyPoints extends ShpShape
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
    
    protected ShpPolyPoints(Type shape_type)
    {
        super(shape_type);
    }

    @Override
    protected void setBytesRecord(ByteBuffer bb)
    {
        bb.putDouble(SHP_bbox[0][0]); // x-min
        bb.putDouble(SHP_bbox[1][0]); // y-min
        bb.putDouble(SHP_bbox[0][1]); // x-max
        bb.putDouble(SHP_bbox[1][1]); // y-max
        bb.putInt(SHP_num_parts);
        bb.putInt(SHP_num_points);
        
        for(int i=0; i < SHP_num_parts; i++)
        {
            bb.putInt(SHP_parts[i]);
        }
        
        for(int i=0; i < SHP_num_points; i++)
        {
            bb.putDouble(SHP_xyz_points[i][0]);
            bb.putDouble(SHP_xyz_points[i][1]);
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
    protected int sizeOfRecord()
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
    }
}
