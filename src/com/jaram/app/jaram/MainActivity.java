package com.jaram.app.jaram;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements
		OnSharedPreferenceChangeListener {
	public final static String EXTRA_BOARD_NAME = "com.jaram.app.jaram.BOARD_NAME";
	public final static String EXTRA_BOARD_ID = "com.jaram.app.jaram.BOARD_ID";
	public final static String EXTRA_THREAD_ID = "com.jaram.app.jaram.THREAD_ID";
	public final static String JSON_SUCCESS_TAG = "success";
	public final static String JSON_ERROR_TAG = "error";
	public final static String JSON_PERMISSION_DENIED_TAG = "permission denied";
	private final static String DEBUG_TAG = "MainActivity";
	static JSONObject jObj = null;
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		String[] values = new String[] { "Diary", "Graduated",
				"English Article", "Copy & Paste", "Class Info", "Black Book",
				"JaramOB" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		String account_id = sharedPref.getString(SettingsActivity.KEY_PREF_ID,
				"");
		String account_pass = sharedPref.getString(
				SettingsActivity.KEY_PREF_PASS, "");

		progressDialog = ProgressDialog.show(MainActivity.this,
				getString(R.string.title_login_dialog),
				getString(R.string.msg_login));

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			setProgressBarIndeterminateVisibility(true);
			this.doLoginProcess(account_id, account_pass);
		} else {
			Toast.makeText(this, getString(R.string.msg_network_not_enabled),
					Toast.LENGTH_LONG).show();
		}
	}

	private void doLoginProcess(String id, String pass) {
		String login_url = "http://www.jaram.org/member/login_check.php?member_id="
				+ id + "&password=" + pass + "&type=json";
		new LoginTask().execute(login_url);
	}

	private void handleLoginResult(String status) {
		Toast.makeText(this, "login " + status, Toast.LENGTH_LONG).show();
	}

	private class LoginTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			try {
				return CommonUtil.downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			setProgressBarIndeterminateVisibility(false);
			progressDialog.dismiss();
			// try parse the string to a JSON object
			try {
				// Log.d(DEBUG_TAG, result);
				String status = new JSONObject(result).getString("status");
				Log.d("LoginTask", status);
				handleLoginResult(status);
				// Getting Array of Contacts
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ListViewLoader.class);
		intent.putExtra(EXTRA_BOARD_NAME, item);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(DEBUG_TAG, key + " changed");
		if (key.equals(SettingsActivity.KEY_PREF_ID)
				|| key.equals(SettingsActivity.KEY_PREF_PASS)) {
			String account_id = sharedPreferences.getString(
					SettingsActivity.KEY_PREF_ID, "");
			String account_pass = sharedPreferences.getString(
					SettingsActivity.KEY_PREF_PASS, "");

			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				setProgressBarIndeterminateVisibility(true);
				this.doLoginProcess(account_id, account_pass);
			} else {
				Toast.makeText(this,
						getString(R.string.msg_network_not_enabled),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
