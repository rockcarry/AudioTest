package com.fanfan.audiotest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.util.AttributeSet;

public class WaveformView extends View {
    short[] mAudioData;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        if (mAudioData == null) {
            return;
        }

        Path  path  = new Path ();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int n = mAudioData.length; // mAudioData.length; // 512;

        paint.setColor(Color.rgb(0, 0, 0));
        path.moveTo(0, h / 2);
        path.lineTo(w, h / 2);
        canvas.drawPath(path, paint);

        int delta = n / w > 1 ? n / w : 1;
        int px, py, i;
        px = 0;
        py = (int)(h * (mAudioData[0] + 0x7fff) / 0x10000);
        paint.setColor(Color.rgb(0, 255, 0));
        path.moveTo(px, py);
        for (i=delta; i<n; i+=delta) {
            px = w * i / n;
            py = (int)(h * (mAudioData[i] + 0x7fff) / 0x10000);
            path.lineTo(px, py);
        }
        canvas.drawPath(path, paint);
    }

    public void setAudioData(short[] data) {
        mAudioData = data;
    }
}
