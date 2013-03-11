package cz.kalcik.vojta.terraingis.layer;

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
        if(spatialiteType.equals("TEXT"))
        {
            return TEXT;
        }
        else if(spatialiteType.equals("INTEGER"))
        {
            return INTEGER;
        }
        else if(spatialiteType.equals("REAL"))
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