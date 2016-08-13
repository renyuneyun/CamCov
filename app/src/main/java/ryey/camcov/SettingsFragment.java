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


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

    SharedPreferences.OnSharedPreferenceChangeListener listener = null;

    SharedPreferences defaultSharedPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultSharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        } else {
            defaultSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        Log.d("defaultpPref", defaultSharedPreference.getAll().toString());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (SharedPreferences.OnSharedPreferenceChangeListener) activity;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SharedPreferences.OnSharedPreferenceChangeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        Log.d(getClass().getSimpleName(), "onResume");
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);

        CheckBoxPreference preference_enabled = (CheckBoxPreference) this.findPreference(getString(R.string.key_pref_enabled));
        preference_enabled.setChecked(OverlayService.isRunning());
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

}

