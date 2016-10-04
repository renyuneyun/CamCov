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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class SettingsActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 1;
    private static final int REQUEST_CODE_MANAGE_CAMERA_PERMISSION = 2;

    private static final String TAG = SettingsActivity.class.getSimpleName();

    RequirePermissionThread thread = null;
    boolean has_camera_permission;
    boolean has_overlay_permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_pref_enabled))) {
            if (sharedPreferences.getBoolean(key, false)) {
                tryStartOverlay();
            } else {
                OverlayService.stop(this);
            }
        } else if (key.equals(getString(R.string.key_pref_alpha))) {
            float alpha = Float.parseFloat(sharedPreferences.getString(key, String.valueOf(CamOverlay.DEFAULT_ALPHA)));
            if (alpha > 1) {
                alpha = 1;
                sharedPreferences.edit().putString(key, String.valueOf(alpha)).apply();
            } else {
                Intent intent = new Intent(OverlayService.ACTION_CHANGE_ALPHA);
                intent.putExtra(OverlayService.EXTRA_ALPHA, alpha);
                sendBroadcast(intent);
            }
        }
    }

    synchronized void tryStartOverlay() {
        Log.d(TAG, "tryStartOverlay()");
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d(TAG, " SDK >= 23");
            if (thread == null) {
                thread = new RequirePermissionThread();
                thread.start();
            }
        } else {
            Log.d(TAG, " SDK < 23");
            OverlayService.start(this, CamOverlay.DEFAULT_ALPHA);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean cameraPermissionIsFine() {
        int result = checkSelfPermission(Manifest.permission.CAMERA);
        boolean status =result == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "camera permission is " + String.valueOf(status));
        return status;
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean overlayPermissionIsFine() {
        boolean status = Settings.canDrawOverlays(this);
        Log.d(TAG, "overlay permission is " + String.valueOf(status));
        return status;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_CODE_MANAGE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT < 23) {
                Log.wtf(TAG, "SDK version < 23 used REQUEST_CODE_MANAGE_OVERLAY_PERMISSION");
            }
            has_overlay_permission = overlayPermissionIsFine();
            Log.v(TAG, " (onActivityResult) notifying");
            synchronized (thread) {
                thread.notify();
            }
            Log.v(TAG, " (onActivityResult) notified");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_CODE_MANAGE_CAMERA_PERMISSION) {
            if (Build.VERSION.SDK_INT < 23) {
                Log.wtf(TAG, "SDK version < 23 used REQUEST_CODE_MANAGE_CAMERA_PERMISSION");
            }
            has_camera_permission = cameraPermissionIsFine();
            Log.v(TAG, " (onRequestPermissionsResult) notifying");
            synchronized (thread) {
                thread.notify();
            }
            Log.v(TAG, " (onRequestPermissionsResult) notified");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    class RequirePermissionThread extends Thread {
        private final String TAG = RequirePermissionThread.class.getSimpleName();
        @Override
        synchronized public void run() {
            Log.d(TAG, "Thread running");
            has_overlay_permission = false;
            has_camera_permission = false;
            if (requireCameraPermission()) {
                Log.v(TAG, "requiring CAMERA permission, waiting");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "checking CAMERA permission");
            if (has_camera_permission) {
                if (requireOverlayPermission()) {
                    Log.v(TAG, "requiring OVERLAY permission, waiting");
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "checking OVERLAY permission");
                if (has_overlay_permission) {
                    OverlayService.start(SettingsActivity.this, CamOverlay.DEFAULT_ALPHA);
                    thread = null;
                    return;
                }
            }
            Log.d(TAG, "permission NOT correct");
            thread = null;
        }

        boolean requireCameraPermission() {
            if (cameraPermissionIsFine()) {
                has_camera_permission = true;
            } else {
                Log.d(TAG, " requiring CAMERA permission");
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_MANAGE_CAMERA_PERMISSION);
            }
            return !has_camera_permission;
        }

        boolean requireOverlayPermission() {
            if (overlayPermissionIsFine()) {
                has_overlay_permission = true;
            } else {
                Log.d(TAG, " requiring OVERLAY permission");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_MANAGE_OVERLAY_PERMISSION);
            }
            return !has_overlay_permission;
        }
    }
}
