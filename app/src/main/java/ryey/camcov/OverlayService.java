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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

public class OverlayService extends Service {
    public static final String ACTION_CHANGE_ALPHA = "ryey.camcov.action.CHANGE_ALPHA";

    public static final String EXTRA_ALPHA = "ryey.camcov.extra.ALPHA";

    private View mOverlayView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("OverlayService", "(broadcast received) action:" + intent.getAction());
            switch (intent.getAction()) {
                case ACTION_CHANGE_ALPHA:
                    setViewAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));
                    break;
            }
        }
    };

    private static boolean running = false;

    public static void start(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        float alpha = Float.parseFloat(sharedPreferences.getString(
                context.getString(R.string.key_pref_alpha), String.valueOf(CamOverlay.DEFAULT_ALPHA)));
        start(context, alpha);
    }

    public static void start(Context context, float alpha) {
        Bundle bundle = new Bundle();
        bundle.putFloat(EXTRA_ALPHA, alpha);
        start(context, bundle);
    }

    public static void start(Context context, @NonNull Bundle bundle) {
        Intent intent = new Intent(context, OverlayService.class);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, OverlayService.class);
        context.stopService(intent);
    }

    public static void toggle(Context context) {
        if (running)
            stop(context);
        else
            start(context);
    }

    public static boolean isRunning() {
        return running;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("OverlayService", "onCreate");
        super.onCreate();
        running = true;

        mOverlayView = CamOverlay.show(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_ALPHA);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("OverlayService", "onStartCommand");
        setViewAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("OverlayService", "onDestroy");
        super.onDestroy();
        running = false;
        unregisterReceiver(mReceiver);

        CamOverlay.hide(this);
    }

    protected void setViewAlpha(float alpha) {
        if (mOverlayView != null) {
            mOverlayView.setAlpha(alpha);
        }
    }
}

