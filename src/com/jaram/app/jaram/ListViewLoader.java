package com.jaram.app.jaram;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListViewLoader extends ListActivity implements
		ListView.OnScrollListener {
	private static String bbs_url = "http://www.jaram.org/board/bbs.php?tableID=[board_id]&type=json";
	private static final String TAG_THREAD = "threads";
	static JSONObject jObj = null;
	static String json = "";
	String board_id = "";
	private static final String DEBUG_TAG = "ListView";
	ProgressDialog progressDialog;
	private int currentFirstVisibleItem;
	private int currentVisibleItemCount;
	private int currentScrollState;
	private int loadedFirstVisibleItem;
	ArrayAdapter<String> adapter;
	ArrayList<String> itemArray;
	ArrayList<JSONObject> threadArray;
	boolean loading_flag = false;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setUpView();

		Intent intent = getIntent();
		board_id = intent.getStringExtra(MainActivity.EXTRA_BOARD_NAME);
		setTitle(board_id);

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			setProgressBarIndeterminateVisibility(true);
			String url = this.getListUrl(board_id);
			Log.d(DEBUG_TAG, url);
			new RetriveJsonTask().execute(url);

		} else {
			Toast.makeText(this, getString(R.string.msg_network_not_enabled),
					Toast.LENGTH_LONG).show();
		}

		getListView().setOnScrollListener(this);
	}

	private void setUpView() {
		itemArray = new ArrayList<String>();
		itemArray.clear();

		threadArray = new ArrayList<JSONObject>();
		threadArray.clear();

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, itemArray);
		setListAdapter(adapter);
	}

	private String getListUrl(String menu) {
		String board_id = "";

		if (menu.equals("Diary")) {
			board_id = "diary";
		} else if (menu.equals("Graduated")) {
			board_id = "graduated";
		} else if (menu.equals("English Article")) {
			board_id = "english";
		} else if (menu.equals("Copy & Paste")) {
			board_id = "fungul";
		} else if (menu.equals("Class Info")) {
			board_id = "classinfo";
		} else if (menu.equals("Black Book")) {
			board_id = "blackbook";
		} else if (menu.equals("JaramOB")) {
			board_id = "jaramob";
		}

		return bbs_url.replace("[board_id]", board_id);
	}

	private String getNextUrl(String menu) {
		int next_page = 1;
		try {
			next_page = jObj.getInt("nowPage") + 1;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getListUrl(menu) + "&startPage=" + next_page;
	}

	public void showList() {
		loadedFirstVisibleItem = currentFirstVisibleItem;
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(DEBUG_TAG, "click position : " + position);
		try {
			Intent intent = new Intent(this, ThreadActivity.class);
			intent.putExtra(MainActivity.EXTRA_BOARD_ID,
					"" + jObj.get("tableID"));
			intent.putExtra(MainActivity.EXTRA_THREAD_ID,
					"" + threadArray.get(position).get("id"));
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

				jObj = new JSONObject(result);

				// Getting Array of Contacts
				JSONArray threads = null;
				try {
					String status = jObj.getString("status");
					Log.d("RetriveJsonTask", status);

					if (status.equals(MainActivity.JSON_SUCCESS_TAG)) {
						threads = jObj.getJSONArray(TAG_THREAD);
						// looping through All Contacts
						for (int i = 0; i < threads.length(); i++) {
							JSONObject t = threads.getJSONObject(i);
							String comment_count = t.getString("subNum");
							if (t.getString("subNum").equals("false")) {
								comment_count = "0";
							}
							itemArray.add(t.getString("title") + " ["
									+ comment_count + "]");
							threadArray.add(t);
						}
						showList();
					} else {
						handlePermissionError(status);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			} finally {
				loading_flag = false;
			}

		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.currentFirstVisibleItem = firstVisibleItem;
		this.currentVisibleItemCount = visibleItemCount;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.currentScrollState = scrollState;
		this.isScrollCompleted();
	}

	private void isScrollCompleted() {
		// reach to end
		if (loadedFirstVisibleItem < currentFirstVisibleItem
				&& (currentFirstVisibleItem + this.currentVisibleItemCount) % 20 == 0
				&& currentFirstVisibleItem > 0 && currentVisibleItemCount == 12
				&& this.currentScrollState == SCROLL_STATE_IDLE
				&& loading_flag == false) {
			Log.d(DEBUG_TAG, "reach to end " + currentFirstVisibleItem + " "
					+ currentVisibleItemCount);
			loading_flag = true;
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				setProgressBarIndeterminateVisibility(true);
				String url = this.getNextUrl(board_id);
				Log.d(DEBUG_TAG, url);
				new RetriveJsonTask().execute(url);
			} else {
				Toast.makeText(this,
						getString(R.string.msg_network_not_enabled),
						Toast.LENGTH_LONG).show();
				loading_flag = false;
			}
		}
		// reach to top (do refresh)
		else if (currentFirstVisibleItem == 0
				&& this.currentVisibleItemCount > 0
				&& this.currentScrollState == SCROLL_STATE_IDLE) {
			// ConnectivityManager connMgr = (ConnectivityManager)
			// getSystemService(Context.CONNECTIVITY_SERVICE);
			// NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			// if (networkInfo != null && networkInfo.isConnected()) {
			// setProgressBarIndeterminateVisibility(true);
			// String url = this.getListUrl(board_id);
			// Log.d(DEBUG_TAG, url);
			// new RetriveJsonTask().execute(url);
			// } else {
			// Toast.makeText(this,
			// getString(R.string.msg_network_not_enabled),
			// Toast.LENGTH_LONG).show();
			// }
		}
	}
}