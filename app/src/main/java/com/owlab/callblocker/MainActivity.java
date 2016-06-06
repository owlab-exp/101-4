package com.owlab.callblocker;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().add(R.id.fragment_container, new PhoneListFragment()).commit();
    }

    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .addToBackStack("PhoneListFragment")
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
                return true;
            case android.R.id.home:
                getFragmentManager().popBackStack("PhoneListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PhoneListFragment())
                        .commit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String changeActionBar(String title) {
        String oldTitle = getSupportActionBar().getTitle().toString();
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        menu.findItem(R.id.action_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.INVISIBLE);
        menu.findItem(R.id.action_settings).setVisible(false);
        return oldTitle;
    }

    public void restoreActionBar() {
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        menu.findItem(R.id.action_main_onoff_switch_layout).getActionView().findViewById(R.id.action_main_onoff_switch).setVisibility(View.VISIBLE);
        menu.findItem(R.id.action_settings).setVisible(true);
    }
}
