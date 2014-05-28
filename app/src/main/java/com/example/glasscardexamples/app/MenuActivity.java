package com.example.glasscardexamples.app;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.glass.app.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thimes on 5/28/14.
 */
public class MenuActivity extends Activity {

    private static final String LIVE_CARD_TAG_CHRONOMETER = null;

    List<Card> mCards = new ArrayList<Card>();

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.card_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_stop:
                stopService(new Intent(this, ChronoService.class));
                stopService(new Intent(this, LevelService.class));
                stopService(new Intent(this, BatteryService.class));
                return true;

            case R.id.menu_show_level:
                stopService(new Intent(this, ChronoService.class));
                startService(new Intent(this, LevelService.class));
                stopService(new Intent(this, BatteryService.class));
                return true;

            case R.id.menu_show_battery:
                startService(new Intent(this, BatteryService.class));
                stopService(new Intent(this, ChronoService.class));
                stopService(new Intent(this, LevelService.class));
                return true;

            case R.id.menu_show_chrono:
                startService(new Intent(this, ChronoService.class));
                stopService(new Intent(this, LevelService.class));
                stopService(new Intent(this, BatteryService.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the activity, but we MUST call finish so the activity goes away.
        finish();
    }

}
