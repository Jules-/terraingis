package cz.kalcik.vojta.terraingis.components;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import cz.kalcik.vojta.terraingis.layer.LayerManager;

/**
 * class for zoom and move of map
 * @author jules
 *
 */
public class Navigator
{
    // constants ==========================================================================
    
    private final static double O_EARTH_ZOOM_LEVEL = 40075016.68557849;
    private final static double DEFAULT_ZOOM = 10;

    // singleton code =====================================================================
    
    private static Navigator instance = new Navigator();
    
    private Navigator() { }
    
    public static Navigator getInstance()
    {
        return instance;
    }
    
    // attributes =========================================================================
    
    private double zoom = DEFAULT_ZOOM;
    private Coordinate positionM = new Coordinate(0,0);
    private LayerManager layerManager = LayerManager.getInstance();
    private Rect screen = new Rect();
    private Envelope pxScreen = new Envelope(); // area showed in screen in pixels
    
    // getter setter ======================================================================
    
    /**
     * set zoom in m/px
     * @param zoom
     */
    public void setZoom(double zoom)
    {
        this.zoom = zoom;
        updateDuplicateAttributes();
    }    

    /**
     * return zoom    
     * @return
     */
    public double getZoom()
    {
        return zoom;
    }

    /**
     * set position by longitude and latitude
     * @param lon
     * @param lat
     */
    public void setLonLatPosition(double lon, double lat)
    {
        setLonLatPosition(new Coordinate(lon, lat));
    }
    
    /**
     * set position by longitude and latitude
     * @param lonLatPosition
     */
    public void setLonLatPosition(Coordinate lonLatPosition)
    {
        positionM = layerManager.lonLatToM(lonLatPosition);
        updateDuplicateAttributes();
    }
    
    /**
     * return rectangle in px showed on screen
     * @param output
     * @return
     */
    public Envelope getPxRectangle(Envelope output)
    {
        Coordinate tempPoint = mToPx(positionM, null);
        
        if(output == null)
        {
            output = new Envelope();
        }
        
        double width_half = screen.width() / 2.0;
        double height_half = screen.height() / 2.0;
        
        output.init(tempPoint.x - width_half,
                    tempPoint.x + width_half,
                    tempPoint.y - height_half,
                    tempPoint.y + height_half);
        
        return output;
    }
    
    public Coordinate getPositionM()
    {
        return positionM;
    }
    
    public void setPositionM(Coordinate position)
    {
        positionM = position;
        updateDuplicateAttributes();
    }
    
    // public methods =====================================================================
    
    /**
     * move map by float px distances
     * @param x
     * @param y
     */
    public void offsetSurfacePx(float x, float y)
    {
        positionM.x += pxToM(x);
        positionM.y += pxToM(-y);
        
        updateDuplicateAttributes();
    }
    
    /**
     * draw layers to canvas by position
     * @param canvas
     * @param width
     * @param height
     */
    public void draw(Canvas canvas, int width, int height)
    {
        if(screen.width() != width || screen.height() != height)
        {
            screen.set(0, 0, width, height);
            updateDuplicateAttributes();
        }
        
        layerManager.redraw(canvas, getMRectangle(null));
    }   
    
    /**
     * draw drawable to canvas
     * @param canvas
     * @param drawable
     * @param bound
     */
    public void drawCanvasDraweblePx(Canvas canvas, Drawable drawable, Envelope bound)
    {
        drawable.setBounds(pxToSurfacePx(bound, null));
        drawable.draw(canvas);
    }
    
    /**
     * draw points to canvas
     * @param canvas
     * @param points
     * @param paint
     */
    public void drawCanvasPathM(Canvas canvas, ArrayList<Coordinate> points, Paint paint)
    {
        Path path = new Path();
        int size = points.size();
        if(size < 2)
        {
            return;
        }
        
        Coordinate pointPx = mToPx(points.get(0), null);
        PointF surfacePoint = pxToSurfacePx(pointPx, null);
        path.moveTo(surfacePoint.x, surfacePoint.y);
        
        for(int i = 1; i < size; i++)
        {
            mToPx(points.get(i), pointPx);
            pxToSurfacePx(pointPx, surfacePoint);
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
    public void drawLinesM(Canvas canvas, ArrayList<Coordinate> points, Paint paint)
    {
        int size = points.size();
        if(size < 2)
        {
            return;
        }      
        
        Coordinate pointPx = mToPx(points.get(0), null);
        PointF previousSurfacePoint = pxToSurfacePx(pointPx, null);
        PointF currentSurfacePoint = new PointF();
        
        for(int i = 1; i < size; i++)
        {
            mToPx(points.get(i), pointPx);
            pxToSurfacePx(pointPx, currentSurfacePoint);
            
            canvas.drawLine(previousSurfacePoint.x, previousSurfacePoint.y,
                            currentSurfacePoint.x, currentSurfacePoint.y, paint);
            previousSurfacePoint.set(currentSurfacePoint);
        }
    }
    
    /**
     * draw icon to position
     * @param canvas
     * @param point
     * @param icon
     */
    public synchronized void drawIconM(Canvas canvas, Coordinate point, Drawable icon)
    {
        Coordinate pointPx = mToPx(point, null);

        if(pxScreen.contains(pointPx.x, pointPx.y))
        {
            PointF pointSurface = pxToSurfacePx(pointPx, null);
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
    
    /**
     * change zoom by scale and position by pivot
     * @param scale
     * @param pivot
     */
    public void zoomByScale(float scale, PointF pivot)
    {
        Coordinate tempPivot = surfacePxToPx(pivot, null);
        
        Coordinate tempPoint = mToPx(positionM, null);
        
        double pivotCenterDistanceX = (tempPoint.x - tempPivot.x);
        double pivotCenterDistanceY = (tempPoint.y - tempPivot.y);
        
        pxToM(tempPivot, tempPivot);
        
        setZoom(zoom/scale);
        
        mToPx(tempPivot, tempPivot);
        
        setPositionM(pxToM(tempPivot.x + pivotCenterDistanceX),
                     pxToM(tempPivot.y + pivotCenterDistanceY));
    }
    
    /**
     * set position in meters
     * @param x
     * @param y
     */
    public void setPositionM(double x, double y)
    {
        positionM.x = x;
        positionM.y = y;
        
        updateDuplicateAttributes();
    }
    
    // static public methods ==============================================================
    
    static public int mpxToZoomLevel(double zoom)
    {
        double doubleZoomLevel = -(Math.log(zoom/O_EARTH_ZOOM_LEVEL)+8*Math.log(2))/Math.log(2);
        return (int)Math.ceil(doubleZoomLevel);
    }

    static public double zoomLevelToMpx(int zoomLevel)
    {
        return O_EARTH_ZOOM_LEVEL/Math.pow(2, (zoomLevel+8));
    }

    // private methods ======================================================================
    /**
     * convert m coordinate to px coordinate
     * @param inputValue
     * @return
     */
    private double mToPx(double inputValue)
    {
        return inputValue/zoom;
    }
    
    /**
     * convert m coordinates to px coordinates
     * @param input
     * @param output
     * @return
     */
    private Coordinate mToPx(Coordinate input, Coordinate output)
    {
        if(output == null)
        {
            output = new Coordinate();
        }
        
        output.x = mToPx(input.x);
        output.y = mToPx(input.y);
        
        return output;
    }
    
    /**
     * convert px coordinate to m coordinate
     * @param inputValue
     * @return
     */
    private double pxToM(double inputValue)
    {
        return inputValue * zoom;
    }
    
    /**
     * convert px coordinates to m coordinates
     * @param input
     * @param output
     * @return
     */
    private Coordinate pxToM(Coordinate input, Coordinate output)
    {
        if(output == null)
        {
            output = new Coordinate();
        }
        
        output.x = pxToM(input.x);
        output.y = pxToM(input.y);
        
        return output;
    }

    /**
     * convert px coordinates to longitude latitude coordinates
     * @param input
     * @param output
     * @return
     */
    private Coordinate pxToLonLat(Coordinate input)
    {
        return layerManager.mToLonLat(pxToM(input, null));
    }

    /**
     * convert px coordinates to coordinates in surface
     * @param input
     * @param output
     * @param pxRectangle
     * @return
     */
    private PointF pxToSurfacePx(Coordinate input, PointF output)
    {
        if(output == null)
        {
            output = new PointF();
        }
        
        output.set((float)(input.x - pxScreen.getMinX()), (float)(pxScreen.getMaxY() - input.y));
        
        return output;
    }
    
    private Rect pxToSurfacePx(Envelope input, Rect output)
    {        
        Coordinate pointPx = new Coordinate(input.getMinX(), input.getMinY());
        PointF point = pxToSurfacePx(pointPx, null);
        int left = (int)Math.round(point.x);
        int bottom = (int)Math.round(point.y);
        pointPx.x = input.getMaxX();
        pointPx.y = input.getMaxY();
        pxToSurfacePx(pointPx, point);
        
        if(output == null)
        {
            output = new Rect();
        }
        
        output.set(left, (int)Math.round(point.y),
                   (int)Math.round(point.x), bottom);
        
        return output;
    }
    
    /**
     * convert longitude latitude coordinates to px coordinates
     * @param input
     * @param output
     * @return
     */
    private Coordinate lonLatToPx(Coordinate input, Coordinate output)
    {
        Coordinate meters = layerManager.lonLatToM(input);
        
        return mToPx(meters, output);
    }
    
    /**
     * convert float point from surface to point in pixels
     * @param input
     * @param output
     * @return
     */
    private Coordinate surfacePxToPx(PointF input, Coordinate output)
    {
        if(output == null)
        {
            output = new Coordinate();
        }
        
        output.x = pxScreen.getMinX() + input.x;
        output.y = pxScreen.getMinY() + (screen.height() - input.y);
        
        return output;
    }
    
    /**
     * return rectangle in meters which is showed on screen
     * @param output
     * @return
     */
    private Envelope getMRectangle(Envelope output)
    {        
        if(output == null)
        {
            output = new Envelope();
        }
        
        double width_half = pxToM(screen.width()) / 2.0;
        double height_half = pxToM(screen.height()) / 2.0;
        
        output.init(positionM.x - width_half,
                    positionM.x + width_half,
                    positionM.y - height_half,
                    positionM.y + height_half);
        
        return output;
    }
    
    /**
     * update attributes which must be computed
     */
    private void updateDuplicateAttributes()
    {
        getPxRectangle(pxScreen);
    }
}