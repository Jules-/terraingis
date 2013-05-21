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
package cz.kalcik.vojta.terraingis.io;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpMultiPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolyLine;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolygon;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayerType;

/**
 * @author jules
 * class for iteration geometries
 */
class ShapeFileIterator implements Iterator<ShapeFileRecord>
{
    // attributes =====================================================================
    ShapeFile mFile;
    ShpShape.Type mType;
    double[][] mMultiPoints;
    int mCount;
    int mIndex = 0;
    int mSubCount = 0;
    int mSubIndex = 0;
    
    // public methods ==================================================================
    public ShapeFileIterator(ShapeFile file)
    {
        mFile = file;
        mType = mFile.getSHP_shapeType();
        mCount = mFile.getSHP_shapeCount();
    }
    
    @Override
    public boolean hasNext()
    {
        return mIndex < mCount;
    }

    @Override
    public ShapeFileRecord next()
    {
        ArrayList<Coordinate> resultPoints = getPoints();
        
        ShapeFileRecord result = new ShapeFileRecord();
        result.setGeometry(VectorLayer.createGeometry(resultPoints,
                VectorLayerType.shapefileToSpatialite(mType)));
        result.setAttributes(mFile.getDBF_record(mIndex));
        incrementIndex();
        
        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    // private methods ==============================================================================
    /**
     * create points for result geometry
     * @return
     */
    private ArrayList<Coordinate> getPoints()
    {
        ArrayList<Coordinate> resultPoints = new ArrayList<Coordinate>();
        if(mType.isTypeOfPoint())
        {
            ShpPoint shape = mFile.getSHP_shape(mIndex);
            double[] values = shape.getPoint();
            resultPoints.add(new Coordinate(values[0], values[1]));
        }
        else if(mType.isTypeOfMultiPoint())
        {
            if(mSubCount == 0)
            {
                ShpMultiPoint shape = mFile.getSHP_shape(mIndex);
                mSubCount = shape.getNumberOfPoints();
                mMultiPoints = shape.getPoints();
            }
            resultPoints.add(new Coordinate(mMultiPoints[mSubIndex][0], mMultiPoints[mSubIndex][1]));
        }
        else if(mType.isTypeOfPolyLine() || mType.isTypeOfPolygon())
        {
            int count = 0;
            double[][] points = null;
            
            if(mType.isTypeOfPolyLine())
            {
                ShpPolyLine shape = mFile.getSHP_shape(mIndex);
                count = shape.getNumberOfPoints();
                points = shape.getPoints();
            }
            else if(mType.isTypeOfPolygon())
            {
                ShpPolygon shape = mFile.getSHP_shape(mIndex);
                count = shape.getNumberOfPoints();
                points = shape.getPoints();
            }
                
                
            for(int i=0; i < count; i++)
            {
                resultPoints.add(new Coordinate(points[i][0], points[i][1]));
            }
        }
        
        return resultPoints;
    }
    
    /**
     * increment index of objects
     */
    private void incrementIndex()
    {
        if(mType.isTypeOfMultiPoint())
        {
            mSubIndex++;
            if(mSubIndex >= mSubCount)
            {
                mSubCount = 0;
                mSubIndex = 0;
                mIndex++;
            }
        }
        else
        {            
            mIndex++;
        }        
    }
    
    // classes =============================================================================================
}