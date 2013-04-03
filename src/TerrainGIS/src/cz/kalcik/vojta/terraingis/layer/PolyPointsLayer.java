/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;

/**
 * @author jules
 *
 */
public abstract class PolyPointsLayer extends VectorLayer
{
    protected Paint mSelectedNodeSelectedObjectPaint;
    protected Paint mNodesSelectedObjectPaint;
    
    public PolyPointsLayer(VectorLayerType type, String name, int srid,
            SpatiaLiteIO spatialite)
    {
        super(type, name, srid, spatialite);
        
        mNodesSelectedObjectPaint = VectorLayerPaints.getPoint(PaintType.SELECTED);
        mSelectedNodeSelectedObjectPaint = VectorLayerPaints.getPoint(PaintType.SELECTED_SELECTED_NODE);
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
            PointF[] points = mNavigator.mToSurfacePx(iter.next().getCoordinates());
            
            mDrawer.drawCanvasPathSurfacePx(canvas,
                    points, 
                    selectObjectPaint(iter));
            
            // if selected object draw nodes
            if(isSelectedObject(iter))
            {
                mDrawer.drawPathNodesSurfacePx(canvas, points, mNodesSelectedObjectPaint,
                        mSelectedNodeSelectedObjectPaint,
                        VectorLayerPaints.getPointRadius(PaintType.DEFAULT), mSelectedNodeIndex);
            }
        }
        
        if(childData.mRecordedPoints.size() > 0)
        {
            Coordinate[] metersCoordinates = childData.mRecordedPoints.toArray(
                    new Coordinate[childData.mRecordedPoints.size()]);
            
            mDrawer.drawCanvasPathSurfacePx(canvas,
                    mNavigator.mToSurfacePx(metersCoordinates),
                    mNotSavedPaint);
        }
    }
}
