/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import cz.kalcik.vojta.terraingis.layer.LayerManager;

/**
 * @author jules
 *
 */
public class Drawer
{
    // singleton code =====================================================================
    
    private static Drawer instance = new Drawer();
    
    private Drawer() { }
    
    public static Drawer getInstance()
    {
        return instance;
    }

    // attributes =========================================================================
    private Navigator mNavigator = Navigator.getInstance();
    private LayerManager mLayerManager = LayerManager.getInstance();
    
    // public methods =====================================================================
    
    /**
     * draw layers to canvas by position
     * @param canvas
     * @param width
     * @param height
     */
    public void draw(Canvas canvas, int width, int height)
    {
        Rect screen = mNavigator.getScreen();
        
        if(screen.width() != width || screen.height() != height)
        {
            screen.set(0, 0, width, height);
            mNavigator.setScreen(screen);
        }
        
        mLayerManager.redraw(canvas, mNavigator.getMRectangle(null));
    }   
    
    /**
     * draw drawable to canvas
     * @param canvas
     * @param drawable
     * @param bound
     */
    public void drawCanvasDraweblePx(Canvas canvas, Drawable drawable, Envelope bound)
    {
        drawable.setBounds(mNavigator.pxToSurfacePx(bound, null));
        drawable.draw(canvas);
    }
    
    /**
     * draw points to canvas
     * @param canvas
     * @param points
     * @param paint
     */
    public void drawCanvasPathM(Canvas canvas, Geometry object, Paint paint)
    {
        Path path = new Path();
        int size = object.getNumPoints();;
        if(size < 2)
        {
            return;
        }
        
        Coordinate[] points = object.getCoordinates();
        Coordinate pointPx = mNavigator.mToPx(points[0], null);
        PointF surfacePoint = mNavigator.pxToSurfacePx(pointPx, null);
        path.moveTo(surfacePoint.x, surfacePoint.y);
        
        for(int i = 1; i < size; i++)
        {
            mNavigator.mToPx(points[i], pointPx);
            mNavigator.pxToSurfacePx(pointPx, surfacePoint);
            path.lineTo(surfacePoint.x, surfacePoint.y);
        }
        
        canvas.drawPath(path, paint);
    }
    
    /**
     * draw lines between points
     * @param canvas
     * @param points
     * @param paint
     */
    public void drawLinesM(Canvas canvas, Geometry object, Paint paint)
    {
        int size = object.getNumPoints();
        if(size < 2)
        {
            return;
        }      
        
        Coordinate[] points = object.getCoordinates();
        Coordinate pointPx = mNavigator.mToPx(points[0], null);
        PointF previousSurfacePoint = mNavigator.pxToSurfacePx(pointPx, null);
        PointF currentSurfacePoint = new PointF();
        
        for(int i = 1; i < size; i++)
        {
            mNavigator.mToPx(points[i], pointPx);
            mNavigator.pxToSurfacePx(pointPx, currentSurfacePoint);
            
            canvas.drawLine(previousSurfacePoint.x, previousSurfacePoint.y,
                            currentSurfacePoint.x, currentSurfacePoint.y, paint);
            previousSurfacePoint.set(currentSurfacePoint);
        }
    }
    
    /**
     * draw circle
     * @param canvas
     * @param paint
     * @param center
     * @param radius
     */
    public void drawCircleM(Canvas canvas, Paint paint, Coordinate center, float radius)
    {
        PointF centerSurfacePx = mNavigator.mToSurfacePx(center, null);
        canvas.drawCircle(centerSurfacePx.x, centerSurfacePx.y, radius, paint);
    }
    
    /**
     * draw icon to position
     * @param canvas
     * @param point
     * @param icon
     */
    public synchronized void drawIconM(Canvas canvas, Coordinate point, Drawable icon)
    {
        Coordinate pointPx = mNavigator.mToPx(point, null);
      
        if(mNavigator.getPxScreen().contains(pointPx.x, pointPx.y))
        {
            PointF pointSurface = mNavigator.pxToSurfacePx(pointPx, null);
            float iconWidthHalf = (float)icon.getIntrinsicWidth() / 2;
            float iconHeightHalf = (float)icon.getIntrinsicHeight() / 2;
            Rect bounds = new Rect((int)Math.round(pointSurface.x-iconWidthHalf),
                                   (int)Math.round(pointSurface.y-iconHeightHalf),
                                   (int)Math.round(pointSurface.x+iconWidthHalf),
                                   (int)Math.round(pointSurface.y+iconHeightHalf));
            icon.setBounds(bounds);
            icon.draw(canvas);
        }
    }
}
