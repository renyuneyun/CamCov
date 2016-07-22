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
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class OverlayService extends Service {
    public static final String EXTRA_ALPHA = "ryey.camcov.extra.ALPHA";

    private static float DEFAULT_ALPHA = 0.3F;

    private WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);

    private View mOverlayView;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        params.alpha = intent.getFloatExtra(EXTRA_ALPHA, DEFAULT_ALPHA);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.addView(mOverlayView, params);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(mOverlayView);
    }
}

