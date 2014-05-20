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

package us.shandian.blacklight.cache.comments;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.comments.StatusCommentApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.StatusCommentTable;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.CommentListModel;
import us.shandian.blacklight.model.MessageListModel;

public class StatusCommentApiCache extends HomeTimeLineApiCache
{
	private long mId;
	
	public StatusCommentApiCache(Context context, long id) {
		super(context);
		mId = id;
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		
		db.delete(StatusCommentTable.NAME, StatusCommentTable.MSGID + "=?", new String[]{String.valueOf(mId)});

		ContentValues values = new ContentValues();
		values.put(StatusCommentTable.MSGID, mId);
		values.put(StatusCommentTable.JSON, new Gson().toJson((CommentListModel) mMessages));

		db.insert(StatusCommentTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(StatusCommentTable.NAME, new String[]{
			StatusCommentTable.MSGID,
			StatusCommentTable.JSON
		}, StatusCommentTable.MSGID + "=?", new String[]{String.valueOf(mId)}, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return StatusCommentApi.fetchCommentOfStatus(mId, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}

	@Override
	protected Class<? extends MessageListModel> getListClass() {
		return CommentListModel.class;
	}
}
