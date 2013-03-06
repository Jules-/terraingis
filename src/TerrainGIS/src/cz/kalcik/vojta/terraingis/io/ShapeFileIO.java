/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpMultiPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolyLine;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolygon;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;

import android.util.Log;

/**
 * @author jules
 *
 */
public class ShapeFileIO
{
    // singleton code =====================================================================
    
    private static ShapeFileIO instance = new ShapeFileIO();
    
    private ShapeFileIO() { }
    
    public static ShapeFileIO getInstance()
    {
        return instance;
    }
    
    // public methods =====================================================================
    /**
     * load shapefile layer
     * @param context
     * @param file
     * @throws Exception 
     */
    public void load(String folder, String filename, String layerName, int srid) throws Exception
    {
        ShapeFile shapeFile = null;

        shapeFile = new ShapeFile(folder, filename);
        shapeFile.READ();
        
        ShpShape.Type type = shapeFile.getSHP_shapeType();
        
        LayerManager layerManager = LayerManager.getInstance();
        SpatiaLiteManager spatialiteManager = layerManager.getSpatialiteManager();
        
        spatialiteManager.createEmptyLayer(
                layerName, SpatiaLiteManager.GEOMETRY_COLUMN_NAME,
                getTypeString(type), srid);
        
        layerManager.loadSpatialite();
        VectorLayer layer = layerManager.getLayerByName(layerName);
        layer.importGeometries(new ObjectIterator(shapeFile));
    }
    
    // private methods =====================================================================
    /**
     * @param type
     * @return type of layer
     */
    private VectorLayerType getType(ShpShape.Type type)
    {
        if(type.isTypeOfPoint() || type.isTypeOfMultiPoint())
        {
            return VectorLayerType.POINT;
        }
        else if(type.isTypeOfPolyLine())
        {
            return VectorLayerType.LINE;
        }
        else if(type.isTypeOfPolygon())
        {
            return VectorLayerType.POLYGON;
        }
        
        return null;
    }
    
    /**
     * @param type
     * @return string spatialite type of layer
     */
    private String getTypeString(ShpShape.Type type)
    {        
        return getType(type).getSpatialiteType();
    }
    
    // classes ==========================================================================
    
    class ObjectIterator implements Iterator<Geometry>
    {
        ShapeFile mFile;
        ShpShape.Type mType;
        double[][] mMultiPoints;
        int mCount;
        int mIndex = 0;
        int mSubCount = 0;
        int mSubIndex = 0;
        
        public ObjectIterator(ShapeFile file)
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
        public Geometry next()
        {
            ArrayList<Coordinate> resultPoints = new ArrayList<Coordinate>();
            if(mType.isTypeOfPoint())
            {
                ShpPoint shape = mFile.getSHP_shape(mIndex);
                double[] values = shape.getPoint();
                resultPoints.add(new Coordinate(values[0], values[1]));
                mIndex++;
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
                
                mSubIndex++;
                if(mSubIndex >= mSubCount)
                {
                    mSubCount = 0;
                    mSubIndex = 0;
                    mIndex++;
                }
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
                
                mIndex++;
            }
            
            return VectorLayer.createGeometry(resultPoints, getType(mType));
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }    
    }
}
