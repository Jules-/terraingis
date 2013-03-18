/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.io.SpatialiteAttributesIterator;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author jules
 * activity for showing attribute table
 */
public class AttributeTableActivity extends Activity
{
    // attributes ================================================================
    private SpatiaLiteIO mSpatialite;
    private VectorLayer mLayer;
    private TableLayout mTable;
    private ArrayList<ObjectRow> mRows = new ArrayList<ObjectRow>();
    private TableRow mSelectedRow = null;
    
    private float mx, my;
    private boolean mValidScroll = false;
    private ScrollView mVScroll;
    private HorizontalScrollView mHScroll;
    
    
    // on methods ================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_table);
        mVScroll = (ScrollView) findViewById(R.id.vScroll);
        mHScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        mTable = (TableLayout)findViewById(R.id.attributeTable);
        
        // layer
        Bundle bundle = getIntent().getExtras();
        String layerName = bundle.getString(LayersFragment.LAYER_ATTRIBUTE_TABLE);
        mSpatialite = new SpatiaLiteIO(MainActivity.DB_FILE.getAbsolutePath());
        Layer values = mSpatialite.getLayer(layerName);
        
        // actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(layerName);
        
        // load values
        if(values != null)
        {
            mLayer = LayerManager.createVectorLayer(values, mSpatialite);
            loadAttributes();
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float curX, curY;

        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN)
        {
            mx = event.getX();
            my = event.getY();
            mValidScroll = true;
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            curX = event.getX();
            curY = event.getY();
            if(mValidScroll)
            {
                mVScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            }
            mx = curX;
            my = curY;
            mValidScroll = true;
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            curX = event.getX();
            curY = event.getY();
            if(mValidScroll)
            {
                mVScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            }
            mValidScroll = false;
        }

        return false;
    }
    // private methods =========================================================
    /**
     * load attributes from db
     */
    private void loadAttributes()
    {
        LayoutInflater inflater = getLayoutInflater();
        
        // header
        TableRow row = new TableRow(this);
        for(Column column: mLayer.getAttributeHeader().getColumns())
        {
            TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell_header, null);
            cell.setText(column.name);
            row.addView(cell);
        }
        mTable.addView(row);
        // table body
        AttributeHeader header = mLayer.getAttributeHeader();
        SpatialiteAttributesIterator iterValues =
                (SpatialiteAttributesIterator)mSpatialite.getAttributes(mLayer.getData().name, header);
        int count = header.getCountColumns();
        while(iterValues.hasNext())
        {
            row = new TableRow(this);
            row.setOnClickListener(selectRowListener);
            String[] values = iterValues.next();
            mRows.add(new ObjectRow(iterValues.getLastROWID(), row));
            
            for(int i = 0; i< count; i++)
            {
                TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell, null);
                cell.setText(values[i]);
                row.addView(cell);
            }
            mTable.addView(row);
        }
        
        setBackgroundColors();
    }
    
    /**
     * set background colors of rows
     */
    private void setBackgroundColors()
    {
        ListBackgroundColors colors = new ListBackgroundColors(this);
        
        for(ObjectRow row: mRows)
        {
            if(row.tableRow.equals(mSelectedRow))
            {
                row.tableRow.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }
            else
            {
                row.tableRow.setBackgroundColor(colors.getNextColor());
            }
        }
    }
    // handlers =======================================================================================
    View.OnClickListener selectRowListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mSelectedRow = (TableRow) v;
            setBackgroundColors();
        }
    };
    
    // classes =========================================================================================
    
    private class ObjectRow
    {
        public TableRow tableRow;
        public String rowid;
        
        public ObjectRow(String rowid, TableRow tableRow)
        {
            this.rowid = rowid;
            this.tableRow = tableRow;
        }
    }
}
