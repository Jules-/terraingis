/**
 * TerrainGIS 
 * Android program for mapping
 * 
 * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kalcik.vojta.terraingis.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.MainActivity.AddPointMode;
import cz.kalcik.vojta.terraingis.layer.LayerManager;

/**
 * @author jules
 *
 */
public class Drawer
{
    // constatns ==========================================================================
    private static float SQRT_2 = (float) Math.sqrt(2);
    
    // attributes =========================================================================
    private static Navigator mNavigator = Navigator.getInstance();
    private static LayerManager mLayerManager = LayerManager.getInstance();
    
    // public methods =====================================================================
    
    /**
     * draw layers to canvas by position
     * @param canvas
     * @param width
     * @param height
     */
    public static void draw(Canvas canvas, MainActivity mainActivity)
    {       
        boolean drawVertexs = mainActivity.getAddPointMode() == AddPointMode.TOPOLOGY_POINT;
        mLayerManager.redraw(canvas, mNavigator.getMRectangle(null), mainActivity,
                drawVertexs);
    }   
    
    /**
     * draw drawable to canvas
     * @param canvas
     * @param drawable
     * @param bound
     */
    public static void drawCanvasDraweblePx(Canvas canvas, Drawable drawable, Envelope bound)
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
    public static void drawCanvasPathSurfacePx(Canvas canvas, PointF[] points, Paint paint)
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
    public static void drawVertexsSurfacePx(Canvas canvas, PointF[] points, Paint paintDefault,
            Paint paintSelected, float radius, int selected)
    {
        int size = points.length;
        
        for(int i = 0; i < size; i++)
        {
            Paint paint = i == selected ? paintSelected : paintDefault;
            
            drawCross45Degree(canvas, paint, points[i], radius);
            if(i == 0)
            {
                drawCross(canvas, paint, points[i], radius);
            }
        }        
    }

    /**
     * draw circles in points
     * @param canvas
     * @param points
     * @param paintDefault
     * @param radius
     */
    public static void drawVertexsSurfacePx(Canvas canvas, PointF[] points,
            Paint paintDefault, float radius)
    {
        drawVertexsSurfacePx(canvas, points, paintDefault, null, radius, -1);
    }
    
    /**
     * draw circle
     * @param canvas
     * @param paint
     * @param center
     * @param radius
     */
    public static void drawCircleM(Canvas canvas, Paint paint, Coordinate center, float radius)
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
    public static void drawIconM(Canvas canvas, Coordinate point, Drawable icon)
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
    
    // private methods =================================================================
    
    /**
     * draw cross rotated 45 degrees
     * @param canvas
     * @param paint
     * @param center
     * @param radius
     */
    private static void drawCross45Degree(Canvas canvas, Paint paint, PointF centerSurfacePx, float radius)
    {
        float length = radius / SQRT_2;
        canvas.drawLine(centerSurfacePx.x-length, centerSurfacePx.y-length,
                centerSurfacePx.x+length, centerSurfacePx.y+length, paint);
        canvas.drawLine(centerSurfacePx.x-length, centerSurfacePx.y+length,
                centerSurfacePx.x+length, centerSurfacePx.y-length, paint);
    }

    /**
     * draw cross
     * @param canvas
     * @param paint
     * @param centerSurfacePx
     * @param radius
     */
    private static void drawCross(Canvas canvas, Paint paint, PointF centerSurfacePx, float radius)
    {
        canvas.drawLine(centerSurfacePx.x-radius, centerSurfacePx.y,
                centerSurfacePx.x+radius, centerSurfacePx.y, paint);
        canvas.drawLine(centerSurfacePx.x, centerSurfacePx.y-radius,
                centerSurfacePx.x, centerSurfacePx.y+radius, paint);
    }
}
