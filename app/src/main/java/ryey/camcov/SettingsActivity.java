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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

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
                OverlayService.start(this, CamOverlay.DEFAULT_ALPHA);
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
}
