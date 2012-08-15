package com.pradeep.appthrower.provider;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class CustomDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "CustomDatabaseHelper";

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "appthrow.db";

	private final Context mContext;

	private String[] mUnrestrictedPackages;

	private boolean mReopenDatabase = false;

	private static CustomDatabaseHelper sSingleton = null;

	public interface Tables {
		public static final String APP_SHARED_INFO = "personal_info";
	}
	
	public interface Views {
        public static final String APP_SHARED_INFO_VIEW = "view_personal_info";
	}
	

	public interface AppSharedInfoColumns {

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"	+ Tables.APP_SHARED_INFO;
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CustomDataProvider.AUTHORITY_URI, Tables.APP_SHARED_INFO);

		public static final String _ID = BaseColumns._ID;
		public static final String APP_NAME = "app_name";
		public static final String PACKAGE_NAME = "package_name";
		public static final String SHARED_DATE = "shared_date";
		public static final String SENT_OR_RECEIVED = "sent_or_received";
		public static final String FROM_TO = "from_to";
		public static final String FROM_TO_NUMBER = "from_to_number";
		public static final String OPENED = "opened";
	}

	public static synchronized CustomDatabaseHelper getInstance(Context context) {
		Log.i(TAG, "getInstance");
		if (sSingleton == null) {
			sSingleton = new CustomDatabaseHelper(context);
		}
		return sSingleton;
	}

	CustomDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.i(TAG, "Creating OpenHelper");

		Resources resources = context.getResources();

		mContext = context;
		int resourceId = resources.getIdentifier("unrestricted_packages",
				"array", context.getPackageName());
		if (resourceId != 0) {
			mUnrestrictedPackages = resources.getStringArray(resourceId);
		} else {
			mUnrestrictedPackages = new String[0];
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.i(TAG, "SnaptaxDatabaseHelper.onCreate");
		

		db.execSQL("CREATE TABLE " + Tables.APP_SHARED_INFO + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
				+ AppSharedInfoColumns.APP_NAME + " TEXT , "
				+ AppSharedInfoColumns.PACKAGE_NAME+ " TEXT , "
				+ AppSharedInfoColumns.SHARED_DATE + " TEXT , "
				+ AppSharedInfoColumns.SENT_OR_RECEIVED + " TEXT , "
				+ AppSharedInfoColumns.FROM_TO + " TEXT , "
				+ AppSharedInfoColumns.FROM_TO_NUMBER + " TEXT , "
				+ AppSharedInfoColumns.OPENED + " TEXT" + ");");
		Log.i(TAG, "SnaptaxDatabaseHelper.onCreate : " + "CREATE TABLE "
				+ Tables.APP_SHARED_INFO);

		mReopenDatabase = true;

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion
				+ ", data will be lost!");

		db.execSQL("DROP TABLE IF EXISTS " + Tables.APP_SHARED_INFO + ";");
		onCreate(db);

	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		Log.i(TAG, "SnaptaxDatabaseHelper.getWritableDatabase");
		SQLiteDatabase db = super.getWritableDatabase();
		if (mReopenDatabase) {
			mReopenDatabase = false;
			close();
			db = super.getWritableDatabase();
		}
		return db;
	}

	public void wipeData() {
		SQLiteDatabase db = getWritableDatabase();

		db.execSQL("DROP TABLE IF EXISTS " + Tables.APP_SHARED_INFO + ";");

		// Note: we are not removing reference data from Tables.NICKNAME_LOOKUP

		db.execSQL("VACUUM;");
	}

}
