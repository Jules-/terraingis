/**
 * shapefilelib
 * based on Thomas Diewald's diewald_shapeFileReader
 *                                 http://thomasdiewald.com/blog/?p=1382
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

package cz.kalcik.vojta.shapefilelib.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import cz.kalcik.vojta.shapefilelib.shapeFile.ShapeFile;

/**
 * base class for Shape-File-Readers. (*.shx, *.shp, *.dbf, ...).
 * 
 * @author thomas diewald (2012).
 * 
 */
public abstract class ShapeFileReader
{

    protected ShapeFile parent_shapefile;
    protected File file;

    public ShapeFileReader(ShapeFile parent_shapefile, File file)
    {
        this.parent_shapefile = parent_shapefile;
        this.file = file;
    }

    public ByteBuffer getFileBytes() throws IOException
    {
        return ShapeFileReader.loadFile(file);
    }
    
    public abstract void read() throws Exception;

    public abstract void printHeader();

    public abstract void printContent();

    public static ByteBuffer loadFile(File file) throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte data[] = new byte[bis.available()];
        bis.read(data);
        bis.close();
        is.close();
        return ByteBuffer.wrap(data);
    }

    public ShapeFile getShapeFile()
    {
        return parent_shapefile;
    }

    public File getFile()
    {
        return file;
    }
    
    /**
     * write bytes to buffer
     * @param buffer
     * @throws IOException
     */
    protected void writeBytesToFile(ByteBuffer buffer) throws IOException
    {
        FileChannel output = new FileOutputStream(file.getAbsolutePath()).getChannel();
        
        buffer.position(0);        
        output.write(buffer);
        output.close();
    }
}
