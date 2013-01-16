package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D.Double;
import cz.kalcik.vojta.terraingis.components.Navigator;

public class VectorPolygon extends VectorPolyPoints
{
    // constructors ================================================
    /**
     * constructor
     */
    public VectorPolygon()
    {
        super();
    }
    
    /**
     * constructor
     * @param points
     */
    public VectorPolygon(ArrayList<Point2D.Double> points)
    {
        super(points);
    }
    
    // public methods ===============================================

    @Override
    public void draw(Canvas canvas, Double rect, Paint paint)
    {
        if(isObjectInRect(rect))
        {
            Navigator.getInstance().drawCanvasPathM(canvas, points, paint);
        }
    }
}