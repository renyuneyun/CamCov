/*
 * Copyright (c) 2016 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of CamCov.
 *
 * CamCov is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CamCov is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CamCov.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.camcov;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class OverlayService extends Service {
    public static final String ACTION_CHANGE_ALPHA = "ryey.camcov.intent.CHANGE_ALPHA";

    public static final String EXTRA_ALPHA = "ryey.camcov.extra.ALPHA";

    private WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);

    private View mOverlayView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_CHANGE_ALPHA:
                    setViewAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));
                    break;
            }
        }
    };

    public static void start(Context context, @Nullable Bundle bundle) {
        Intent intent = new Intent(context, OverlayService.class);
        if (bundle != null)
            intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void start(Context context, float alpha) {
        Bundle bundle = new Bundle();
        bundle.putFloat(EXTRA_ALPHA, alpha);
        start(context, bundle);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, OverlayService.class);
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mOverlayView == null) {
            mOverlayView = new FrameLayout(this) {
                {
                    View v = new CamOverlay(getContext());
                    addView(v);
                }
            };
        }

        params.alpha = 0.9F;

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.addView(mOverlayView, params);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_ALPHA);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setViewAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(mOverlayView);
    }

    protected void setViewAlpha(float alpha) {
        if (mOverlayView != null) {
            mOverlayView.setAlpha(alpha);
        }
    }
}

