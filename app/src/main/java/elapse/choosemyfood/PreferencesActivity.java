package elapse.choosemyfood;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.NumberPicker;

/**
 * Created by Joshua on 9/10/2017.
 */

public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(myToolbar != null) {
            setSupportActionBar(myToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
        //MenuItem menuItem = menu.findItem(R.id.menu_settings);
        //menuItem.setVisible(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }
    private void setupView(){
        PreferencesSingleton pref = PreferencesSingleton.getInstance();

        NumberPicker radiusPicker = (NumberPicker) findViewById(R.id.max_radius);
        radiusPicker.setMaxValue(50);
        radiusPicker.setMinValue(1);
        radiusPicker.setValue(Integer.parseInt(pref.getRadius())/1000);


    }
}
