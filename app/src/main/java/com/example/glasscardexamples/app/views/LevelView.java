package com.example.glasscardexamples.app.views;

/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View used to draw the level line.
 */
public class LevelView extends View {

    private Paint mPaint = new Paint();
    private float mRollAngle = 0.f;
    private float mPitchAngle = 0.f;
    private int mUpColor =   Color.parseColor("#1100ff00");
    private int mDownColor = Color.parseColor("#11ff0000");

    public LevelView(Context context) {
        this(context, null, 0);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        mPaint.setColor(Color.BLUE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(5);
    }

    /**
     * Set the angle of the level line.
     *
     * @param angle Angle of the level line.
     */
    public void setRollAngle(float angle) {
        mRollAngle = angle;
        // Redraw the line.
        invalidate();
    }

    public void setPitchAngle(float angle) {
        mPitchAngle = angle;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight() / 2;

        if (mPitchAngle > 0) {
            canvas.drawColor(mUpColor);
        } else {
            canvas.drawColor(mDownColor);
        }

        // Compute the coordinates.
        float y = (float) Math.tan(mRollAngle) * width / 2;

        // Draw the level line.
        canvas.drawLine(0, y + height, width, -y + height, mPaint);
    }

}