/**
 * 
 */
package cz.kalcik.vojta.terraingis.layer;

import jsqlite.Exception;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.Drawer;
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
    protected Paint mSelectedVertexSelectedObjectPaint;
    protected Paint mVertexsSelectedObjectPaint;
    protected Paint mStrokePolygonPaint;
    
    public PolyPointsLayer(VectorLayerType type, String name, int srid,
            SpatiaLiteIO spatialite, MapFragment mapFragment) throws Exception
    {
        super(type, name, srid, spatialite, mapFragment);
        
        mVertexsSelectedObjectPaint = VectorLayerPaints.getVertex(PaintType.DEFAULT);
        mSelectedVertexSelectedObjectPaint = VectorLayerPaints.getVertex(PaintType.SELECTED);
        mStrokePolygonPaint = VectorLayerPaints.getLine(PaintType.SELECTED);
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
        // saved objects
        SpatialiteGeomIterator iter = getObjects(rect);
        while(iter.hasNext())
        {
            Geometry geometry = iter.next();

            if(!isSelectedObject(iter) && !isRecordedObject(iter))
            {
                PointF[] points = mNavigator.mToSurfacePx(geometry.getCoordinates());
                Drawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        mPaint);
            }
        }
        
        // recording of object
        if(mVectorLayerData.recordedPoints.size() > 0)
        {
            Coordinate[] metersCoordinates = mVectorLayerData.recordedPoints.toArray(
                    new Coordinate[mVectorLayerData.recordedPoints.size()]);
            PointF[] points = mNavigator.mToSurfacePx(metersCoordinates);
            
            Drawer.drawCanvasPathSurfacePx(canvas,
                    points, mNotSavedPaint);
            
            Drawer.drawVertexsSurfacePx(canvas, points, mVertexsSelectedObjectPaint,
                    mSelectedVertexSelectedObjectPaint,
                    VectorLayerPaints.getVertexRadius(), mVectorLayerData.recordedVertexIndex);
        }
        
        // selected object
        if(!mVectorLayerData.selectedObjectPoints.isEmpty())
        {
            PointF[] points = mNavigator.mToSurfacePx(
                    mVectorLayerData.selectedObjectPoints.toArray(
                            new Coordinate[mVectorLayerData.selectedObjectPoints.size()]));
            
            Drawer.drawCanvasPathSurfacePx(canvas,
                    points, 
                    mSelectedPaint);
            
            // stroke of polygon
            if(mType == VectorLayerType.POLYGON)
            {
                Drawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        mStrokePolygonPaint);                
            }
            
            Drawer.drawVertexsSurfacePx(canvas, points, mVertexsSelectedObjectPaint,
                    mSelectedVertexSelectedObjectPaint,
                    VectorLayerPaints.getVertexRadius(), mVectorLayerData.selectedVertexIndex);
        }
    }
}
