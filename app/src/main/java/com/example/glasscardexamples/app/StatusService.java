package com.example.glasscardexamples.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class StatusService extends Service implements OnInitListener {

    private static final String CHRONOMETER_LIVE_CARD_TAG = "com.thimes.status.tag.StatusCard";

    private static final String BATTERY_LIVE_CARD_TAG = "com.thimes.status.tag.StatusCard";

    @SuppressWarnings("unused")
    private static final String TAG = "StatusService";

    private BroadcastReceiver mBatteryInfoReceiver;

    private TextToSpeech mTTS;

    private boolean mTTSReady = false;

    public class StatusBinder extends Binder {
        public StatusService getService() {
            return StatusService.this;
        }
    }

    private final StatusBinder mBinder = new StatusBinder();

    private final List<LiveCard> cards = new ArrayList<LiveCard>();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTTS = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer t = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                showBatteryCard();
            }
        };

        t.schedule(task, 3000);

        showChronometerCard();

        return START_STICKY;
    }

    private void showBatteryCard() {
        mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.status);
                CharSequence cs = level + "%";

                views.setTextViewText(R.id.battery_state, cs);
                LiveCard liveCard = new LiveCard(StatusService.this, CHRONOMETER_LIVE_CARD_TAG);
                liveCard.setViews(views);

                cards.add(liveCard);

                sayLevel(level);

                liveCard.publish(LiveCard.PublishMode.REVEAL);
                unregisterReceiver(mBatteryInfoReceiver);

                Timer t = new Timer();
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        StatusService.this.stopSelf();
                    }
                };

                t.schedule(task, 6000);

            }
        };

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void showChronometerCard() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.clocks);

        long myBirthday = SystemClock.elapsedRealtime();
        boolean started = true;
        views.setChronometer(R.id.remote_chronometer, myBirthday, null, started);

        LiveCard liveCard = new LiveCard(this, CHRONOMETER_LIVE_CARD_TAG);

        liveCard.setViews(views);

        // the pendingintent is necessary, in order for the card to get published
        Intent menuIntent = new Intent(this, MenuActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        // the pendingintent is necessary, in order for the card to get published

        cards.add(liveCard);

        liveCard.publish(LiveCard.PublishMode.REVEAL);
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = mTTS.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                mTTSReady = true;
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void sayLevel(int level) {
        sayString(level + " percent");
    }

    private void sayString(String string) {
        if (mTTSReady) {
            mTTS.speak(string, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {

        for (LiveCard card : cards) {
            if (card.isPublished()) {
                card.unpublish();
            }
        }

        if (mTTS != null) {
            if (mTTS.isSpeaking()) {
                mTTS.stop();
            }

            mTTS.shutdown();
        }

        super.onDestroy();
    }

}
