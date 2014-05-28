package com.example.glasscardexamples.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

public class ChronoService extends Service {

    private static final String CHRONOMETER_LIVE_CARD_TAG = "com.thimes.status.tag.ChronometerCard";

    // the reference to the live card we've created
    private LiveCard mLiveCard;

    public class ChronoBinder extends Binder {
        public ChronoService getService() {
            return ChronoService.this;
        }
    }

    private final ChronoBinder mBinder = new ChronoBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showChronometerCard();

        return START_STICKY;
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

        mLiveCard = liveCard;

        liveCard.publish(LiveCard.PublishMode.REVEAL);
    }

    @Override
    public void onDestroy() {

        if (mLiveCard.isPublished()) {
            mLiveCard.unpublish();
        }

        super.onDestroy();
    }

}
