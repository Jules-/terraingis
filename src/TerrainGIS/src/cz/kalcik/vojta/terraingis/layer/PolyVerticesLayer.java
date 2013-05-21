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

import java.util.ArrayList;

import jsqlite.Exception;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.Drawer;
import cz.kalcik.vojta.terraingis.fragments.MapFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteGeomIterator;
import cz.kalcik.vojta.terraingis.layer.VectorLayerPaints.PaintType;

/**
 * @author jules
 *
 */
public abstract class PolyVerticesLayer extends VectorLayer
{
    protected Paint mSelectedVertexSelectedObjectPaint;
    protected Paint mVertexsSelectedObjectPaint;
    protected Paint mStrokePolygonPaint;
    
    public PolyVerticesLayer(VectorLayerType type, String name, int srid,
            SpatiaLiteIO spatialite, MapFragment mapFragment) throws Exception
    {
        super(type, name, srid, spatialite, mapFragment);
        
        mVertexsSelectedObjectPaint = VectorLayerPaints.getVertex(PaintType.DEFAULT);
        mSelectedVertexSelectedObjectPaint = VectorLayerPaints.getVertex(PaintType.SELECTED);
        mStrokePolygonPaint = VectorLayerPaints.getLine(PaintType.SELECTED);
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
        // saved objects
        SpatialiteGeomIterator iter = getObjects(rect);
        while(iter.hasNext())
        {
            Geometry geometry = iter.next();

            if(!isEditedObject(iter))
            {
                PointF[] points = mNavigator.mToSurfacePx(geometry.getCoordinates());
                boolean isSelected = isSelectedObject(iter);           
                
                Paint paint = mPaint;
                // paint for selected
                if(isSelected)
                {
                    paint = mSelectedPaint;   
                }
                
                Drawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        paint);
                
                
                // stroke of polygon
                if(isSelected && mType == VectorLayerType.POLYGON)
                {
                    Drawer.drawCanvasPathSurfacePx(canvas,
                            points, 
                            mStrokePolygonPaint);                
                }

                
                if(drawVertices || isSelected)
                {
                    Drawer.drawVertexsSurfacePx(canvas, points, mVertexsSelectedObjectPaint,
                            VectorLayerPaints.getVertexRadius());                    
                }
            }
        }
        
        // recording of object
        if(mEditedObject.isOpened())
        {
            ArrayList<Coordinate> vertices = mEditedObject.getVertices();
            Coordinate[] metersCoordinates = vertices.toArray(
                    new Coordinate[vertices.size()]);
            PointF[] points = mNavigator.mToSurfacePx(metersCoordinates);
            
            Drawer.drawCanvasPathSurfacePx(canvas,
                    points, mNotSavedPaint);
            
            // stroke of polygon
            if(mType == VectorLayerType.POLYGON)
            {
                Drawer.drawCanvasPathSurfacePx(canvas,
                        points, 
                        mStrokeNotSavedPaint);                
            }
            
            Drawer.drawVertexsSurfacePx(canvas, points, mVertexsSelectedObjectPaint,
                    mSelectedVertexSelectedObjectPaint,
                    VectorLayerPaints.getVertexRadius(), mEditedObject.getSelectedVertexIndex());
        }
    }
}
