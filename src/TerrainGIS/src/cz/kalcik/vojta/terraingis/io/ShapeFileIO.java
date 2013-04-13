/**
 * 
 */
package cz.kalcik.vojta.terraingis.io;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field;
import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field.FieldType;
import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_File;
import cz.kalcik.vojta.shapefilelib.files.shp.SHP_File;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPoint;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolyLine;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpPolygon;
import cz.kalcik.vojta.shapefilelib.files.shp.shapeTypes.ShpShape;
import cz.kalcik.vojta.shapefilelib.files.shx.SHX_File;
import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
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
    public void importShapefile(String folder, String filename, String layerName, String charset, 
            int srid, MapFragment mapFragment) throws Exception
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
            String layerName, int srid, MapFragment mapFragment) throws java.lang.Exception
    {
        ShapeFile shapeFile = new ShapeFile(folder, filename, charset);
        
        LayerManager layerManager = LayerManager.getInstance();
        SpatiaLiteIO spatialiteManager = layerManager.getSpatialiteIO();
        Layer layer = spatialiteManager.getLayer(layerName);
        VectorLayer vectorLayer = LayerManager.createVectorLayer(layer,
                spatialiteManager, mapFragment);
        
        exportShpAndShx(shapeFile, spatialiteManager, vectorLayer, srid);
        exportDbf(shapeFile, spatialiteManager, vectorLayer);
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
    
    /**
     * @param type
     * @return shape by type
     */
    private ShpShape createShape(ShpShape.Type type)
    {
        ShpShape shape = null;
        if(type.isTypeOfPoint())
        {
            shape = new ShpPoint(type);
        }
        if(type.isTypeOfPolyLine())
        {
            shape = new ShpPolygon(type);
        }
        if(type.isTypeOfPolygon())
        {
            shape = new ShpPolyLine(type);
        }
        
        return shape;
    }
    
    private void exportShpAndShx(ShapeFile shapeFile, SpatiaLiteIO spatialiteManager,
            VectorLayer layer, int srid) throws jsqlite.Exception, ParseException, IOException
    {
        ShpShape.Type type = VectorLayerType.spatialiteToShapefile(layer.getType()); 
        String layerName = layer.getData().name;
        String column = spatialiteManager.getColumnGeom(layerName);
        SpatialiteGeomIterator iterator = spatialiteManager.getAllObjects(layerName, column, srid);

        ArrayList<ShpShape> shapes = new ArrayList<ShpShape>();
        
        while (iterator.hasNext())
        {
            ShpShape shape = createShape(type);
            shape.loadFromJTS(iterator.next());
            shapes.add(shape);
        }
        
        SHP_File shpFile = shapeFile.getFile_SHP();
        shpFile.setShpShapes(shapes);
        
        Envelope envelope = layer.getEnvelope();
        shpFile.write(envelope, type);
        
        SHX_File shxFile = shapeFile.getFile_SHX();
        shxFile.write(envelope, type, shapes);
    }
    
    private void exportDbf(ShapeFile shapeFile, SpatiaLiteIO spatialiteManager,
            VectorLayer layer) throws Exception
    {
        AttributeHeader attributeHeader = layer.getAttributeHeader();
        String layerName = layer.getData().name;
        
        ArrayList<Column> columns = attributeHeader.getColumns();

        // fields
        int countColumns = columns.size();
        DBF_Field[] fields = new DBF_Field[countColumns];
        
        DBF_File dbfFile = shapeFile.getFile_DBF();
        String charset = dbfFile.getCharset().toLowerCase(Locale.getDefault());
        boolean multibyte = charset.startsWith("utf");
        
        for(int i=0; i < countColumns; i++)
        {
            Column column = columns.get(i);
            DBF_Field field = new DBF_Field(dbfFile, i);
            field.setName(column.name);
            field.setType(AttributeType.fromSpatialiteToShapefile(column.type).ID());
            
            int maxLength = spatialiteManager.maxLengthOfAttribute(layerName, column.name);
            if(column.type == AttributeType.INTEGER ||
                    column.type == AttributeType.REAL)
            {
                maxLength = maxLength > DBF_Field.NUMERIC_RECORD_LENGTH ? maxLength : 
                    DBF_Field.NUMERIC_RECORD_LENGTH;
                field.setLength(maxLength);
            }
            else if(column.type == AttributeType.TEXT)
            {
                if(multibyte)
                {
                    maxLength *= 2;
                }
                
                maxLength = maxLength > DBF_Field.TEXT_RECORD_LENGTH ? maxLength : 
                    DBF_Field.TEXT_RECORD_LENGTH;
                field.setLength(maxLength);               
            }
            
            fields[i] = field;
        }        
        
        dbfFile.setFields(fields);
        
        // records
        SpatialiteAttributesIterator iterator = spatialiteManager.getAttributes(
                layer.getData().name, layer.getAttributeHeader());
        String[][] records = new String[countColumns][];
        int index = 0;
        while (iterator.hasNext())
        {
            records[index] = iterator.next();
            
            index++;
        }
        
        dbfFile.setRecords(records);
        dbfFile.write();
    }
    // classes ==========================================================================
}
