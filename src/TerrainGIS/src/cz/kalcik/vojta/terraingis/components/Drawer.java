/**
 * 
 */
package cz.kalcik.vojta.terraingis.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

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
    public void draw(Canvas canvas, int width, int height, Context context)
    {
        Rect screen = mNavigator.getScreen();
        
        if(screen.width() != width || screen.height() != height)
        {
            screen.set(0, 0, width, height);
            mNavigator.setScreen(screen);
        }
        
        mLayerManager.redraw(canvas, mNavigator.getMRectangle(null), context);
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
    public void drawCanvasPathSurfacePx(Canvas canvas, PointF[] points, Paint paint)
    {
        Path path = new Path();
        int size = points.length;
        if(size < 2)
        {
            return;
        }
        
        path.moveTo(points[0].x, points[0].y);
        
        for(int i = 1; i < size; i++)
        {
            path.lineTo(points[i].x, points[i].y);
        }
        
        canvas.drawPath(path, paint);
    }
    
    /**
     * draw circles in points
     * @param canvas
     * @param points
     * @param paintDefault
     * @param paintSelected
     * @param radius
     * @param selected
     */
    public void drawPathNodesSurfacePx(Canvas canvas, PointF[] points, Paint paintDefault,
            Paint paintSelected, float radius, int selected)
    {
        int size = points.length;
        
        for(int i = 0; i < size; i++)
        {
            Paint paint = i == selected ? paintSelected : paintDefault;
            
            canvas.drawCircle(points[i].x, points[i].y, radius, paint);
        }        
    }

    /**
     * draw circles in points
     * @param canvas
     * @param points
     * @param paintDefault
     * @param radius
     */
    public void drawPathNodesSurfacePx(Canvas canvas, PointF[] points,
            Paint paintDefault, float radius)
    {
        drawPathNodesSurfacePx(canvas, points, paintDefault, null, radius, -1);
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
    public void drawIconM(Canvas canvas, Coordinate point, Drawable icon)
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
