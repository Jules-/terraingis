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

import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * Shape: Polygon.<br>
 * 
 * <pre>
 * polygon:   consists of one or more rings (multiple outer rings).
 * ring:      four or more points ... closed, non-self-intersecting loop.
 * interiour: clockwise order of vertices.
 * same content as PolyLine.
 * possible ShapeTypes:
 *   Polygon   (  8 ), 
 *   PolygonZ  ( 18 ), 
 *   PolygonM  ( 28 ),
 * </pre>
 * 
 * @author thomas diewald (2012)
 * 
 */
public class ShpPolygon extends ShpPolyVertices
{
    public ShpPolygon(ShpShape.Type shape_type)
    {
        super(shape_type);
    }

    @Override
    protected void readRecordContent(ByteBuffer bb)
    {
        SHP_bbox[0][0] = bb.getDouble(); // x-min
        SHP_bbox[1][0] = bb.getDouble(); // y-min
        SHP_bbox[0][1] = bb.getDouble(); // x-max
        SHP_bbox[1][1] = bb.getDouble(); // y-max
        SHP_num_parts = bb.getInt(); // number of polygon-parts / rings
        SHP_num_points = bb.getInt(); // number of points (total of all parts)

        SHP_parts = new int[SHP_num_parts];
        for (int i = 0; i < SHP_num_parts; i++)
        {
            SHP_parts[i] = bb.getInt(); // index of the point-list (indicates
                                        // start-point of a polygon)
        }

        SHP_xyz_points = new double[SHP_num_points][3];
        for (int i = 0; i < SHP_num_points; i++)
        {
            SHP_xyz_points[i][0] = bb.getDouble(); // x - coordinate
            SHP_xyz_points[i][1] = bb.getDouble(); // y - coordinate
        }

        // if SHAPE-TYPE: 15
        if (shape_type.hasZvalues())
        {
            SHP_bbox[2][0] = bb.getDouble(); // z-min
            SHP_bbox[2][1] = bb.getDouble(); // z-max
            for (int i = 0; i < SHP_num_points; i++)
            {
                SHP_xyz_points[i][2] = bb.getDouble(); // z - coordinate
            }
        }

        // if SHAPE-TYPE: 15 | 25
        if (shape_type.hasMvalues())
        {
            SHP_range_m[0] = bb.getDouble(); // m-min
            SHP_range_m[1] = bb.getDouble(); // m-max
            SHP_m_values = new double[SHP_num_points];
            for (int i = 0; i < SHP_num_points; i++)
            {
                SHP_m_values[i] = bb.getDouble(); // m - value
            }
        }
    }
}
