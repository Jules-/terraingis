/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;


import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field;
import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field.FieldType;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeType;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.VectorLayerType;

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
     * import shapefile layer
     * @param context
     * @param file
     * @throws Exception 
     */
    public void importShapefile(String folder, String filename, String layerName, int srid,
            String charset, MapFragment mapFragment) throws Exception
    {
        ShapeFile shapeFile = new ShapeFile(folder, filename, charset);
        shapeFile.READ();
        
        ShpShape.Type type = shapeFile.getSHP_shapeType();
        
        LayerManager layerManager = LayerManager.getInstance();
        SpatiaLiteIO spatialiteManager = layerManager.getSpatialiteIO();
        
        spatialiteManager.createEmptyLayer(layerName, getTypeString(type),
                createAttributeHeader(shapeFile).createSQLColumns(), srid);
        
        layerManager.loadSpatialite(mapFragment);
        VectorLayer layer = layerManager.getLayerByName(layerName);
        layer.importObjects(new ShapeFileIterator(shapeFile));
    }
    
    
    public void exportShapefile(String folder, String filename, String charset,
            String layerName) throws Exception
    {
        ShapeFile shapeFile = new ShapeFile(folder, filename, charset);
        
        LayerManager layerManager = LayerManager.getInstance();
        SpatiaLiteIO spatialiteManager = layerManager.getSpatialiteIO();
        
        Layer layer = spatialiteManager.getLayer(layerName);
        ShpShape.Type type = VectorLayerType.spatialiteToShapefile(
                VectorLayerType.valueOf(layer.type));
    }
    // public static methods ==============================================================
    
    /**
     * @param type
     * @return string spatialite type of layer
     */
    public static String getTypeString(ShpShape.Type type)
    {        
        return VectorLayerType.shapefileToSpatialite(type).getSpatialiteType();
    }
    
    // private methods =====================================================================    
    /**
     * create attribute table from shapeFile
     * @param shapeFile
     * @return
     */
    private AttributeHeader createAttributeHeader(ShapeFile shapeFile)
    {
        AttributeHeader result = new AttributeHeader();
        int count = shapeFile.getDBF_fieldCount();
        for(int i=0; i < count; i++)
        {
            DBF_Field field = shapeFile.getDBF_field(i);
            String name = field.getName();
            AttributeType type = AttributeType.getTypeShapefile(
                    FieldType.byID(field.getType()));
            if(type != null)
            {
                result.addColumn(name, type, false);
            }
        }
        
        return result;
    }
    
    // classes ==========================================================================
}
