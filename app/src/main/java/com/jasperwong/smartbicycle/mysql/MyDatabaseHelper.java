package com.jasperwong.smartbicycle.mysql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

	public static final String CREATE_BOOK = "create table Book ("
			+ "id integer primary key autoincrement, " 
			+ "author text, "
			+ "price real, " 
			+ "pages integer, " 
			+ "name text)";
	
	public static final String CREATE_CATEGORY = "create table Category ("
			+ "id integer primary key autoincrement, "
			+ "category_name text, "
			+ "category_code integer)";

	public static final String CREAT_DATE_RECORD="create table DateRecord ("+
			"id integer primary key autoincrement, "+
			"date text, "+
			"record real)";

//	public static final String CREAT_MONTH_RECORD="create table MonthRecord ("+
//			"month text, "+
//			"record integer)";

	private Context mContext;

	public MyDatabaseHelper(Context context, String name,
							CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL(CREATE_BOOK);
//		db.execSQL(CREATE_CATEGORY);
		db.execSQL(CREAT_DATE_RECORD);
		Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(CREAT_DATE_RECORD);
//		db.execSQL("drop table if exists Book");
//		db.execSQL("drop table if exists Category");
		onCreate(db);
	}

}
