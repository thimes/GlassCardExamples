package com.example.glasscardexamples.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;

import com.example.glasscardexamples.app.views.LevelRenderer;
import com.google.android.glass.timeline.LiveCard;

public class LevelService extends Service {

    private static final String LIVE_CARD_TAG = "level";

    private LiveCard mLiveCard;
    private LevelRenderer mRenderer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            mRenderer = new LevelRenderer(sensorManager, this);

            mLiveCard.setDirectRenderingEnabled(true);
            mLiveCard.getSurfaceHolder().addCallback(mRenderer);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            // Display the options menu when the live card is tapped.

            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
            mLiveCard = null;
        }
        super.onDestroy();
    }

}
