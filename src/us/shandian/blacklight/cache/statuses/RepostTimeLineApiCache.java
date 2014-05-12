package us.shandian.blacklight.cache.statuses;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import us.shandian.blacklight.api.statuses.RepostTimeLineApi;
import us.shandian.blacklight.cache.Constants;
import us.shandian.blacklight.cache.database.tables.RepostTimeLineTable;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.model.RepostListModel;

public class RepostTimeLineApiCache extends HomeTimeLineApiCache
{
	private long mId;

	public RepostTimeLineApiCache(Context context, long id) {
		super(context);
		mId = id;
	}

	@Override
	public void cache() {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();

		db.delete(RepostTimeLineTable.NAME, RepostTimeLineTable.MSGID + "=?", new String[]{String.valueOf(mId)});

		ContentValues values = new ContentValues();
		values.put(RepostTimeLineTable.MSGID, mId);
		values.put(RepostTimeLineTable.JSON, new Gson().toJson((RepostListModel) mMessages));

		db.insert(RepostTimeLineTable.NAME, null, values);

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	protected Cursor query() {
		return mHelper.getReadableDatabase().query(RepostTimeLineTable.NAME, new String[]{
			RepostTimeLineTable.MSGID,
			RepostTimeLineTable.JSON
		}, RepostTimeLineTable.MSGID + "=?", new String[]{String.valueOf(mId)}, null, null, null);
	}

	@Override
	protected MessageListModel load() {
		return RepostTimeLineApi.fetchRepostTimeLine(mId, Constants.HOME_TIMELINE_PAGE_SIZE, ++mCurrentPage);
	}

	@Override
	protected Class<? extends MessageListModel> getListClass() {
		return RepostListModel.class;
	}
}
