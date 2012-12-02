package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D;

/**
 * class for objects in vector line layer
 * @author jules
 *
 */
public class VectorPolyPoints extends VectorObject
{
    // attributes ==================================================================
    ArrayList<Point2D.Double> points;
    Rectangle2D.Double bound = null;

    // constructors ================================================================
    /**
     * constructor
     */
    public VectorPolyPoints()
    {
        this(null);
    }
    
    public VectorPolyPoints(ArrayList<Point2D.Double> points)
    {
        if(points == null)
        {
            points = new ArrayList<Point2D.Double>();
        }
        this.points = points;
        
        updateBound();
    }
    
    // public methods ===============================================================
    @Override
    public void draw(Canvas canvas, Rectangle2D.Double rect, Paint paint)
    {
        if((bound != null) &&  bound.intersects(rect.x, rect.y, rect.height, rect.width))
        {
            canvas.drawPath(getPathPx(), paint);
        }
    }
    
    /**
     * add latlon points
     * @param points
     */
    public void addLonLatPoints(ArrayList<Point2D.Double> latLonPoints)
    {
        LayerManager layerManager = LayerManager.getInstance();
        for(Point2D.Double latLonPoint: latLonPoints)
        {
            points.add(layerManager.lonLatToM(latLonPoint, null));
        }
        updateBound();
    }
    
    // private methods ==============================================================
    /**
     * return bound of points
     * @param points
     * @param bound
     * @return
     */
    private void updateBound()
    {
        int size = points.size();
        
        if(size == 0)
        {
            bound = null;
        }
        else
        {
            if(bound == null)
            {
                bound = new Rectangle2D.Double();
            }
            
            bound.setRect(points.get(0).x, points.get(0).y, 0, 0);
            
            for(int i=1; i < size; i++)
            {
                bound.add(points.get(i));
            }
        }
    }
    
    /**
     * create array of floats coordinates for canvas.drawLines
     * @param mPoints
     * @return
     */
    private Path getPathPx()
    {
        Path result = new Path();
        int size = points.size();
        if(size < 2)
        {
            return result;
        }
        
        LayerManager layerManager = LayerManager.getInstance();
        PointF tempPoint = layerManager.mToPx(points.get(0), (PointF)null);
        result.moveTo(tempPoint.x, -tempPoint.y);
        
        for(int i = 1; i < size; i++)
        {
            layerManager.mToPx(points.get(i), tempPoint);
            result.lineTo(tempPoint.x, -tempPoint.y);
        }
        
        return result;
    }
}