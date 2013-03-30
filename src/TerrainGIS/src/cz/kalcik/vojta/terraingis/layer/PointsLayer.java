package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.components.ConvertUnits;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
    // constants ==============================================================
    
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteIO spatialite)
    {
        super(VectorLayerType.POINT, name, srid, spatialite);
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
            
            if(isSelectedObject(iter))
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.SELECTED);
            }
            else
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.DEFAULT);
            }
            
            mDrawer.drawCircleM(canvas, selectObjectPaint(iter),
                    iter.next().getCoordinate(), radius);
        }
    }
}
