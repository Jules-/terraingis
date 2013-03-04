/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;

import java.io.File;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;
import diewald_shapeFile.files.shp.shapeTypes.ShpMultiPoint;
import diewald_shapeFile.files.shp.shapeTypes.ShpPoint;
import diewald_shapeFile.files.shp.shapeTypes.ShpPolyLine;
import diewald_shapeFile.files.shp.shapeTypes.ShpPolygon;
import diewald_shapeFile.files.shp.shapeTypes.ShpShape;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @author jules
 *
 */
public class ShapeFile
{
    // singleton code =====================================================================
    
    private static ShapeFile instance = new ShapeFile();
    
    private ShapeFile() { }
    
    public static ShapeFile getInstance()
    {
        return instance;
    }
    
    // public methods =====================================================================
    /**
     * load shapefile layer
     * @param context
     * @param file
     */
    public void load(Context context, File file)
    {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        diewald_shapeFile.shapeFile.ShapeFile shapeFile =null;
        
        try
        {
            if(dotIndex == -1)
            {
                throw new Exception("In file name is not dot.");
            }
            
            name = name.substring(0, dotIndex);        
            shapeFile = new diewald_shapeFile.shapeFile.ShapeFile(
                    file.getParent(), name);
            shapeFile.READ();
            
            ShpShape.Type type = shapeFile.getSHP_shapeType();
            
            LayerManager layerManager = LayerManager.getInstance();
            SpatiaLiteManager spatialiteManager = layerManager.getSpatialiteManager();
            
            // FIXME srid
            if(!spatialiteManager.createEmptyLayer(
                    name, SpatiaLiteManager.GEOMETRY_COLUMN_NAME,
                    getType(type), SpatiaLiteManager.EPSG_LONLAT))
            {
                throw new Exception("Can not create table.");
            }
            
            layerManager.loadSpatialite();
            VectorLayer layer = layerManager.getLayerByName(name);
            
            if(type.isTypeOfPoint())
            {
                ArrayList<ShpPoint> points = shapeFile.getSHP_shape();
                for(ShpPoint point: points)
                {
                    importPoint(layer, point.getPoint());
                }
            }
            else if(type.isTypeOfMultiPoint())
            {
                ArrayList<ShpMultiPoint> multiPoints = shapeFile.getSHP_shape();
                for(ShpMultiPoint multiPoint: multiPoints)
                {
                    int count = multiPoint.getNumberOfPoints();
                    double[][] values = multiPoint.getPoints();
                    for(int i=0; i < count; i++)
                    {
                        importPoint(layer, values[i]);
                    }                    
                }                
            }
            else if(type.isTypeOfPolyLine())
            {
                ArrayList<ShpPolyLine> objects = shapeFile.getSHP_shape();
                for(ShpPolyLine object: objects)
                {
                    importMultiObjects(layer,
                            object.getPointsAs3DArray(), 
                            object.getNumberOfParts(),
                            object.getNumberOfPoints());
                }  
            }
            else if(type.isTypeOfPolygon())
            {
                ArrayList<ShpPolygon> objects = shapeFile.getSHP_shape();
                for(ShpPolygon object: objects)
                {
                    importMultiObjects(layer,
                            object.getPointsAs3DArray(), 
                            object.getNumberOfParts(),
                            object.getNumberOfPoints());
                }  
            }
            
            spatialiteManager.reopen();
        }
        catch (Exception e)
        {
            Toast.makeText(context, R.string.load_shapefile_error, Toast.LENGTH_LONG).show();
            Log.e("TerrainGIS", e.getMessage());
        }
    }
    
    // private methods =====================================================================
    /**
     * @param type
     * @return string spatialite type of layer
     */
    private String getType(ShpShape.Type type)
    {
        if(type.isTypeOfPoint() || type.isTypeOfMultiPoint())
        {
            return VectorLayerType.POINT.getSpatialiteType();
        }
        else if(type.isTypeOfPolyLine())
        {
            return VectorLayerType.LINE.getSpatialiteType();
        }
        else if(type.isTypeOfPolygon())
        {
            return VectorLayerType.POLYGON.getSpatialiteType();
        }
        
        return null;
    }

    /**
     * import parts of objects
     * @param layer
     * @param multiObjects
     * @param countParts
     * @param countPoints
     */
    private void importMultiObjects(VectorLayer layer, double[][][] multiObjects, int countParts, int countPoints)
    {
        for(int i=0; i < countParts; i++)
        {
            for(int j=0; j < countPoints; j++)
            {
                layer.addPoint(new Coordinate(multiObjects[i][j][0], multiObjects[i][j][1]), SpatiaLiteManager.EPSG_LONLAT);
            }
            
            layer.endObject(false);  
        }  
    }
    
    /**
     * import one point nad close
     */
    private void importPoint(VectorLayer layer, double[] point)
    {
        layer.addPoint(new Coordinate(point[0], point[1]), SpatiaLiteManager.EPSG_LONLAT);
        layer.endObject(false);          
    }
}
