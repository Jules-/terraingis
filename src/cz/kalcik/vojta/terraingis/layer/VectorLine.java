package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import cz.kalcik.vojta.geom.Point2D;

public class VectorLine extends VectorPolyPoints
{
    // constructors ================================================
    /**
     * constructor
     */
    public VectorLine()
    {
        super();
    }
    
    /**
     * constructor
     * @param points
     */
    public VectorLine(ArrayList<Point2D.Double> points)
    {
        super(points);
    }    
}