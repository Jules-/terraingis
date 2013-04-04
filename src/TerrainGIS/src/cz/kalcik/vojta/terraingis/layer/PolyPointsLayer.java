/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.fragments.MapFragment;
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
    protected Paint mStrokePolygonPaint;
    
    public PolyPointsLayer(VectorLayerType type, String name, int srid,
            SpatiaLiteIO spatialite, MapFragment mapFragment)
    {
        super(type, name, srid, spatialite, mapFragment);
        
        mNodesSelectedObjectPaint = VectorLayerPaints.getPoint(PaintType.SELECTED);
        mSelectedNodeSelectedObjectPaint = VectorLayerPaints.getPoint(PaintType.SELECTED_SELECTED_NODE);
        mStrokePolygonPaint = VectorLayerPaints.getLine(PaintType.SELECTED);
    }

    /**
     * draw objects to canvas
     */
    @Override
    public void draw(Canvas canvas, Envelope rect)
    {
        // saved objects
        SpatialiteGeomIterator iter = getObjects(rect);
        while(iter.hasNext())
        {
            Geometry geometry = iter.next();

            if(!isSelectedObject(iter))
            {
                PointF[] points = mNavigator.mToSurfacePx(geometry.getCoordinates());
                mDrawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        mPaint);
            }
        }
        
        // recording of object
        if(childData.recordedPoints.size() > 0)
        {
            Coordinate[] metersCoordinates = childData.recordedPoints.toArray(
                    new Coordinate[childData.recordedPoints.size()]);
            
            mDrawer.drawCanvasPathSurfacePx(canvas,
                    mNavigator.mToSurfacePx(metersCoordinates),
                    mNotSavedPaint);
        }
        
        // selected object
        if(!childData.selectedObjectPoints.isEmpty())
        {
            PointF[] points = mNavigator.mToSurfacePx(
                    childData.selectedObjectPoints.toArray(
                            new Coordinate[childData.selectedObjectPoints.size()]));
            
            mDrawer.drawCanvasPathSurfacePx(canvas,
                    points, 
                    mSelectedPaint);
            
            // stroke of polygon
            if(mType == VectorLayerType.POLYGON)
            {
                mDrawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        mStrokePolygonPaint);                
            }
            
            mDrawer.drawPathNodesSurfacePx(canvas, points, mNodesSelectedObjectPaint,
                    mSelectedNodeSelectedObjectPaint,
                    VectorLayerPaints.getPointRadius(PaintType.DEFAULT), childData.selectedNodeIndex);
        }
    }
}
