package com.pradeep.appthrower.adapter;

import com.pradeep.appthrower.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactListAdapter extends CursorAdapter implements Filterable {

	public int COLUMN_NUMBER;

	public String COLUMN_NAME;

	ContactListAdapter adapter;

	protected ContentResolver content;

	protected Cursor cursor;

	public static final String[] PEOPLE_PROJECTION = new String[] {
			ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			Phone.NUMBER,
	};
	
	private ContentResolver mCR;

	public ContactListAdapter(Context context, Cursor c, int columnNumber, String columnName) {
		super(context, c);
		mCR = context.getContentResolver();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		((TextView) view).setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + "<"+cursor.getString(cursor.getColumnIndex(Phone.NUMBER))+">");
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final TextView view = (TextView) inflater.inflate(
				R.layout.dropdown_item_1line, parent, false);

		view.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + "<"+cursor.getString(cursor.getColumnIndex(Phone.NUMBER))+">");

		return view;

	}

	@Override
	public String convertToString(Cursor cursor) {

		return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + "<"+cursor.getString(cursor.getColumnIndex(Phone.NUMBER))+">";
	}

	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		System.out.println("CO: " + constraint);
		if (getFilterQueryProvider() != null) {
			return getFilterQueryProvider().runQuery(constraint);
		}

		StringBuilder buffer = null;
		String[] args = null;
		if (constraint != null) {
			buffer = new StringBuilder();
			buffer.append("UPPER(");
			buffer.append(ContactsContract.Contacts.DISPLAY_NAME);
			buffer.append(") GLOB ?");
			buffer.append(" and "+Phone.TYPE + " = "+ Phone.TYPE_MOBILE);
			args = new String[] {
					constraint.toString().toUpperCase() + "*"
			};
		}

		Cursor c = mCR
				.query(Phone.CONTENT_URI, PEOPLE_PROJECTION,
						buffer == null ? null : buffer.toString(), args,
						null);
		
		if(c != null && c.getCount() == 0) {
			if (constraint != null) {
				buffer = new StringBuilder();
				buffer.append("UPPER(");
				buffer.append(Phone.NUMBER);
				buffer.append(") GLOB ?");
				buffer.append(" and "+Phone.TYPE + " = "+ Phone.TYPE_MOBILE);
				args = new String[] {
						constraint.toString().toUpperCase() + "*"
				};
			}

			c = mCR
					.query(Phone.CONTENT_URI, PEOPLE_PROJECTION,
							buffer == null ? null : buffer.toString(), args,
							null);
		}
		
		return c;
	}
}