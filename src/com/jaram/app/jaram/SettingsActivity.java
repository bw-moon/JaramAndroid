package com.jaram.app.jaram;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	final static String KEY_PREF_ID = "pref_key_account_id";
	final static String KEY_PREF_PASS = "pref_key_account_pass";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String account_id = sharedPref.getString(SettingsActivity.KEY_PREF_ID,
				"");
		String account_pass = sharedPref.getString(
				SettingsActivity.KEY_PREF_PASS, "");

		Preference id_pref = findPreference(KEY_PREF_ID);
		// Set summary to be the user-description for the selected value
		if (account_id.length() > 0) {
			id_pref.setSummary(account_id);
		} else {
			id_pref.setSummary(getString(R.string.msg_id_not_inserted));
		}

		Preference pass_pref = findPreference(KEY_PREF_PASS);
		// Set summary to be the user-description for the selected value
		if (account_pass.length() > 0) {
			pass_pref.setSummary(CommonUtil.repeat("*", account_pass.length()));
		} else {
			pass_pref.setSummary(getString(R.string.msg_password_not_inserted));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("SettingsActivity", key + " changed");
		if (key.equals(KEY_PREF_ID)) {
			Preference id_pref = findPreference(key);
			// Set summary to be the user-description for the selected value
			id_pref.setSummary(sharedPreferences.getString(key, ""));
		}
		else if (key.equals(KEY_PREF_PASS)){
			Preference pass_pref = findPreference(key);
			pass_pref.setSummary(CommonUtil.repeat("*", sharedPreferences.getString(key, "").length()));
		}
	}
}
