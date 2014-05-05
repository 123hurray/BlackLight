package us.shandian.blacklight.cache.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import us.shandian.blacklight.cache.database.tables.UsersTable;

public class DataBaseHelper extends SQLiteOpenHelper
{
	private static String DB_NAME = "weibo_data";
	private static int DB_VER = 1;
	
	private static DataBaseHelper instance;
	
	private DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UsersTable.CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int from, int to) {
		// Hmmm... We have not had any previous version
	}
	
	// TODO Clean unavailable records
	
	public static synchronized DataBaseHelper instance(Context context) {
		if (instance == null) {
			instance = new DataBaseHelper(context);
		}
		
		return instance;
	}

}
