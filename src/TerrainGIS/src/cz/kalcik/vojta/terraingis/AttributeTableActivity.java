/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;

import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.dialogs.UpdateAttributesDialog;
import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.io.SpatialiteAttributesIterator;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.view.AttributeTableRow;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author jules
 * activity for showing attribute table
 */
public class AttributeTableActivity extends AbstractActivity
{
    // attributes ================================================================
    private SpatiaLiteIO mSpatialite;
    private VectorLayer mLayer;
    private TableLayout mTable;
    private ArrayList<AttributeTableRow> mRows = new ArrayList<AttributeTableRow>();
    private AttributeTableRow mSelectedRow = null;
    private AttributeTableRow mTouchedRow = null;
    private GestureDetector mGestureDetector;
    
    private float mx, my;
    private ScrollView mVScroll;
    private HorizontalScrollView mHScroll;
    
    // public methods ============================================================
        
    // getter, setter ============================================================
    
    /**
     * @param mTouchedRow the mTouchedRow to set
     */
    public void setTouchedRow(AttributeTableRow touchedRow)
    {
        this.mTouchedRow = touchedRow;
    }

    // on methods ================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_table);
        mVScroll = (ScrollView) findViewById(R.id.vScroll);
        mHScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        mTable = (TableLayout)findViewById(R.id.attributeTable);
        mGestureDetector = new GestureDetector(this, new MySimpleOnGestureListener());
        
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
        if(mGestureDetector.onTouchEvent(event))
        {
            return true;
        }
        
        float curX, curY;

        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN)
        {
            mx = event.getX();
            my = event.getY();
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            curX = event.getX();
            curY = event.getY();
            mVScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            mx = curX;
            my = curY;
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            curX = event.getX();
            curY = event.getY();
            mVScroll.scrollBy((int) (mx - curX), (int) (my - curY));
            mHScroll.scrollBy((int) (mx - curX), (int) (my - curY));
        }

        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_attribute_table, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        // remove
        if(id == R.id.menu_remove)
        {

        }
        // edit
        else if(id == R.id.menu_edit)
        {
            if(mSelectedRow != null)
            {
                UpdateAttributesDialog dialog = new UpdateAttributesDialog();
                dialog.setLayer(mLayer);
                dialog.setValues(mSelectedRow.getValues());
                showDialog(dialog);
            }
        }
     
        return true;
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
        
        AttributeTableRow bodyRow;
        while(iterValues.hasNext())
        {
            bodyRow = new AttributeTableRow(this);
            String[] values = iterValues.next();
            bodyRow.setRowid(iterValues.getLastROWID());
            mRows.add(bodyRow);
            
            for(int i = 0; i< count; i++)
            {
                TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell, null);
                cell.setText(values[i]);
                bodyRow.addView(cell);
            }
            mTable.addView(bodyRow);
        }
        
        setBackgroundColors();
    }
    
    /**
     * set background colors of rows
     */
    private void setBackgroundColors()
    {
        ListBackgroundColors colors = new ListBackgroundColors(this);
        
        for(AttributeTableRow row: mRows)
        {
            int rowColor = colors.getNextColor();
            
            if(row.equals(mSelectedRow))
            {
                row.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }
            else
            {
                row.setBackgroundColor(rowColor);
            }
        }
    }
    // handlers =======================================================================================
    
    // classes =========================================================================================
    /**
     * detector for gestures
     * @author jules
     *
     */
    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent event) 
        {
            mSelectedRow = mTouchedRow;
            setBackgroundColors();
            
            return true;
        }        
    }
}
