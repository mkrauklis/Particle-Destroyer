package com.michaelkrauklis.android.lastdefender.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.michaelkrauklis.android.lastdefender.LastDefender;
import com.michaelkrauklis.android.lastdefender.R;

public class Preferences extends PreferenceActivity {
	public static final String VIBRATE_ON = "vibrateOn";
	public static final String DIFFICULTY = "difficulty";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// Get the custom preference
		CheckBoxPreference vibrateOn = (CheckBoxPreference) findPreference("vibrateOn");
		vibrateOn.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences customSharedPreference = getSharedPreferences(
						LastDefender.class.getName(), Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				editor.putBoolean(VIBRATE_ON, !customSharedPreference
						.getBoolean(VIBRATE_ON, false));
				editor.commit();
				return true;
			}

		});
		ListPreference difficulty = (ListPreference) findPreference("difficulty");
		difficulty
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences customSharedPreference = getSharedPreferences(
								LastDefender.class.getName(),
								Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = customSharedPreference
								.edit();
						editor.putString(DIFFICULTY, (String) newValue);
						editor.commit();
						return true;
					}
				});
	}
}
