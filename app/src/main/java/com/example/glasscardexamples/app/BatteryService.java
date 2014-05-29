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
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryService extends Service implements OnInitListener {

    private static final String BATTERY_LIVE_CARD_TAG = "com.thimes.status.tag.BatteryCard";

    @SuppressWarnings("unused")
    private static final String TAG = "StatusService";

    private BroadcastReceiver mBatteryInfoReceiver;

    private TextToSpeech mTTS;

    private boolean mTTSReady = false;

    public class BatteryBinder extends Binder {
        public BatteryService getService() {
            return BatteryService.this;
        }
    }

    private final BatteryBinder mBinder = new BatteryBinder();

    private LiveCard mLiveCard;

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
        showBatteryCard();

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
                mLiveCard = new LiveCard(BatteryService.this, BATTERY_LIVE_CARD_TAG);
                mLiveCard.setViews(views);

                sayLevel(level);

                // Display the options menu when the live card is tapped.
                Intent menuIntent = new Intent(this, MenuActivity.class);
                menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
                // Display the options menu when the live card is tapped.

                mLiveCard.publish(LiveCard.PublishMode.REVEAL);
                unregisterReceiver(mBatteryInfoReceiver);

                Timer t = new Timer();
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        BatteryService.this.stopSelf();
                    }
                };

                t.schedule(task, 6000);

            }
        };

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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
            Log.e("TTS", "Initialization Failed!");
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

        if (mLiveCard.isPublished()) {
            mLiveCard.unpublish();
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
