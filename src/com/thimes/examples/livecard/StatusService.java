package com.thimes.examples.livecard;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

public class StatusService extends Service implements OnInitListener {

    private static final String LIVE_CARD_TAG = "com.thimes.status.tag.StatusCard";

    @SuppressWarnings("unused")
    private static final String TAG           = "StatusService";

    private TimelineManager     mTimelineManager;
    private LiveCard            mLiveCard;
    private BroadcastReceiver   mBatteryInfoReceiver;
    private TextToSpeech        mTTS;
    private boolean             mTTSReady     = false;

    public class StatusBinder extends Binder {
        public StatusService getService() {
            return StatusService.this;
        }
    }

    private final StatusBinder mBinder = new StatusBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
        mTTS = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
        if (mLiveCard.isPublished()) {
            mLiveCard.unpublish();
        }

        //        showBatteryCard();
        showChronometerCard();

        // the pendingintent is necessary, in order for the card to get published
        Intent menuIntent = new Intent(this, MenuActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
        // the pendingintent is necessary, in order for the card to get published

        return START_NOT_STICKY;
    }

    private void showBatteryCard() {
        mBatteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.status);
                CharSequence cs = level + "%";

                views.setTextViewText(R.id.battery_state, cs);
                mLiveCard.setViews(views);

                mLiveCard.publish(PublishMode.REVEAL);
                sayLevel(level);
                unregisterReceiver(mBatteryInfoReceiver);

                Timer t = new Timer();
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        StatusService.this.stopSelf();
                    }
                };

                t.schedule(task, 3000);

            }
        };

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void showChronometerCard() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.clocks);

        long myBirthday = SystemClock.elapsedRealtime();
        boolean started = true;
        views.setChronometer(R.id.remote_chronometer, myBirthday, null, started);

        mLiveCard.setViews(views);

        mLiveCard.publish(PublishMode.REVEAL);
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
        if (mTTSReady) {
            mTTS.speak(level + " percent", TextToSpeech.QUEUE_FLUSH, null);
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
