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
                stopService(new Intent(this, StatusService.class));
                return true;

            /* none of this works post XE16, so don't!
            case R.id.menu_new_static_card:
                Card staticCard = new Card(this);
                staticCard.setText(R.string.static_card_title);
                staticCard.setFootnote(R.string.static_card_footnote);
                staticCard.setImageLayout(Card.ImageLayout.FULL);
                staticCard.addImage(R.drawable.catbreading);

                final long staticId = tm.insert(staticCard);
            {
                Timer t = new Timer();
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        tm.delete(staticId);
                    }
                };

                t.schedule(task, 15000);
            }
            */

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the activity.
        finish();
    }

}
