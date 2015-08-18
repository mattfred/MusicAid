package com.mattfred.musicaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mattfred.musicaid.wheels.WheelAdapter;

import java.util.ArrayList;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

import static com.mattfred.musicaid.utils.Constants.ABOUT_FRAGMENT;
import static com.mattfred.musicaid.utils.Constants.METRONOME;
import static com.mattfred.musicaid.utils.Constants.PREFERENCES;
import static com.mattfred.musicaid.utils.Constants.SHARPS;
import static com.mattfred.musicaid.utils.Constants.TUNER;

public class Transpose extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private List<TextView> textViews;

    private String[] sharpKeys;
    private String[] flatKeys;
    private int originalKey;
    private int newKey;

    private WheelView originalKeyWheel;

    private Button accidentals;

    private boolean scrolling = false;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transpose);

        preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        accidentals = (Button) findViewById(R.id.bt_accidentals);
        if (!preferences.getBoolean(SHARPS, true)) accidentals.setText(getString(R.string.use_sharps));

        String[] mActivityTitles = getResources().getStringArray(R.array.activity_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mActivityTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        sharpKeys = getResources().getStringArray(R.array.sharp_keys);
        flatKeys = getResources().getStringArray(R.array.flat_keys);

        originalKey = newKey = 0;

        originalKeyWheel = (WheelView) findViewById(R.id.original_key);
        originalKeyWheel.setViewAdapter(new WheelAdapter(getApplicationContext()));
        originalKeyWheel.setCurrentItem(0);
        originalKeyWheel.setCyclic(true);
        originalKeyWheel.addChangingListener(changedListener);
        originalKeyWheel.addScrollingListener(scrollListener);

        WheelView newKeyWheel = (WheelView) findViewById(R.id.new_key);
        newKeyWheel.setViewAdapter(new WheelAdapter(getApplicationContext()));
        newKeyWheel.setCurrentItem(0);
        newKeyWheel.setCyclic(true);
        newKeyWheel.addChangingListener(changedListener);
        newKeyWheel.addScrollingListener(scrollListener);

        setTextFields();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        if (id == R.id.action_about)
        {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(ABOUT_FRAGMENT);
            if (fragment == null)
            {
                fragment = new About();
                fragment.setCancelable(true);
                fragment.show(fm, ABOUT_FRAGMENT);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setMessage(R.string.exit_dialog)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectItem(int position)
    {
        Log.i(TAG, "Clicked: " + position);
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        goToNewActivity(position);
    }

    public void changeAccidentals(@SuppressWarnings("UnusedParameters") View view)
    {
        boolean sharps = preferences.getBoolean(SHARPS, true);
        if (sharps) accidentals.setText(getString(R.string.use_sharps));
        else accidentals.setText(getString(R.string.use_flats));
        preferences.edit().putBoolean(SHARPS, !sharps).apply();
        transpose(newKey - originalKey);

    }

    private final OnWheelScrollListener scrollListener = new OnWheelScrollListener()
    {
        @Override
        public void onScrollingStarted(WheelView wheelView)
        {
            Log.i(TAG, "Scrolling");
            scrolling = true;
        }

        @Override
        public void onScrollingFinished(WheelView wheelView)
        {
            Log.i(TAG, "Done Scrolling");
            scrolling = false;
            changedListener.onChanged(wheelView, 0, wheelView.getCurrentItem());
        }
    };

    private final OnWheelChangedListener changedListener = new OnWheelChangedListener()
    {
        @Override
        public void onChanged(WheelView wheelView, int oldValue, int newValue)
        {
            if (!scrolling)
            {
                if (wheelView.getId() == originalKeyWheel.getId())
                {
                    Log.i(TAG, "Original Changed");
                    originalKey = newValue;
                }
                else
                {
                    Log.i(TAG, "New Changed");
                    newKey = newValue;
                }

                // A -> C (0 -> 4) move +4
                // C -> C (4 -> 4) move 0
                // D -> B (6 -> 3) move -3
                transpose(newKey - originalKey);
            }
        }
    };

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    private void goToNewActivity(int position)
    {
        if (position == METRONOME)
        {
            Intent intent = new Intent(Transpose.this, Metronome.class);
            startActivity(intent);
            finish();
        }

        if (position == TUNER)
        {
            Intent intent = new Intent(Transpose.this, Tuner.class);
            startActivity(intent);
            finish();
        }
    }

    private void setTextFields()
    {
        textViews = new ArrayList<>(12);

        TextView tv_a = (TextView) findViewById(R.id.tv_a);
        textViews.add(tv_a);
        TextView tv_as = (TextView) findViewById(R.id.tv_as);
        textViews.add(tv_as);
        TextView tv_b = (TextView) findViewById(R.id.tv_b);
        textViews.add(tv_b);
        TextView tv_c = (TextView) findViewById(R.id.tv_c);
        textViews.add(tv_c);
        TextView tv_cs = (TextView) findViewById(R.id.tv_cs);
        textViews.add(tv_cs);
        TextView tv_d = (TextView) findViewById(R.id.tv_d);
        textViews.add(tv_d);
        TextView tv_ds = (TextView) findViewById(R.id.tv_ds);
        textViews.add(tv_ds);
        TextView tv_e = (TextView) findViewById(R.id.tv_e);
        textViews.add(tv_e);
        TextView tv_f = (TextView) findViewById(R.id.tv_f);
        textViews.add(tv_f);
        TextView tv_fs = (TextView) findViewById(R.id.tv_fs);
        textViews.add(tv_fs);
        TextView tv_g = (TextView) findViewById(R.id.tv_g);
        textViews.add(tv_g);
        TextView tv_gs = (TextView) findViewById(R.id.tv_gs);
        textViews.add(tv_gs);

        transpose(0);
    }

    private void transpose(int position)
    {
        String middleText = " " + getString(R.string.to) + " ";
        String[] keysToUse = sharpKeys;

        if (!preferences.getBoolean(SHARPS, true)) keysToUse = flatKeys;

        for (int i = 0; i < 12; i++)
        {
            int newKey = i + position;

            if (newKey > 11)
            {
                newKey = newKey - 12;
            }
            else if (newKey < 0)
            {
                newKey = newKey + 12;
            }

            String text = keysToUse[i] + middleText + keysToUse[newKey];
            textViews.get(i).setText(text);
        }
    }
}
