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

package us.shandian.blacklight.api.search;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.api.Constants;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.http.WeiboParameters;

import static us.shandian.blacklight.BuildConfig.DEBUG;

/*
  Searches for statuses with an exact topic
*/
public class TopicsApi extends BaseApi
{
	private static final String TAG = TopicsApi.class.getSimpleName();

	public static MessageListModel searchTopic(String q, int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("q", q);
		params.put("count", count);
		params.put("page", page);

		try {
			JSONObject json = request(Constants.SEARCH_TOPICS, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot search, " + e.getClass().getSimpleName());
			}
			return null;
		}
	}
}
