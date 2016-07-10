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

package info.papdt.blacklight.api.statuses;

import android.util.Log;
import com.google.gson.Gson;
import info.papdt.blacklight.api.BaseApi;
import info.papdt.blacklight.api.Constants;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.support.http.WeiboParameters;
import org.json.JSONObject;

import java.util.List;

import static info.papdt.blacklight.BuildConfig.DEBUG;

/* Fetches a Public Timeline */
public class HotTimeLineApi extends BaseApi
{
	private static final String TAG = HotTimeLineApi.class.getSimpleName();
	private static class IdsModel {
		public List<Long> ids;
		public String getIdsString() {
			return join(ids.toArray(), ',', 0, ids.size());
		}
	}
	public static String join(final Object[] array, final char separator, final int startIndex, final int endIndex) {
		if (array == null) {
			return null;
		}
		final int noOfItems = endIndex - startIndex;
		if (noOfItems <= 0) {
			return "";
		}
		final StringBuilder buf = new StringBuilder(noOfItems * 16);
		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}
	public static MessageListModel fetchHotTimeLine(int count, int page) {
		WeiboParameters params = new WeiboParameters();
		params.put("catlog_id", "102803_ctg1_8999_-_ctg1_8999");
		params.put("page", page);

		try {
			JSONObject json = requestWithoutAccessToken(Constants.HOT_TIMELINE_IDS, params, HTTP_GET);
			IdsModel idsModel = new Gson().fromJson(json.toString(), IdsModel.class);
			params = new WeiboParameters();
			params.put("ids", idsModel.getIdsString());
			json = request(Constants.HOT_TIMELINE, params, HTTP_GET);
			return new Gson().fromJson(json.toString(), MessageListModel.class);
		} catch (Exception e) {
			if (DEBUG) {
				Log.d(TAG, "Cannot fetch hot timeline, " + e.getClass().getSimpleName());
				Log.d(TAG, Log.getStackTraceString(e));
			}
			return null;
		}
	}
}
