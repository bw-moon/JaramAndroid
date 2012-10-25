package com.jaram.app.jaram;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditorActivity extends Activity {
	private final static String DEBUG_TAG = "EditorActivity";
	private final static String write_url = "http://www.jaram.org/board/upload_result.php?tableID=[board_id]&type=json&fileDataOut=&replayID=&modID=&mtime=[mtime]&title=[title]&note=[note]";
	public final static String ACTION_WRITE_THREAD_SUCCESS = "com.jaram.app.jaram.WRITE_THREAD_SUCCESS";
	private String board_id;
	private int mtime;
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		board_id = intent.getStringExtra(MainActivity.EXTRA_BOARD_ID);
		mtime = (int) (System.currentTimeMillis() / 1000L);
		setContentView(R.layout.activity_editor);
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

	public void writeThread(View view) {
		progressDialog = ProgressDialog.show(EditorActivity.this,
				getString(R.string.title_write_dialog),
				getString(R.string.msg_write));
		String title = ((EditText) findViewById(R.id.fieldTitle)).getText()
				.toString();
		String content = ((EditText) findViewById(R.id.fieldContent)).getText()
				.toString();

		Log.d(DEBUG_TAG, "title :" + title);

		String request_url;
		try {
			request_url = write_url
					.replace("[board_id]", board_id)
					.replace("[mtime]", "" + mtime)
					.replace("[title]", URLEncoder.encode(title, "utf-8"))
					.replace("[note]",
							Base64.encodeToString(content.getBytes(), 0));
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				Log.d(DEBUG_TAG, request_url);
				setProgressBarIndeterminateVisibility(true);
				new WriteJsonTask().execute(request_url);
			} else {
				Toast.makeText(this, "No network connection available.",
						Toast.LENGTH_LONG).show();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onWriteDone() {
		Intent i = new Intent(ACTION_WRITE_THREAD_SUCCESS);
		sendBroadcast(i);
		finish();
	}

	private class WriteJsonTask extends AsyncTask<String, Void, String> {
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
				progressDialog.dismiss();
				JSONObject jObj = new JSONObject(result);
				try {
					String status = jObj.getString("status");
					Log.d("RetriveJsonTask", status);

					if (status.equals(MainActivity.JSON_SUCCESS_TAG)) {
						handlePermissionError(status);
						onWriteDone();
					} else {
						handlePermissionError(status);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
				Log.i("JSON Parser", result);
			}
		}
	}

	private void handlePermissionError(String status) {
		Toast.makeText(this, status, Toast.LENGTH_LONG).show();
	}
}
