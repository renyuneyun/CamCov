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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TriggerReceiver extends BroadcastReceiver {

    public static final String ACTION_START_OVERLAY = "ryey.camcov.action.START_OVERLAY";
    public static final String ACTION_STOP_OVERLAY = "ryey.camcov.action.STOP_OVERLAY";
    public static final String ACTION_TOGGLE_OVERLAY = "ryey.camcov.action.TOGGLE_OVERLAY";

    public TriggerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TriggerReceiver", "(broadcast received) action:" + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_START_OVERLAY:
                OverlayService.start(context);
                break;
            case ACTION_STOP_OVERLAY:
                OverlayService.stop(context);
                break;
            case ACTION_TOGGLE_OVERLAY:
                OverlayService.toggle(context);
                break;
        }
    }
}
