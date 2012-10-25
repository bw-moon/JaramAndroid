package com.jaram.app.jaram;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ThreadActivity extends Activity {
	private static String view_url = "http://jaram.org/board/view.php?tableID=[board_id]&id=[thread_id]&type=json";
	private static final String DEBUG_TAG = "ThreadActivity";
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Intent intent = getIntent();
		String board_id = intent.getStringExtra(MainActivity.EXTRA_BOARD_ID);
		String thread_id = intent.getStringExtra(MainActivity.EXTRA_THREAD_ID);

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String url = view_url.replace("[board_id]", board_id).replace(
					"[thread_id]", thread_id);
			Log.d(DEBUG_TAG, url);
			setProgressBarIndeterminateVisibility(true);
			new RetriveJsonTask().execute(url);

		} else {
			Toast.makeText(this, "No network connection available.",
					Toast.LENGTH_LONG).show();
		}
	}

	private void viewThread(JSONObject data) {
		// Create the text view
		try {
			setContentView(R.layout.activity_thread);
			TextView info_tv = (TextView) findViewById(R.id.threadInfo);
			info_tv.setText(Html.fromHtml(data.getJSONObject("thread")
					.getString("user_name")
					+ " / "
					+ data.getJSONObject("thread").getString("date")
					+ " / "
					+ data.getJSONObject("thread").getString("count")
					+ " <strong>hits</strong>"));

			TextView content_tv = (TextView) findViewById(R.id.threadBody);
			content_tv.setText(Html.fromHtml(data.getJSONObject("thread")
					.getString("note").replaceAll("\n", "<br/>")));

			setTitle(data.getJSONObject("thread").getString("title"));

			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.comments);
			JSONArray comments = data.getJSONArray("comments");

			for (int i = 0; i < comments.length(); i++) {
				JSONObject comment = comments.getJSONObject(i);
				String user_name = comment.getString("user_name");
				String date = comment.getString("date");
				TextView tv = new TextView(this);
				tv.setText(user_name + " / " + date);
				tv.setId(comment.getInt("id"));
				tv.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				tv.setBackgroundColor(Color.GRAY);
				linearLayout.addView(tv);

				String note = comment.getString("note");

				TextView tv_note = new TextView(this);
				tv_note.setText(Html.fromHtml(note.replaceAll("\n", "<br/>")));
				LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				llp.setMargins(10, 0, 0, 20);
				tv_note.setLayoutParams(llp);
				linearLayout.addView(tv_note);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set the text view as the activity layout
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_thread, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void handlePermissionError(String status) {
		Toast.makeText(this, status, Toast.LENGTH_LONG).show();
	}

	private class RetriveJsonTask extends AsyncTask<String, Void, String> {
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
			// try parse the string to a JSON object
			try {
				setProgressBarIndeterminateVisibility(false);
				String status = new JSONObject(result).getString("status");
				Log.d("RetriveJsonTask", status);
				if (status.equals(MainActivity.JSON_SUCCESS_TAG)) {
					viewThread(new JSONObject(result));
				} else {
					handlePermissionError(status);
				}
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}

		}
	}
}
