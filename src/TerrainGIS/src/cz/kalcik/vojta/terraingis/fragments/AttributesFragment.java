/**
 * 
 */
package cz.kalcik.vojta.terraingis.fragments;

import java.util.ArrayList;
import java.util.NavigableMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import jsqlite.Exception;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import cz.kalcik.vojta.terraingis.MainActivity;
import cz.kalcik.vojta.terraingis.R;
import cz.kalcik.vojta.terraingis.components.ListBackgroundColors;
import cz.kalcik.vojta.terraingis.components.Navigator;
import cz.kalcik.vojta.terraingis.dialogs.RemoveObjectDialog;
import cz.kalcik.vojta.terraingis.dialogs.UpdateAttributesDialog;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatialiteAttributesIterator;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import cz.kalcik.vojta.terraingis.view.AttributeTableRow;
import cz.kalcik.vojta.terraingis.view.VScroll;

/**
 * @author jules
 *
 */
public class AttributesFragment extends PanelFragment
{
    // attributes =========================================================
    private VectorLayer mLayer = null;
    private TableLayout mTable;
    private ArrayList<AttributeTableRow> mRows = new ArrayList<AttributeTableRow>();
    private AttributeTableRow mSelectedRow = null;
    private AttributeTableRow mTouchedRow = null;
    
    private VScroll mVScroll;
    
    // public methods =====================================================
    
    /**
     * set background colors of rows
     */
    public void setBackgroundColors()
    {
        ListBackgroundColors colors = new ListBackgroundColors(mMainActivity);
        
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
    
    /**
     * remove selected row
     */
    public void removeSelectedRow()
    {
        mTable.removeView(mSelectedRow);
    }
    
    /**
     * @param index
     * @return row with index
     */
    public AttributeTableRow getRowAtIndex(int index)
    {
        return (AttributeTableRow) mTable.getChildAt(index);
    }
    
    @Override
    protected void switchToMe()
    {
        mMainActivity.getLayersLayout().setVisibility(View.GONE);
        mMainActivity.getAttributesLayout().setVisibility(View.VISIBLE);
        
        VectorLayer layer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
        if(!layer.equals(mLayer))
        {
            clear();
            mLayer = layer;
        
            if(mLayer != null)
            {
                try
                {
                    loadAttributes();
                }
                catch (Exception e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    // getter setter ======================================================
    
    /**
     * @param mTouchedRow the mTouchedRow to set
     */
    public void setTouchedRow(AttributeTableRow touchedRow)
    {
        this.mTouchedRow = touchedRow;
    }
    
    /**
     * set selected row as touched row
     */
    public void selectedRowIsTouched()
    {
        mSelectedRow = mTouchedRow;
    }
    
    // on methods =========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.attribute_table_layout, container, false);
        
        setCommon(myView);
        
        // buttons
        ImageButton buttonZoomObject = (ImageButton)myView.findViewById(R.id.button_zoom_to_object);
        buttonZoomObject.setOnClickListener(zoomObjectHandler);
        ImageButton buttonEditObject = (ImageButton)myView.findViewById(R.id.button_edit);
        buttonEditObject.setOnClickListener(editHandler);
        ImageButton buttonRemoveObject = (ImageButton)myView.findViewById(R.id.button_remove);
        buttonRemoveObject.setOnClickListener(removeHandler);
        
        // views
        mVScroll = (VScroll)myView.findViewById(R.id.vScroll);
        mVScroll.initView(this);
        mTable = (TableLayout)myView.findViewById(R.id.attributeTable);
        
        return myView;
    }
    
    // private methods =========================================================
    /**
     * load attributes from db
     * @throws Exception 
     */
    private void loadAttributes() throws Exception
    {
        LayoutInflater inflater = mMainActivity.getLayoutInflater();
        
        // header
        TableRow row = new TableRow(mMainActivity);
        for(Column column: mLayer.getAttributeHeader().getColumns())
        {
            TextView cell = (TextView)inflater.inflate(R.layout.attribute_table_cell_header, null);
            cell.setText(column.name);
            row.addView(cell);
        }
        mTable.addView(row);
        // table body
        AttributeHeader header = mLayer.getAttributeHeader();
        SpatialiteAttributesIterator iterValues = mLayer.getAttributes();
        int count = header.getCountColumns();
        
        AttributeTableRow bodyRow;
        while(iterValues.hasNext())
        {
            bodyRow = new AttributeTableRow(mMainActivity);
            String[] values = iterValues.next();
            bodyRow.setRowid(iterValues.getLastROWID());
            mRows.add(bodyRow);
            
            bodyRow.createCells(inflater, values, count);
            
            mTable.addView(bodyRow);
        }
        
        setBackgroundColors();
    }
    
    /**
     * clear table
     */
    private void clear()
    {
        mTable.removeAllViews();
    }
    // handlers ===============================================================
    
    /**
     * zoom to selected object
     */
    View.OnClickListener zoomObjectHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(mSelectedRow != null)
            {
                try
                {
                    Geometry object = mLayer.getObject(mSelectedRow.getRowid());
                    Navigator.getInstance().zoomToEnvelopeM(object.getEnvelopeInternal());
                    
                    mMainActivity.getMapFragment().getMap().invalidate();
                }
                catch (Exception e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
                catch (ParseException e)
                {
                    Toast.makeText(mMainActivity, R.string.database_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    
    /**
     * edit slected object
     */
    View.OnClickListener editHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            UpdateAttributesDialog dialog = new UpdateAttributesDialog();
            dialog.setLayer(mLayer);
            dialog.setRow(mSelectedRow);
            mMainActivity.showDialog(dialog);
        }
    };
    
    /**
     * remove selected object
     */
    View.OnClickListener removeHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            RemoveObjectDialog dialog = new RemoveObjectDialog();
            dialog.setMessage(getResources().getString(R.string.confirm_remove_object_message));
            dialog.setRowid(mSelectedRow.getRowid());
            dialog.setLayerName(mLayer.getData().name);
            mMainActivity.showDialog(dialog);
        }
    };
}
