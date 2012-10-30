package cz.kalcik.vojta.terraingis;

import java.util.ArrayList;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

import cz.kalcik.vojta.geom.Point2D;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends FragmentActivity
{   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button run_button = (Button) findViewById(R.id.button_run);
        run_button.setOnClickListener(run_handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    View.OnClickListener run_handler = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            run();
        }
    };
    
    private void run()
    {
        Spinner coord_spinner = (Spinner) findViewById(R.id.spinner_coords);
        
        String value_coord = coord_spinner.getSelectedItem().toString();
        
        ArrayList<String> params = new ArrayList<String>();
        if(value_coord.equals("UTM-33"))
        {
            params.add("+proj=utm");
            params.add("+zone=33");
            params.add("+ellps=WGS84");
            params.add("+datum=WGS84");
            params.add("+units=m");
            params.add("+no_defs");
            params.add("+towgs84=0,0,0");
        }
        
        Projection projection = ProjectionFactory.fromPROJ4Specification(params.toArray(new String[params.size()]));
        
        EditText text_edit_x = (EditText) findViewById(R.id.editText_x_coord);
        EditText text_edit_y = (EditText) findViewById(R.id.editText_y_coord);

        Point2D.Double src = new Point2D.Double(Double.parseDouble(text_edit_x.getText().toString()),
                                                Double.parseDouble(text_edit_y.getText().toString()));
        Point2D.Double dst = new Point2D.Double();
        
        EditText text_edit_count = (EditText) findViewById(R.id.editText_count);
        int count = Integer.parseInt(text_edit_count.getText().toString());
        for(int i=0; i < count; i++)
        {
            projection.transform(src, dst);
        }
        
        TextView result_view = (TextView) findViewById(R.id.textView_output);
        
        result_view.setText(String.format("%f %f", dst.getX(), dst.getY()));
    }
}
