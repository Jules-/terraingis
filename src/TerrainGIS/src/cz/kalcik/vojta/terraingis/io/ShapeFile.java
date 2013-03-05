/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;

import java.io.File;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpMultiPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolyLine;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolygon;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;

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
    public void load(String folder, String filename, String layerName, int srid)
    {
        cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile shapeFile =null;
        try
        {
            shapeFile = new cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile(
                    folder, filename);
            shapeFile.READ();
            
            ShpShape.Type type = shapeFile.getSHP_shapeType();
            
            LayerManager layerManager = LayerManager.getInstance();
            SpatiaLiteManager spatialiteManager = layerManager.getSpatialiteManager();
            
            // FIXME srid
            if(!spatialiteManager.createEmptyLayer(
                    layerName, SpatiaLiteManager.GEOMETRY_COLUMN_NAME,
                    getType(type), srid))
            {
                throw new Exception("Can not create table.");
            }
            
            layerManager.loadSpatialite();
            VectorLayer layer = layerManager.getLayerByName(layerName);
            
            if(type.isTypeOfPoint())
            {
                ArrayList<ShpPoint> points = shapeFile.getSHP_shape();
                for(ShpPoint point: points)
                {
                    importPoint(layer, point.getPoint(), srid);
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
                        importPoint(layer, values[i], srid);
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
                            object.getNumberOfPoints(), srid);
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
                            object.getNumberOfPoints(), srid);
                }  
            }
            
            spatialiteManager.reopen();
        }
        catch (Exception e)
        {
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
    private void importMultiObjects(VectorLayer layer, double[][][] multiObjects, int countParts,
            int countPoints, int srid)
    {
        for(int i=0; i < countParts; i++)
        {
            for(int j=0; j < countPoints; j++)
            {
                layer.addPoint(new Coordinate(multiObjects[i][j][0], multiObjects[i][j][1]), srid);
            }
            
            layer.endObject(false);  
        }  
    }
    
    /**
     * import one point nad close
     */
    private void importPoint(VectorLayer layer, double[] point, int srid)
    {
        layer.addPoint(new Coordinate(point[0], point[1]), srid);
        layer.endObject(false);          
    }
}
