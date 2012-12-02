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

public abstract class Rectangle2D
{
    public static final int OUT_LEFT = 1;
    public static final int OUT_TOP = 2;
    public static final int OUT_RIGHT = 4;
    public static final int OUT_BOTTOM = 8;


    public static class Double extends Rectangle2D
    {
        public double x;
        public double y;
        public double width;
        public double height;

        public Double() {}

        public Double(double x, double y, double w, double h)
        {
            setRect(x, y, w, h);
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
            return (width <= 0.0) || (height <= 0.0);
        }

        public void setRect(double x, double y, double w, double h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        public void setRect(Rectangle2D r)
        {
            this.x = r.getX();
            this.y = r.getY();
            this.width = r.getWidth();
            this.height = r.getHeight();
        }

        public int outcode(double x, double y)
        {
            int out = 0;
            if (this.width <= 0) {
                out |= OUT_LEFT | OUT_RIGHT;
            } else if (x < this.x) {
                out |= OUT_LEFT;
            } else if (x > this.x + this.width) {
                out |= OUT_RIGHT;
            }
            if (this.height <= 0) {
                out |= OUT_TOP | OUT_BOTTOM;
            } else if (y < this.y) {
                out |= OUT_TOP;
            } else if (y > this.y + this.height) {
                out |= OUT_BOTTOM;
            }
            return out;
        }

        public Rectangle2D getBounds2D()
        {
            return new Double(x, y, width, height);
        }

        public String toString()
        {
            return getClass().getName()
                + "[x=" + x +
                ",y=" + y +
                ",w=" + width +
                ",h=" + height + "]";
        }
        
        public double getMinX()
        {
            return getX();
        }
        
        public double getMinY()
        {
            return getY();
        }
        
        public double getMaxX()
        {
            return getX() + getWidth();
        }

        public double getMaxY()
        {
            return getY() + getHeight();
        }
        
        public void setFrame(double x, double y, double w, double h)
        {
            setRect(x, y, w, h);
        }
        
        public boolean intersects(double x, double y, double w, double h)
        {
            if (isEmpty() || w <= 0 || h <= 0)
            {
                return false;
            }
            double x0 = getX();
            double y0 = getY();
            return (x + w > x0 &&
                    y + h > y0 &&
                    x < x0 + getWidth() &&
                    y < y0 + getHeight());
        }
    }

    protected Rectangle2D() {}
    public abstract void setRect(double x, double y, double w, double h);

    public void setRect(Rectangle2D r)
    {
        setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public abstract double getX();
    public abstract double getY();
    public abstract double getWidth();
    public abstract double getHeight();
    public abstract double getMinX();  
    public abstract double getMinY();
    public abstract double getMaxX();
    public abstract double getMaxY();

    
    public abstract boolean isEmpty();

    public abstract int outcode(double x, double y);
    
    public int outcode(Point2D p)
    {
        return outcode(p.getX(), p.getY());
    }

    public boolean contains(double x, double y)
    {
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }

    public boolean contains(double x, double y, double w, double h)
    {
        if (isEmpty() || w <= 0 || h <= 0)
        {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                (x + w) <= x0 + getWidth() &&
                (y + h) <= y0 + getHeight());
    }

    public void add(double newx, double newy)
    {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public void add(Point2D pt)
    {
        add(pt.getX(), pt.getY());
    }


    public void add(Rectangle2D r) {
        double x1 = Math.min(getMinX(), r.getMinX());
        double x2 = Math.max(getMaxX(), r.getMaxX());
        double y1 = Math.min(getMinY(), r.getMinY());
        double y2 = Math.max(getMaxY(), r.getMaxY());
        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public int hashCode()
    {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Rectangle2D) {
            Rectangle2D r2d = (Rectangle2D) obj;
            return ((getX() == r2d.getX()) &&
                    (getY() == r2d.getY()) &&
                    (getWidth() == r2d.getWidth()) &&
                    (getHeight() == r2d.getHeight()));
        }
        return false;
    }
}