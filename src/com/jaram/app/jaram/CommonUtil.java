package com.jaram.app.jaram;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class CommonUtil {
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private static final String DEBUG_TAG = "CommonUtil";
	private static CookieManager cm = null;

	public static String downloadUrl(String myurl) throws IOException {
		InputStream is = null;

		if (cm == null)
			cm = new CookieManager();

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			cm.setCookies(conn);
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			cm.storeCookies(conn);
			int response = conn.getResponseCode();
			Log.d(DEBUG_TAG, "The response is: " + response);
			is = conn.getInputStream();
			// Convert the InputStream into a string

			return readIt(is);

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	public static String readIt(InputStream stream) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = stream.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	public static String repeat(String str, int times) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < times; i++)
			ret.append(str);
		return ret.toString();
	}
}
