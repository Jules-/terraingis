package cz.kalcik.vojta.terraingis.view;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D;
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
    private Point2D.Double positionM = new Point2D.Double(0,0);
    private Point2D.Double tempPoint = new Point2D.Double(0,0);
    private LayerManager layerManager = LayerManager.getInstance();
    Rect screen = new Rect();
    Rectangle2D.Double pxScreen = new Rectangle2D.Double(); // area showed in screen in pixels
    
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
        setLonLatPosition(new Point2D.Double(lon, lat));
    }
    
    /**
     * set position by longitude and latitude
     * @param lonLatPosition
     */
    public void setLonLatPosition(Point2D.Double lonLatPosition)
    {
        layerManager.lonLatToM(lonLatPosition, positionM);
        updateDuplicateAttributes();
    }
    
    /**
     * return rectangle in px showed on screen
     * @param output
     * @return
     */
    public Rectangle2D.Double getPxRectangle(Rectangle2D.Double output)
    {
        mToPx(positionM, tempPoint);
        int width = screen.width();
        int height = screen.height();
        
        if(output == null)
        {
            output = new Rectangle2D.Double();
        }
        
        output.setRect(tempPoint.x - width / 2.0,
                       tempPoint.y - height / 2.0,
                       width,
                       height);
        
        return output;
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
    public void drawCanvasDraweblePx(Canvas canvas, Drawable drawable, Rectangle2D.Double bound)
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
    public void drawCanvasPathM(Canvas canvas, ArrayList<Point2D.Double> points, Paint paint)
    {
        Path path = new Path();
        int size = points.size();
        if(size < 2)
        {
            return;
        }
        
        Point2D.Double pointPx = mToPx(points.get(0), null);
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
     * change zoom by scale and position by pivot
     * @param scale
     * @param pivot
     */
    public void zoomByScale(float scale, PointF pivot)
    {
        Point2D.Double tempPivot = surfacePxToPx(pivot, null);
        
        mToPx(positionM, tempPoint);
        
        double pivotCenterDistanceX = (tempPoint.x - tempPivot.x);
        double pivotCenterDistanceY = (tempPoint.y - tempPivot.y);
        
        pxToM(tempPivot, tempPivot);
        
        setZoom(zoom/scale);
        
        mToPx(tempPivot, tempPivot);
        
        setPositionM(pxToM(tempPivot.x + pivotCenterDistanceX),
                     pxToM(tempPivot.y + pivotCenterDistanceY));
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
    private Point2D.Double mToPx(Point2D.Double input, Point2D.Double output)
    {
        if(output == null)
        {
            output = new Point2D.Double();
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
    private Point2D.Double pxToM(Point2D.Double input, Point2D.Double output)
    {
        if(output == null)
        {
            output = new Point2D.Double();
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
    private Point2D.Double pxToLonLat(Point2D.Double input, Point2D.Double output)
    {
        Point2D.Double meters = pxToM(input, null);
        
        return layerManager.mToLonLat(meters, output);
    }

    /**
     * convert px coordinates to coordinates in surface
     * @param input
     * @param output
     * @param pxRectangle
     * @return
     */
    private PointF pxToSurfacePx(Point2D.Double input, PointF output)
    {
        if(output == null)
        {
            output = new PointF();
        }
        
        output.set((float)(input.x - pxScreen.x), (float)(pxScreen.y + pxScreen.height - input.y));
        
        return output;
    }
    
    private Rect pxToSurfacePx(Rectangle2D.Double input, Rect output)
    {        
        Point2D.Double pointPx = new Point2D.Double(input.x, input.y);
        PointF point = pxToSurfacePx(pointPx, null);
        int left = (int)Math.round(point.x);
        int bottom = (int)Math.round(point.y);
        pointPx.x = input.x + input.width;
        pointPx.y = input.y + input.height;
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
    private Point2D.Double lonLatToPx(Point2D.Double input, Point2D.Double output)
    {
        Point2D.Double meters = layerManager.lonLatToM(input, null);
        
        return mToPx(meters, output);
    }
    
    /**
     * convert float point from surface to point in pixels
     * @param input
     * @param output
     * @return
     */
    private Point2D.Double surfacePxToPx(PointF input, Point2D.Double output)
    {
        if(output == null)
        {
            output = new Point2D.Double();
        }
        
        output.x = pxScreen.x + input.x;
        output.y = pxScreen.y + (screen.height() - input.y);
        
        return output;
    }
    
    /**
     * return rectangle in meters which is showed on screen
     * @param output
     * @return
     */
    private Rectangle2D.Double getMRectangle(Rectangle2D.Double output)
    {
        double width = pxToM(screen.width());
        double height = pxToM(screen.height());
        
        if(output == null)
        {
            output = new Rectangle2D.Double();
        }
        
        output.setRect(positionM.x - width / 2.0,
                       positionM.y - height / 2.0,
                       width,
                       height);
        
        return output;
    }
    
    /**
     * update attributes which must be computed
     */
    private void updateDuplicateAttributes()
    {
        getPxRectangle(pxScreen);
    }
    
    /**
     * set position in meters
     * @param x
     * @param y
     */
    private void setPositionM(double x, double y)
    {
        positionM.x = x;
        positionM.y = y;
        
        updateDuplicateAttributes();
    }
}