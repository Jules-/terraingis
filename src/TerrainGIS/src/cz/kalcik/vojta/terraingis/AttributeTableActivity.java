/**
 * 
 */
package cz.kalcik.vojta.terraingis;

import java.util.Iterator;

import cz.kalcik.vojta.terraingis.fragments.LayersFragment;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO;
import cz.kalcik.vojta.terraingis.io.SpatiaLiteIO.Layer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
import cz.kalcik.vojta.terraingis.layer.LayerManager;
import cz.kalcik.vojta.terraingis.layer.VectorLayer;
import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
import android.app.Activity;
import android.os.Bundle;
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
    
    // on methods ================================================================
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_table);
        mTable = (TableLayout)findViewById(R.id.attributeTable);
        
        Bundle bundle = getIntent().getExtras();
        String layerName = bundle.getString(LayersFragment.LAYER_ATTRIBUTE_TABLE);
        mSpatialite = new SpatiaLiteIO(MainActivity.DB_FILE.getAbsolutePath());
        Layer values = mSpatialite.getLayer(layerName);
        if(values != null)
        {
            mLayer = LayerManager.createVectorLayer(values, mSpatialite);
            loadAttributes();
        }
    }
    
    // private methods =========================================================
    /**
     * load attributes from db
     */
    private void loadAttributes()
    {
        // header
        TableRow headRow = new TableRow(this);
        for(Column column: mLayer.getAttributeHeader().getColumns())
        {
            TextView cell = new TextView(this);
            cell.setText(column.name);
            headRow.addView(cell);
        }
        mTable.addView(headRow);
        // table body
        AttributeHeader header = mLayer.getAttributeHeader();
        Iterator<String[]> iterValues =
                mSpatialite.getAttributes(mLayer.getData().name, header);
        int count = header.getCountColumns();
        while(iterValues.hasNext())
        {
            headRow = new TableRow(this);
            String[] values = iterValues.next();
            
            for(int i = 0; i< count; i++)
            {
                TextView cell = new TextView(this);
                cell.setText(values[i]);
                headRow.addView(cell);
            }
            mTable.addView(headRow);
        }
    }
}
