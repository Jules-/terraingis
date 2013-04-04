package cz.kalcik.vojta.terraingis.layer;

import java.util.Locale;

import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field.FieldType;

/**
 * Data type of attribute
 * @author jules
 *
 */
public enum AttributeType
{
    TEXT, INTEGER, REAL;
    
    public static AttributeType getTypeSpatialite(String spatialiteType)
    {
        spatialiteType = spatialiteType.toLowerCase(Locale.UK);
        if(spatialiteType.equals("text"))
        {
            return TEXT;
        }
        else if(spatialiteType.equals("integer"))
        {
            return INTEGER;
        }
        else if(spatialiteType.equals("real"))
        {
            return REAL;
        }
        else
        {
            return null;
        }
    }
    
    public static AttributeType getTypeShapefile(FieldType type)
    {
        if(type == FieldType.C ||
           type == FieldType.D ||
           type == FieldType.M)
        {
            return TEXT;
        }
        else if(type == FieldType.N || type == FieldType.L)
        {
            return INTEGER;
        }
        else
        {
            return null;
        }
    }
}