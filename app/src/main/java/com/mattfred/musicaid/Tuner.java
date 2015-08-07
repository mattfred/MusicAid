package com.mattfred.musicaid;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import com.mattfred.musicaid.tuner.PitchView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

import static com.mattfred.musicaid.utils.Constants.ABOUT_FRAGMENT;
import static com.mattfred.musicaid.utils.Constants.METRONOME;
import static com.mattfred.musicaid.utils.Constants.TRANSPOSE;

public class Tuner extends AppCompatActivity implements View.OnClickListener
{
    private final String TAG = this.getClass().getSimpleName();

    private PdUiDispatcher dispatcher;

    private Button eButton;
    private Button aButton;
    private Button dButton;
    private Button gButton;
    private Button bButton;
    private Button e2Button;
    private TextView pitchLabel;
    private PitchView pitchView;

    private PdService pdService = null;

    private String[] mActivityTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private final ServiceConnection pdConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            pdService = ((PdService.PdBinder)service).getService();
            try
            {
                initPd();
                loadPatch();
            }
            catch (IOException e)
            {
                Log.e(TAG, e.toString());
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuner);

        mActivityTitles = getResources().getStringArray(R.array.activity_titles);
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

        initGui();
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    private void initGui()
    {
        setContentView(R.layout.tuner);
        eButton = (Button) findViewById(R.id.e_button);
        eButton.setOnClickListener(this);
        aButton = (Button) findViewById(R.id.a_button);
        aButton.setOnClickListener(this);
        dButton = (Button) findViewById(R.id.d_button);
        dButton.setOnClickListener(this);
        gButton = (Button) findViewById(R.id.g_button);
        gButton.setOnClickListener(this);
        bButton = (Button) findViewById(R.id.b_button);
        bButton.setOnClickListener(this);
        e2Button = (Button) findViewById(R.id.e2_button);
        e2Button.setOnClickListener(this);

        pitchLabel = (TextView) findViewById(R.id.pitch_label);

        pitchView = (PitchView) findViewById(R.id.pitch_view);
        pitchView.setCenterPitch(45);
        pitchLabel.setText("A-String");
    }

    private void triggerNote(int n) {
        PdBase.sendFloat("midinote", n);
        PdBase.sendBang("trigger");
        pitchView.setCenterPitch(n);
    }

    private void initPd() throws IOException {
        //Configure the audio glue
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate,  1 , 2, 10.0f);
        pdService.startAudio();

        //Create and install the dispatcher
        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
        dispatcher.addListener("pitch", new PdListener.Adapter()
        {
            @Override
            public void receiveFloat(String source, float x)
            {
                pitchView.setCurrentPitch(x);
            }
        });
    }

    private void loadPatch() throws IOException {
        File dir = getFilesDir();
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.bitcrush), dir, true);
        File patchFile = new File(dir, "bitcrush.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    private void start() {
        if (!pdService.isRunning()) {
            Intent intent = new Intent(Tuner.this, Tuner.class);
            pdService.startAudio(intent, R.drawable.icon, "GuitarTuner", "Return to Guitar Tuner.");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(pdConnection);
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

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.e_button:
                triggerNote(40);
                pitchLabel.setText("E-String");
                break;
            case R.id.a_button:
                triggerNote(45);
                pitchLabel.setText("A-String");
                break;
            case R.id.d_button:
                triggerNote(50);
                pitchLabel.setText("D-String");
                break;
            case R.id.g_button:
                triggerNote(55);
                pitchLabel.setText("G-String");
                break;
            case R.id.b_button:
                triggerNote(59);
                pitchLabel.setText("B-String");
                break;
            case R.id.e2_button:
                triggerNote(60);
                pitchLabel.setText("E-String");
                break;
        }
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
        if (position == METRONOME)
        {
            Intent intent = new Intent(Tuner.this, Metronome.class);
            startActivity(intent);
            finish();
        }

        if (position == TRANSPOSE)
        {
            Intent intent = new Intent(Tuner.this, Transpose.class);
            startActivity(intent);
            finish();
        }
    }
}
