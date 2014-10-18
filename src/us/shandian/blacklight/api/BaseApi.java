/* 
 * Copyright (C) 2014 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.shandian.blacklight.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import us.shandian.blacklight.support.http.HttpUtility;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

public abstract class BaseApi {
	// Http Methods
	protected static final String HTTP_GET = HttpUtility.GET;
	protected static final String HTTP_POST = HttpUtility.POST;
	private static final String TAG = BaseApi.class.getSimpleName();
	// Access Token
	private static String mAccessToken;

	protected static JSONObject request(String url, WeiboParameters params, String method) throws Exception {
		return (JSONObject) request(mAccessToken, url, params, method, JSONObject.class);
	}

	protected static JSONArray requestArray(String url, WeiboParameters params, String method) throws Exception {
		return (JSONArray) request(mAccessToken, url, params, method, JSONArray.class);
	}

	protected static String requestString(String url, WeiboParameters params, String method) throws Exception {
		return (String) request(mAccessToken, url, params, method, null);
	}

	protected static Object request(String token, String url, WeiboParameters params, String method, Class<?> jsonClass) throws Exception {
		if (token == null) {
			return null;
		} else {
			params.put("access_token", token);
			String jsonData = HttpUtility.doRequest(url, params, method);

			if (DEBUG) {
				Log.d(TAG, "jsonData = " + jsonData);
			}

			if (jsonData != null && (jsonData.contains("{") || jsonData.contains("["))) {
				try {
					return jsonClass.getConstructor(String.class).newInstance(jsonData);
				} catch (Exception e) {
					return jsonData;
				}
			} else {
				return jsonData;
			}
		}
	}

	protected static JSONObject requestWithoutAccessToken(String url, WeiboParameters params, String method) throws Exception {
		String jsonData = HttpUtility.doRequest(url, params, method);

		if (DEBUG) {
			Log.d(TAG, "jsonData = " + jsonData);
		}

		if (jsonData != null && jsonData.contains("{")) {
			return new JSONObject(jsonData);
		} else {
			return null;
		}
	}

	public static String getAccessToken() {
		return mAccessToken;
	}

	public static void setAccessToken(String token) {
		mAccessToken = token;
	}
}
