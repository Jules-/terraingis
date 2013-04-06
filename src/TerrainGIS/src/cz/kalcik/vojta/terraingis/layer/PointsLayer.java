package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;
import android.graphics.Canvas;

public class PointsLayer extends VectorLayer
{
    // constants ==============================================================
    
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteIO spatialite, MapFragment mapFragment)
    {
        super(VectorLayerType.POINT, name, srid, spatialite, mapFragment);
    }
	
	/**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Envelope rect)
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
                coordinate = vectorLayerData.selectedObjectPoints.get(0);
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
}
