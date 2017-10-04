package elapse.choosemyfood;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Activity to choose preferences for search
 */

public class PreferencesActivity extends AppCompatActivity {
    private SeekBar radiusPicker;
    private TextView radiusText;
    private NumberPicker minPrice;
    private NumberPicker maxPrice;
    private EditText keywordTextInput;
    private ArrayList<String> keywords;
    private CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(myToolbar != null) {
            setSupportActionBar(myToolbar);
            getSupportActionBar().openOptionsMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setupView();
        GridView gridview = (GridView) findViewById(R.id.keyword_list);
        adapter = new CustomAdapter(PreferencesActivity.this,R.id.grid_text,keywords);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Preferences","Gridviewclicked");
                keywords.remove(position);
                adapter.notifyDataSetChanged();
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_settings).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setOptionValues();
                boolean fromShowActivity = getIntent().getBooleanExtra("from_show",false);
                if(!fromShowActivity){
                    onBackPressed();
                }else{
                    Intent i = new Intent(PreferencesActivity.this, ShowActivity.class);
                    startActivity(i);
                }
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }
    //Set view values and onclickListeners.
    private void setupView(){
        PreferencesSingleton pref = PreferencesSingleton.getInstance();
        this.keywords = pref.getKeywords();
        keywordTextInput = (EditText) findViewById(R.id.keywords);

        this.radiusPicker = (SeekBar) findViewById(R.id.max_radius);
        radiusPicker.setProgress(Integer.parseInt(pref.getRadius())/1000);
        this.radiusText =  (TextView) findViewById(R.id.radius_text);
        radiusText.setText("Max Distance: "+radiusPicker.getProgress()+"km"+"(~"+Math.round(radiusPicker.getProgress()/1.609)+"mi)");

        this.minPrice = (NumberPicker) findViewById(R.id.min_price);
        this.maxPrice = (NumberPicker) findViewById(R.id.max_price);
        minPrice.setMinValue(0);
        minPrice.setValue(Integer.parseInt(pref.getMinPrice()));
        maxPrice.setMaxValue(4);
        maxPrice.setValue(Integer.parseInt(pref.getMaxPrice()));
        minPrice.setMaxValue(maxPrice.getValue());
        maxPrice.setMinValue(minPrice.getValue());
        minPrice.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                maxPrice.setMinValue(newVal);
                picker.setValue(newVal);
            }
        });
        maxPrice.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                minPrice.setMaxValue(newVal);
                picker.setValue(newVal);
            }
        });

        radiusPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    radiusText.setText("Max Distance: "+seekBar.getProgress()+"km"+"(~"+Math.round(seekBar.getProgress()/1.61)+"mi)");
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ImageButton addKeyword = (ImageButton) findViewById(R.id.keyword_add);
        addKeyword.setOnClickListener(new ImageButton.OnClickListener(){

            public void onClick(View v){
                String word = keywordTextInput.getText().toString();
                if (!word.equals("")){
                    keywords.add(word);
                    adapter.notifyDataSetChanged();
                    keywordTextInput.setText("");
                }
            }
        });
    }

    //Call at end to set preference singleton values.
    private void setOptionValues(){
        PreferencesSingleton pref = PreferencesSingleton.getInstance();
        pref.setRadius(Integer.toString(radiusPicker.getProgress()*1000));
        pref.setMinPrice(Integer.toString(minPrice.getValue()));
        pref.setMaxPrice(Integer.toString(maxPrice.getValue()));
        pref.setKeywords(keywords);
    }

    //Adapter to show text and delete button in Grid
    class CustomAdapter extends ArrayAdapter {
        private ArrayList<String> keywords;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.keywords = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.grid_element, null);
            }
            String item = keywords.get(position);
            if (item != null) {
                TextView main_list_view_text = (TextView) v.findViewById(R.id.grid_text);
                ImageView button = (ImageView) v.findViewById(R.id.grid_button);

                if (main_list_view_text != null) {
                    main_list_view_text.setText(keywords.get(position));
                }
            }

            return v;
        }
    }
}
