package cz.kalcik.vojta.terraingis.layer;

import java.util.ArrayList;

import cz.kalcik.vojta.geom.Point2D;
import cz.kalcik.vojta.geom.Rectangle2D;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;


/**
 * abstract class for items in vector layer
 * @author jules
 *
 */
public abstract class VectorObject
{
    abstract public void draw(Canvas canvas, Rectangle2D.Double rect, Paint paint);
}