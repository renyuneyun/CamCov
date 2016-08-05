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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class SettingsActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(getString(R.string.key_pref_enabled), OverlayService.isRunning())
                .apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_pref_enabled))) {
            if (sharedPreferences.getBoolean(key, false)) {
                if (overlayPermissionIsFine()) {
                    OverlayService.start(this, CamOverlay.DEFAULT_ALPHA);
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_MANAGE_OVERLAY_PERMISSION);
                    }
                }
            } else {
                OverlayService.stop(this);
            }
        } else if (key.equals(getString(R.string.key_pref_alpha))) {
            float alpha = Float.parseFloat(sharedPreferences.getString(key, String.valueOf(CamOverlay.DEFAULT_ALPHA)));
            if (alpha > 1) {
                alpha = 1;
                sharedPreferences.edit().putFloat(key, alpha).apply();
            } else {
                Intent intent = new Intent(OverlayService.ACTION_CHANGE_ALPHA);
                intent.putExtra(OverlayService.EXTRA_ALPHA, alpha);
                sendBroadcast(intent);
            }
        }
    }

    boolean overlayPermissionIsFine() {
        if (Build.VERSION.SDK_INT >= 23)
            return Settings.canDrawOverlays(this);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MANAGE_OVERLAY_PERMISSION) {
            if (overlayPermissionIsFine()) {
                OverlayService.start(this, CamOverlay.DEFAULT_ALPHA);
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(getString(R.string.key_pref_enabled), false)
                        .apply();
            }
        }
    }
}
