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
package cz.kalcik.vojta.terraingis.layer;

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PointsLayer extends VectorLayer
{
    // constants ==============================================================
    public static final int MIN_POINTS = 1;
    
    // public methods =========================================================
    public PointsLayer(String name, int srid,
                       SpatiaLiteIO spatialite, MapFragment mapFragment)
                               throws Exception
    {
        super(VectorLayerType.POINT, name, srid, spatialite, mapFragment);
    }
	
	/**
     * draw objects to canvas
	 * @throws ParseException 
	 * @throws Exception 
     */
    @Override
    public void draw(Canvas canvas, Envelope rect, boolean drawVertices)
            throws Exception, ParseException
    {
        SpatialiteGeomIterator iter = getObjects(rect);
        while(iter.hasNext())
        {
            float radius;
            Coordinate coordinate;
            Paint paint;
            
            Geometry geometry = iter.next();
            
            if(isEditedObject(iter))
            {
                continue;
            }
            else if(isSelectedObject(iter))
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.SELECTED);
                coordinate = geometry.getCoordinate();
                paint = mSelectedPaint; 
            }
            else
            {
                radius = VectorLayerPaints.getPointRadius(PaintType.DEFAULT);
                coordinate = geometry.getCoordinate();
                paint = mPaint;
            }
            
            Drawer.drawCircleM(canvas, paint, coordinate, radius);
        }
        
        // opened object must be out while loop
        if(hasOpenedEditedObject())
        {
            float radius = VectorLayerPaints.getPointRadius(PaintType.SELECTED);
            Coordinate coordinate = mEditedObject.getVertices().get(0);
            
            Drawer.drawCircleM(canvas, mNotSavedPaint, coordinate, radius);            
            Drawer.drawCircleM(canvas, mStrokeNotSavedPaint, coordinate, radius);
        }
    }
    
    @Override
    protected int getMinCountPoints()
    {
        return MIN_POINTS;
    }
}
