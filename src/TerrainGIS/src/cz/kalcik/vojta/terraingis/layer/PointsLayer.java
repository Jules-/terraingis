package cz.kalcik.vojta.terraingis.layer;

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;
import android.graphics.Canvas;

public class PointsLayer extends VectorLayer
{
    // constants ==============================================================
    public static final int MIN_POINTS = 1;
    
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteIO spatialite, MapFragment mapFragment)
                               throws Exception
    {
        super(VectorLayerType.POINT, name, srid, spatialite, mapFragment);
    }
	
	/**
     * draw objects to canvas
	 * @throws ParseException 
	 * @throws Exception 
     */
    @Override
    public void draw(Canvas canvas, Envelope rect)
            throws Exception, ParseException
    {
        SpatialiteGeomIterator iter = getObjects(rect);
        while(iter.hasNext())
        {
            float radius;
            Coordinate coordinate;
            
            Geometry geometry = iter.next();
            if(isSelectedObject(iter))
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.SELECTED);
                coordinate = mVectorLayerData.selectedObjectPoints.get(0);
            }
            else
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.DEFAULT);
                coordinate = geometry.getCoordinate();
            }
            
            mDrawer.drawCircleM(canvas, selectObjectPaint(iter),
                    coordinate, radius);
        }
    }
    
    @Override
    protected int getMinCountPoints()
    {
        return MIN_POINTS;
    }
}
