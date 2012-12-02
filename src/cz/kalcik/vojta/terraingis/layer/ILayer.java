package cz.kalcik.vojta.terraingis.layer;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface ILayer
{
    public void draw(final Canvas canvas, Rect screenRect);
}