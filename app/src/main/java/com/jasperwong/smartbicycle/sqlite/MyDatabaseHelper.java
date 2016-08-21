package com.jasperwong.smartbicycle.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

	public static final String CREATE_USER = "create table USER ("
			+ "username text, "
			+ "date text, "
			+ "distanceDay real, "
			+ "distanceTotal real, "
			+ "hourTotal real, "
			+ "timesTotal real,"
            + "primary key(`username`,`date`))";
	
//	public static final String CREATE_CATEGORY = "create table Category ("
//			+ "id integer primary key autoincrement, "
//			+ "category_name text, "
//			+ "category_code integer)";

	private Context mContext;

	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//        db.execSQL("drop table if exists test");
		db.execSQL(CREATE_USER);
//		db.execSQL(CREATE_CATEGORY);
		Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("drop table if exists USER");
		onCreate(db);
	}

}
