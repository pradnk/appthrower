package com.pradeep.appthrower.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.pradeep.appthrower.provider.CustomDatabaseHelper.AppSharedInfoColumns;
import com.pradeep.appthrower.provider.CustomDatabaseHelper.Tables;
import com.pradeep.appthrower.provider.CustomDatabaseHelper.Views;

public class CustomDataProvider extends CustomContentProvider {

	private static final String TAG = "SnapTaxDataProvider";
	private static final boolean VERBOSE_LOGGING = Log.isLoggable(TAG,
			Log.VERBOSE);

	public static final String AUTHORITY = "com.pradeep.appthrower";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final HashMap<String, String> sCountProjectionMap;
	private static final HashMap<String, String> sPersonalInfoProjectionMap;

	private volatile CountDownLatch mAccessLatch;

	private static final int APP_SHARED_INFO = 1000;

	static {
		// SnapTax URI matching table
		final UriMatcher matcher = sUriMatcher;
		matcher.addURI(AUTHORITY, Tables.APP_SHARED_INFO, APP_SHARED_INFO);

	}

	static {

		sCountProjectionMap = new HashMap<String, String>();
		sCountProjectionMap.put(BaseColumns._COUNT, "COUNT(*)");

		sPersonalInfoProjectionMap = new HashMap<String, String>();
		sPersonalInfoProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.APP_NAME,
				AppSharedInfoColumns.APP_NAME);
		
		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.PACKAGE_NAME,
				AppSharedInfoColumns.PACKAGE_NAME);

		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.SHARED_DATE,
				AppSharedInfoColumns.SHARED_DATE);
		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.SENT_OR_RECEIVED,
				AppSharedInfoColumns.SENT_OR_RECEIVED);
		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.FROM_TO,
				AppSharedInfoColumns.FROM_TO);
		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.FROM_TO_NUMBER,
				AppSharedInfoColumns.FROM_TO_NUMBER);
		sPersonalInfoProjectionMap.put(AppSharedInfoColumns.OPENED,
				AppSharedInfoColumns.OPENED);
	}

	@Override
	protected SQLiteOpenHelper getDatabaseHelper(Context context) {
		return CustomDatabaseHelper.getInstance(context);
	}

	@Override
	protected Uri insertInTransaction(Uri uri, ContentValues values) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "insertInTransaction: " + uri);
		}
		final int match = sUriMatcher.match(uri);
		long id = 0;

		switch (match) {
			case APP_SHARED_INFO: {
				id = mDb.insert(Tables.APP_SHARED_INFO, null, values);
				break;
			}
		}

		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	protected int updateInTransaction(Uri uri, ContentValues values,
			String selection, String[] selectionArgs) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "updateInTransaction: " + uri);
		}

		int count = 0;

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case APP_SHARED_INFO: {
				count = mDb.update(Tables.APP_SHARED_INFO, values, selection,
						selectionArgs);
				break;
			}
		}
		return count;
	}

	@Override
	protected int deleteInTransaction(Uri uri, String selection,
			String[] selectionArgs) {

		int count = 0;
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "deleteInTransaction: " + uri + "\n selection = "
					+ selection);
		}
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case APP_SHARED_INFO: {
				count = mDb.delete(Tables.APP_SHARED_INFO, selection, selectionArgs);
				break;
			}
		}
		return count;
	}

	@Override
	protected void notifyChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case APP_SHARED_INFO: {
				return AppSharedInfoColumns.CONTENT_TYPE;
			}
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "query: " + uri);
		}

		final SQLiteDatabase db = mDbHelper.getReadableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String groupBy = null;
		String limit = getLimit(uri);

		// TODO: Consider writing a test case for RestrictionExceptions when you
		// write a new query() block to make sure it protects restricted data.
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case APP_SHARED_INFO: {
				qb.setTables(Tables.APP_SHARED_INFO);
				qb.setProjectionMap(sPersonalInfoProjectionMap);
	
				break;
			}
		}
		return query(db, qb, projection, selection, selectionArgs, sortOrder,
				groupBy, limit);
	}

	private Cursor query(final SQLiteDatabase db, SQLiteQueryBuilder qb,
			String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String groupBy, String limit) {
		if (projection != null && projection.length == 1
				&& BaseColumns._COUNT.equals(projection[0])) {
			qb.setProjectionMap(sCountProjectionMap);
		}
		final Cursor c = qb.query(db, projection, selection, selectionArgs,
				groupBy, null, sortOrder, limit);
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(),
					AUTHORITY_URI);
		}
		return c;
	}

	private CustomDatabaseHelper mDbHelper;

	@Override
	public boolean onCreate() {
		super.onCreate();
		try {
			return initialize();
		} catch (RuntimeException e) {
			Log.e(TAG, "Cannot start provider", e);
			return false;
		}
	}

	private boolean initialize() {
		// final Context context = getContext();
		mDbHelper = (CustomDatabaseHelper) getDatabaseHelper();

		final SQLiteDatabase db = mDbHelper.getReadableDatabase();

		return true;
	}

	private void waitForAccess() {
		CountDownLatch latch = mAccessLatch;
		if (latch != null) {
			while (true) {
				try {
					latch.await();
					mAccessLatch = null;
					return;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i(TAG, "insert " + uri + "\n ContentValues " + values);
		waitForAccess();
		return super.insert(uri, values);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.i(TAG, "update " + uri + "\n ContentValues = " + values
				+ "\n selection =" + selection);
		waitForAccess();
		return super.update(uri, values, selection, selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.i(TAG, "delete " + uri + "\n selection =" + selection);
		waitForAccess();
		return super.delete(uri, selection, selectionArgs);
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		waitForAccess();
		return super.applyBatch(operations);
	}

	/* package */void wipeData() {
		mDbHelper.wipeData();
	}

	/**
	 * Gets the value of the "limit" URI query parameter.
	 * 
	 * @return A string containing a non-negative integer, or <code>null</code>
	 *         if the parameter is not set, or is set to an invalid value.
	 */
	private String getLimit(Uri url) {
		String limitParam = url.getQueryParameter("limit");
		if (limitParam == null) {
			return null;
		}
		// make sure that the limit is a non-negative integer
		try {
			int l = Integer.parseInt(limitParam);
			if (l < 0) {
				Log.w(TAG, "Invalid limit parameter: " + limitParam);
				return null;
			}
			return String.valueOf(l);
		} catch (NumberFormatException ex) {
			Log.w(TAG, "Invalid limit parameter: " + limitParam);
			return null;
		}
	}
}
