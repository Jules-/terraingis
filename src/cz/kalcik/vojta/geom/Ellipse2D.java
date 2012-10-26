/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package cz.kalcik.vojta.geom;

public abstract class Ellipse2D
{    
    public static class Double extends Ellipse2D implements Shape
    {
        public double x;
        public double y;
        public double width;
        public double height;

        public Double() {}

        public Double(double x, double y, double w, double h)
        {
            setFrame(x, y, w, h);
        }

        public double getX()
        {
            return x;
        }

        public double getY()
        {
            return y;
        }

        public double getWidth()
        {
            return width;
        }

        public double getHeight()
        {
            return height;
        }

        public boolean isEmpty()
        {
            return (width <= 0.0 || height <= 0.0);
        }

        public void setFrame(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
       
        public boolean contains(double x, double y)
        {
            // Normalize the coordinates compared to the ellipse
            // having a center at 0,0 and a radius of 0.5.
            double ellw = getWidth();
            if (ellw <= 0.0) {
                return false;
            }
            double normx = (x - getX()) / ellw - 0.5;
            double ellh = getHeight();
            if (ellh <= 0.0) {
                return false;
            }
            double normy = (y - getY()) / ellh - 0.5;
            return (normx * normx + normy * normy) < 0.25;
        }

        public boolean contains(Point2D point)
        {
            return contains(point.getX(), point.getY());
        }
        
        public boolean contains(double x, double y, double w, double h)
        {
            return (contains(x, y) &&
                    contains(x + w, y) &&
                    contains(x, y + h) &&
                    contains(x + w, y + h));
        }

        public boolean contains(Rectangle2D rectangle)
        {
            return contains(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }
        
        public Rectangle2D getBounds2D()
        {
            return new Rectangle2D.Double(x, y, width, height);
        }
    }

    public abstract double getX();
    public abstract double getY();
    public abstract double getWidth();
    public abstract double getHeight();
    
    protected Ellipse2D()
    {
    }

    public int hashCode()
    {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ellipse2D) {
            Ellipse2D e2d = (Ellipse2D) obj;
            return ((getX() == e2d.getX()) &&
                    (getY() == e2d.getY()) &&
                    (getWidth() == e2d.getWidth()) &&
                    (getHeight() == e2d.getHeight()));
        }
        return false;
    }
}
