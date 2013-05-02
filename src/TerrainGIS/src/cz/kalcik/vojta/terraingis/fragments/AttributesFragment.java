/**
 * 
 */
package cz.kalcik.vojta.terraingis.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.NavigableMap;

import com.vividsolutions.jts.geom.Envelope;
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
    // constants =====================================================================================
    private static final String TAG_SAVESTATE = "cz.kalcik.vojta.terraingis.AttributesFragmentSaveState";
    
    // attributes =========================================================
    private static class AttributesFragmentData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public String selectedRowId;
    }
    
    private VectorLayer mLayer = null;
    private TableLayout mTable;
    private ArrayList<AttributeTableRow> mRows = new ArrayList<AttributeTableRow>();
    private AttributeTableRow mTouchedRow = null;
    
    private VScroll mVScroll;
    
    private AttributesFragmentData mData = new AttributesFragmentData();
    
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
            
            if(row.getRowid().equals(mData.selectedRowId))
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
        mTable.removeView(findRowByRowId(mData.selectedRowId));
    }
    
    /**
     * @param index
     * @return row with index
     */
    public AttributeTableRow getRowAtIndex(int index)
    {
        return (AttributeTableRow) mTable.getChildAt(index);
    }
    
    /**
     * select row whitch has rowid
     * @param rowid
     */
    public void selectItemWithRowid(String rowid)
    {
        mData.selectedRowId = rowid;
        
        setBackgroundColors();
    }
    
    /**
     * clear selection of row
     */
    public void clearSelection()
    {
        mData.selectedRowId = null;
    }
    
    /**
     * reload attributes table
     */
    public void reload()
    {
        clear();
        
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
        if(mTouchedRow == null)
        {
            return;
        }
        
        mData.selectedRowId = mTouchedRow.getRowid();

        try
        {
            Geometry object = mLayer.getObject(mData.selectedRowId);
            Envelope envelopeObject = object.getEnvelopeInternal();
            Navigator navigator = Navigator.getInstance();
            if(!navigator.getMRectangle(null).intersects(envelopeObject))
            {
                navigator.setPositionM(envelopeObject.centre());
            }
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
        
        if(mMainActivity.canSelectObject())
        {
            mLayer.selectObject(mData.selectedRowId);
            mMainActivity.getMapFragment().getMap().invalidate();
        }
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
        clear();
        
        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {       
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null)
        {
            mData = (AttributesFragmentData) savedInstanceState.getSerializable(TAG_SAVESTATE);
            setBackgroundColors();
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {        
        super.onSaveInstanceState(outState);

        // Map view state
        outState.putSerializable(TAG_SAVESTATE, mData);
    }
    // protected methods =======================================================
    @Override
    protected void switchToMeChild()
    {
        mMainActivity.getLayersLayout().setVisibility(View.GONE);
        mMainActivity.getAttributesLayout().setVisibility(View.VISIBLE);
        
        VectorLayer layer = mMainActivity.getLayersFragment().getSelectedLayerIfVector();
        
        if(layer == null || !layer.equals(mLayer))
        {
            mLayer = layer;
        
            reload();
        }
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
        mRows.clear();
    }
    
    /**
     * find row with rowid
     * @param rowid
     * @return
     */
    private AttributeTableRow findRowByRowId(String rowid)
    {
        for(AttributeTableRow row: mRows)
        {
            if(row.getRowid().equals(mData.selectedRowId))
            {
                return row;
            }
        }
        
        return null;
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
            if(mData.selectedRowId == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_object, Toast.LENGTH_LONG).show();
                return;
            }
            
            try
            {
                Geometry object = mLayer.getObject(mData.selectedRowId);
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
    };
    
    /**
     * edit slected object
     */
    View.OnClickListener editHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(mData.selectedRowId == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_object, Toast.LENGTH_LONG).show();
                return;
            }
            
            UpdateAttributesDialog dialog = new UpdateAttributesDialog();
            dialog.setLayer(mLayer);
            dialog.setRow(findRowByRowId(mData.selectedRowId));
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
            if(mData.selectedRowId == null)
            {
                Toast.makeText(mMainActivity, R.string.not_selected_object, Toast.LENGTH_LONG).show();
                return;
            }
            
            RemoveObjectDialog dialog = new RemoveObjectDialog();
            dialog.setMessage(getResources().getString(R.string.confirm_remove_object_message));
            dialog.setRowid(mData.selectedRowId);
            mMainActivity.showDialog(dialog);
        }
    };
}
