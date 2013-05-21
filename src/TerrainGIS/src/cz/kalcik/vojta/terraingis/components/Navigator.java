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

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import android.graphics.PointF;
import android.graphics.Rect;
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
    private final static double MIN_ZOOM_TO_LAYER = 0.5;

    // singleton code =====================================================================
    
    private static Navigator instance = new Navigator();
    
    private Navigator() { }
    
    public static Navigator getInstance()
    {
        return instance;
    }
    
    // attributes =========================================================================
    
    private double zoom = DEFAULT_ZOOM; // m/px
    private Coordinate positionM = new Coordinate(0,0);
    private LayerManager layerManager = LayerManager.getInstance();
    private Rect mScreen = new Rect();
    private Envelope mPxScreen = new Envelope(); // area showed in screen in pixels
    
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
     * @throws ParseException 
     * @throws Exception 
     */
    public void setLonLatPosition(double lon, double lat)
            throws Exception, ParseException
    {
        setLonLatPosition(new Coordinate(lon, lat));
    }
    
    /**
     * set position by longitude and latitude
     * @param lonLatPosition
     * @throws ParseException 
     * @throws Exception 
     */
    public void setLonLatPosition(Coordinate lonLatPosition)
            throws Exception, ParseException
    {
        positionM = layerManager.lonLatWGS84ToM(lonLatPosition);
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
        
        double width_half = mScreen.width() / 2.0;
        double height_half = mScreen.height() / 2.0;
        
        output.init(tempPoint.x - width_half,
                    tempPoint.x + width_half,
                    tempPoint.y - height_half,
                    tempPoint.y + height_half);
        
        return output;
    }
    
    /**
     * @return position i meters
     */
    public Coordinate getPositionM()
    {
        return positionM;
    }
    
    /**
     * set position in meters
     * @param position
     */
    public void setPositionM(Coordinate position)
    {
        positionM = position;
        updateDuplicateAttributes();
    }
    
    /**
     * @return screen rectangle
     */
    public Rect getScreen()
    {
        return mScreen;
    }
    
    /**
     * set screen
     * @param screen
     */
    public void setScreen(Rect screen)
    {
        mScreen = screen;
        updateDuplicateAttributes();
    }
    
    /**
     * @return screen rectangle in px
     */
    public Envelope getPxScreen()
    {
        return mPxScreen;
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
    
    /**
     * change zoom and position by envelop
     * @param zoomingEnvelope
     */
    public void zoomToEnvelopeM(Envelope zoomingEnvelope)
    {
        double envelopeWidth = zoomingEnvelope.getWidth();
        double envelopeHeight = zoomingEnvelope.getHeight();
        double screenWidth = mPxScreen.getWidth();
        double screenHeight = mPxScreen.getHeight();
        
        double newZoom;
        if(envelopeWidth/envelopeHeight >
           screenWidth/screenHeight)
        {
            newZoom = envelopeWidth/screenWidth;
        }
        else
        {
            newZoom = envelopeHeight/screenHeight;
        }
        
        if(newZoom < MIN_ZOOM_TO_LAYER)
        {
            newZoom = MIN_ZOOM_TO_LAYER;
        }
        
        setZoom(newZoom);
        
        setPositionM(zoomingEnvelope.centre());
    }
    
    
    /**
     * return rectangle in meters which is showed on screen
     * @param output
     * @return
     */
    public Envelope getMRectangle(Envelope output)
    {        
        if(output == null)
        {
            output = new Envelope();
        }
        
        double width_half = pxToM(mScreen.width()) / 2.0;
        double height_half = pxToM(mScreen.height()) / 2.0;
        
        output.init(positionM.x - width_half,
                positionM.x + width_half,
                positionM.y - height_half,
                positionM.y + height_half);
        
        return output;
    }

    /**
     * convert rectangle in px to rectangle in Surface px
     * @param input
     * @param output
     * @return
     */
    public Rect pxToSurfacePx(Envelope input, Rect output)
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
     * convert px coordinates to coordinates in surface
     * @param input
     * @param output
     * @param pxRectangle
     * @return
     */
    public PointF pxToSurfacePx(Coordinate input, PointF output)
    {
        if(output == null)
        {
            output = new PointF();
        }
        
        output.set((float)(input.x - mPxScreen.getMinX()), (float)(mPxScreen.getMaxY() - input.y));
        
        return output;
    }
    
    
    /**
     * convert m coordinates to px coordinates
     * @param input
     * @param output
     * @return
     */
    public Coordinate mToPx(Coordinate input, Coordinate output)
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
     * convert coordinates in meters to surface px
     * @param input
     * @param output
     * @return
     */
    public PointF mToSurfacePx(Coordinate input, PointF output)
    {
        Coordinate pxCoord = mToPx(input, null);
        return pxToSurfacePx(pxCoord, output);
    }
    
    /**
     * convert points in m to surface px
     * @param input
     * @return
     */
    public PointF[] mToSurfacePx(Coordinate[] input)
    {
        int size = input.length;        
        PointF[] result = new PointF[size];
        
        Coordinate pointPx = new Coordinate();
        
        for(int i = 0; i < size; i++)
        {
            mToPx(input[i], pointPx);
            result[i] = pxToSurfacePx(pointPx, null);
        }
        
        return result;
    }
    
    /**
     * convert pixel coordinates of canvas to meters
     * @param input
     * @param output
     * @return
     */
    public Coordinate surfacePxToM(PointF input, Coordinate output)
    {
        Coordinate pxCoord = surfacePxToPx(input, output);
        return pxToM(pxCoord, output);
    }
    
    /**
     * convert px value to m value
     * @param inputValue
     * @return
     */
    public double pxToM(double inputValue)
    {
        return inputValue * zoom;
    }
    

    /**
     * @return buffer distance by zoom
     */
    public double getBufferDistance()
    {
        return pxToM(ConvertUnits.dp2px(Settings.DP_RADIUS_CLICK));
    }
    // static public methods ==============================================================
    
    static public int mpxToZoomLevel(double zoom)
    {
        double doubleZoomLevel = -(Math.log(zoom/O_EARTH_ZOOM_LEVEL)+8*Math.log(2))/Math.log(2);
        return (int)Math.round(doubleZoomLevel);
    }

    static public double zoomLevelToMpx(int zoomLevel)
    {
        return O_EARTH_ZOOM_LEVEL/Math.pow(2, (zoomLevel+8));
    }

    // private methods ======================================================================
    /**
     * convert m value to px value
     * @param inputValue
     * @return
     */
    private double mToPx(double inputValue)
    {
        return inputValue/zoom;
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
        
        output.x = mPxScreen.getMinX() + input.x;
        output.y = mPxScreen.getMinY() + (mScreen.height() - input.y);
        
        return output;
    }
    
    /**
     * update attributes which must be computed
     */
    private void updateDuplicateAttributes()
    {
        getPxRectangle(mPxScreen);
    }
}