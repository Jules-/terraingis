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

import java.io.Serializable;

import jsqlite.Exception;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;

import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;

import android.graphics.Canvas;

/**
 * parent of all layers
 * @author jules
 *
 */
public abstract class AbstractLayer
{
    // attributes ==============================================================
    public static class AbstractLayerData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public boolean visible;
        public String name;
        public Serializable childData;

        public AbstractLayerData(boolean mVisible)
        {
            this.visible = mVisible;
        }
    }
    
    protected Envelope mEnvelope;
    protected int mSrid;
    protected LayerManager mLayerManager = LayerManager.getInstance();
    protected AbstractLayerData data = new AbstractLayerData(true);
    protected Navigator mNavigator = Navigator.getInstance();

    // abstract methods ========================================================
    public abstract void draw(final Canvas canvas, Envelope rect, boolean drawVertexs)
            throws Exception, ParseException;
    public abstract void detach();
    
    // getter, setter ==========================================================
    /**
     * @return envelope in srid of layer
     */
    public Envelope getEnvelope()
    {
        return mEnvelope;
    }

    /**
     * @param srid
     * @return envelope in srid from argument
     * @throws Exception
     * @throws ParseException
     */
    public Envelope getEnvelope(int srid) throws Exception, ParseException
    {
        Envelope result = mEnvelope;
        
        if(mSrid != srid)
        {
            SpatiaLiteIO spatialite = mLayerManager.getSpatialiteIO();
            result = spatialite.transformSRSEnvelope(result, mSrid, srid);
        }
        
        return result;
    }
    
    /**
     * @return srid of layer
     */
    public int getSrid()
    {
        return mSrid;
    }
    
    /**
     * Is layer visible?
     * @return
     */
    public boolean isVisible()
    {
        return data.visible;
    }
    
    /**
     * @return the data
     */
    public AbstractLayerData getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(AbstractLayerData data)
    {
        this.data = data;
    }    
    // public methods =========================================================
    @Override
    public String toString()
    {
        return data.name;
    }
    
    /**
     * Toggle visibility
     */
    public void toggleVisibility()
    {
        data.visible = !data.visible;
    }
}