package com.mattfred.musicaid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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

import com.mattfred.musicaid.metronome.MetronomePlayer;

import static com.mattfred.musicaid.utils.Constants.ABOUT_FRAGMENT;
import static com.mattfred.musicaid.utils.Constants.TRANSPOSE;
import static com.mattfred.musicaid.utils.Constants.TUNER;

public class Metronome extends AppCompatActivity implements View.OnClickListener
{
    private final String TAG = this.getClass().getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Button btPlus, btMinus;
    private Button btQuarter, btEighth, btTrip, btSix;
    private Button btPlay;
    private boolean playing = false;
    private boolean quarter = true, eighth, trip, six;

    private MetronomeAsyncTask asyncTask;

    private TextView tempo;
    int currentTempo;
    int bpm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metronome);

        asyncTask = new MetronomeAsyncTask();

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

        btPlus = (Button) findViewById(R.id.bt_plus);
        btPlus.setOnClickListener(this);

        btMinus = (Button) findViewById(R.id.bt_minus);
        btMinus.setOnClickListener(this);

        btQuarter = (Button) findViewById(R.id.bt_quarter);
        btQuarter.setOnClickListener(this);

        btEighth = (Button) findViewById(R.id.bt_eighth);
        btEighth.setOnClickListener(this);

        btTrip = (Button) findViewById(R.id.bt_trip);
        btTrip.setOnClickListener(this);

        btSix = (Button) findViewById(R.id.bt_sixteen);
        btSix.setOnClickListener(this);

        btPlay = (Button) findViewById(R.id.bt_start);
        btPlay.setOnClickListener(this);

        tempo = (TextView) findViewById(R.id.tempo);
        currentTempo = 90;
        tempo.setText("" + currentTempo);
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

    private void selectItem(int position)
    {
        Log.i(TAG, "Clicked: " + position);
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);

        goToNewActivity(position);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == btPlus.getId())
        {
            setTempo(1);
        }
        else if (v.getId() == btMinus.getId())
        {
            setTempo(-1);
        }
        else if (v.getId() == btQuarter.getId())
        {
            quarter = !quarter;
            eighth = false;
            trip = false;
            six = false;
            setTempo(0);
        }
        else if (v.getId() == btEighth.getId())
        {
            eighth = !eighth;
            quarter = false;
            trip = false;
            six = false;
            setTempo(0);
        }
        else if (v.getId() == btTrip.getId())
        {
            trip = !trip;
            eighth = false;
            quarter = false;
            six = false;
            setTempo(0);
        }
        else if (v.getId() == btSix.getId())
        {
            six = !six;
            eighth = false;
            trip = false;
            quarter = false;
            setTempo(0);
        }
        else if (v.getId() == btPlay.getId())
        {
            playing = !playing;
            if (playing)
            {
                btPlay.setText(getString(R.string.stop));
                setTempo(0);

                if (playing)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    else
                        asyncTask.execute();
                }
            }
            else
            {
                btPlay.setText(getString(R.string.start));
                asyncTask.stop();
                asyncTask = new MetronomeAsyncTask();
            }
        }
    }


    private void setTempo(int amount)
    {
        currentTempo = currentTempo + amount;
        if (currentTempo < 40)
        {
            currentTempo = 40;
            btMinus.setEnabled(false);
        }
        else btMinus.setEnabled(true);

        if (currentTempo > 210)
        {
            currentTempo = 210;
            btPlus.setEnabled(false);
        }
        else btPlus.setEnabled(true);

        if (currentTempo > 120)
        {
            btSix.setEnabled(false);
            if (six)
            {
                six = false;
                quarter = true;
            }
        }
        else btSix.setEnabled(true);

        if (currentTempo > 160)
        {
            btTrip.setEnabled(false);
            if (trip)
            {
                trip = false;
                quarter = true;
            }
        }
        else btTrip.setEnabled(true);

        if (currentTempo > 180)
        {
            btEighth.setEnabled(false);
            if (eighth)
            {
                eighth = false;
                quarter = true;
            }
        }
        else btEighth.setEnabled(true);

        tempo.setText("" + currentTempo);

        // set bpm to quarter note
        bpm = currentTempo;
        asyncTask.setBeat(1);

        // set bpm to quarter note times two
        if (eighth)
        {
            bpm = currentTempo * 2;
            //asyncTask.setBeat(2);
        }

        // set bpm to quarter note times three
        if (trip)
        {
            bpm = currentTempo * 3;
            //asyncTask.setBeat(3);
        }

        // set bpm to quarter not times four
        if (six)
        {
            bpm = currentTempo * 4;
            //asyncTask.setBeat(2);
        }

        asyncTask.setBpm(bpm);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    private void goToNewActivity(int position)
    {
        if (position == TRANSPOSE)
        {
            asyncTask.stop();
            Intent intent = new Intent(Metronome.this, Transpose.class);
            startActivity(intent);
            finish();
        }

        if (position == TUNER)
        {
            asyncTask.stop();
            Intent intent = new Intent(Metronome.this, Tuner.class);
            startActivity(intent);
            finish();
        }
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
                            asyncTask.stop();
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

    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        MetronomePlayer metronome;

        MetronomeAsyncTask() {
            metronome = new MetronomePlayer();
        }

        protected String doInBackground(Void... params) {
            short noteValue = 4;
            metronome.setNoteValue(noteValue);
            metronome.setBpm(currentTempo);
            double uBeat = 7040;
            metronome.setuBeat(uBeat);
            double dBeat = 7440;
            metronome.setdBeat(dBeat);

            metronome.play();

            return null;
        }

        public void stop() {
            metronome.stop();
            metronome = null;
        }

        public void setBpm(int bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }

        public void setBeat(int beat) {
            if(metronome != null)
                metronome.setBeat(beat);
        }
    }
}
