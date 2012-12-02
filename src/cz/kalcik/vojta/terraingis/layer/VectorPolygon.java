package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import cz.kalcik.vojta.geom.Point2D;

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
}