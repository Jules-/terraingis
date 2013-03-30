package cz.kalcik.vojta.terraingis.layer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import android.graphics.Canvas;

public class PolygonsLayer extends VectorLayer
{
    //constants ===============================================================
    
    // public methods =========================================================
    public PolygonsLayer(String name, int srid,
                         SpatiaLiteIO spatialite)
    {
        super(VectorLayerType.POLYGON, name, srid, spatialite);
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
            mDrawer.drawCanvasPathM(canvas, iter.next().getCoordinates(), 
                    selectObjectPaint(iter));
        }
        
        if(childData.mRecordedPoints.size() > 0)
        {
            mDrawer.drawCanvasPathM(canvas,
                    childData.mRecordedPoints.toArray(
                            new Coordinate[childData.mRecordedPoints.size()]),
                    mNotSavedPaint);
        }
    }
}
