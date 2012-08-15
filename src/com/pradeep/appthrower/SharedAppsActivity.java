package com.pradeep.appthrower;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.pradeep.appthrower.provider.CustomDatabaseHelper.AppSharedInfoColumns;
import com.pradeep.appthrower.util.MarketingUtil;
import com.pradeep.appthrower.vo.SharedAppsListVO;

public class SharedAppsActivity extends Activity {

	private static List<String> listOfPackages = new ArrayList<String>();
	private static List<String> listOfAppNames = new ArrayList<String>();
	String SENT = "SMS_SENT";
	String DELIVERED = "SMS_DELIVERED";
	private ListView listView;
	private TextView emptyText;
	private ViewGroup emptyView;
	private int mSentReceived;
	private ArrayList<SharedAppsListVO> list = new ArrayList<SharedAppsListVO>();
	private static List<String> fromTo = new ArrayList<String>();

	private static final String GOOGLE_MARKET = "com.android.vending";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSentReceived = getIntent().getIntExtra("SENTRECEIVED", -1);
		if (mSentReceived == -1) {
			finish();
			return;
		}
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(101);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.shared_apps);
		
		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
		if(mSentReceived == 0) {
			actionbar.setHomeLogo(R.drawable.home_thrown);
		} else {
			actionbar.setHomeLogo(R.drawable.home_caught);
		}
		actionbar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		actionbar.addAction(new Action() {

			@Override
			public void performAction(View view) {
				finish();
				if(mSentReceived == 0) {
					startActivity(new Intent(SharedAppsActivity.this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 1));
				} else if(mSentReceived == 1) {
					startActivity(new Intent(SharedAppsActivity.this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 0));
				}
			}

			@Override
			public int getDrawable() {
				if(mSentReceived == 0) {
					return R.drawable.download;
				} else {
					return R.drawable.share;
				}
			}
		});
		//End
	}
	
	protected void onResume() {
		super.onResume();
		emptyText = (TextView) findViewById(R.id.emptyText);
		emptyView = (ViewGroup) findViewById(R.id.emptyView);
		listView = (ListView) findViewById(R.id.list);
		
		Cursor cursor = getContentResolver().query(
				AppSharedInfoColumns.CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		
		final List<String> packageNames = new ArrayList<String>();
		final List<String> appNames = new ArrayList<String>();
		list = new ArrayList<SharedAppsListVO>();
		if(cursor != null && cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				if (mSentReceived == 0) {
					if ("0".equals(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.SENT_OR_RECEIVED)))) {
						appNames.add(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.APP_NAME)) + "/" + cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.FROM_TO)));
						fromTo.add(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.FROM_TO)));
						packageNames.add(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.PACKAGE_NAME)));
						SharedAppsListVO vo = new SharedAppsListVO();
						vo.appName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.APP_NAME));
						vo.shareReceivePersonName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.FROM_TO));
						vo.packageName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.PACKAGE_NAME));
						vo.shareReceivePersonNumber = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.FROM_TO_NUMBER));
						try {
							vo.appIcon = getPackageManager().getApplicationIcon(vo.packageName);
							ApplicationInfo isInstalledAppInfo = getPackageManager().getApplicationInfo(vo.packageName, PackageManager.GET_META_DATA);
							if(isInstalledAppInfo != null && isInstalledAppInfo.packageName.equals(vo.packageName)) {
								vo.isInstalled = true;
							}
						} catch (NameNotFoundException e) {
							//Default fallback icon
							vo.appIcon = getResources().getDrawable(android.R.drawable.ic_dialog_info);
							vo.isInstalled = false;
						}
						list.add(vo);
					}
				} else if (mSentReceived == 1) {
					if ("1".equals(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.SENT_OR_RECEIVED)))) {
						
						String number = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.FROM_TO));
						Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
						Cursor lookupCursor = getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
						String name = "Unknown";
						if(lookupCursor != null) {
							lookupCursor.moveToFirst();
							name = lookupCursor.getString(0);
							if(TextUtils.isEmpty(name)) {
								name = "Unknown";
							}
						}
						
						String appName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.APP_NAME));
						appNames.add(appName + "/" + name);
						fromTo.add(name);
						packageNames.add(cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.PACKAGE_NAME)));
						
						SharedAppsListVO vo = new SharedAppsListVO();
						vo.appName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.APP_NAME));
						vo.shareReceivePersonName = name;
						vo.shareReceivePersonNumber = number;
						vo.packageName = cursor.getString(cursor.getColumnIndex(AppSharedInfoColumns.PACKAGE_NAME));
						try {
							vo.appIcon = getPackageManager().getApplicationIcon(vo.packageName);
							ApplicationInfo isInstalledAppInfo = getPackageManager().getApplicationInfo(vo.packageName, PackageManager.GET_META_DATA);
							if(isInstalledAppInfo != null && isInstalledAppInfo.packageName.equals(vo.packageName)) {
								vo.isInstalled = true;
							}
						} catch (NameNotFoundException e) {
							//Default fallback icon
							vo.appIcon = getResources().getDrawable(android.R.drawable.ic_dialog_info);
							vo.isInstalled = false;
						}
						list.add(vo);
					}
				}
				cursor.moveToNext();
			}
		}
		cursor.close();

		if (packageNames != null && packageNames.size() == 0) {
			String sendReceiveTxt = "";
			if (mSentReceived == 0) {
				sendReceiveTxt = "thrown";
			} else {
				sendReceiveTxt = "caught";
			}
			emptyView.setVisibility(View.VISIBLE);
			emptyView.startAnimation(AnimationUtils.loadAnimation(SharedAppsActivity.this, R.anim.zoom_center));
			emptyText.setText("No apps have been " + sendReceiveTxt
					+ " by you. Start now!");
			listView.setVisibility(View.GONE);
		} else {
			CustomAdapter adapter = new CustomAdapter(getApplicationContext(), R.layout.custom_list_sharereceive_item, list);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View view,
						int pos, long duration) {

					String url = "market://details?id=" + packageNames.get(pos);//+ "&rdid="+packageNames.get(pos)+"&rdot=1";
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(url)));
					finish();
				}

			});
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		final SharedPreferences sharedPrefs = getSharedPreferences("Settings",
				MODE_PRIVATE);
		if(sharedPrefs.getBoolean("flurry", true)) {
			MarketingUtil.startFlurry(this);
			if(mSentReceived == 0) {
				MarketingUtil.logFlurryEvent("Entered ThrownApps (SharedAppsActivity) Activity showing " + list.size() + "items");
			} else if(mSentReceived == 1) {
				MarketingUtil.logFlurryEvent("Entered CaughtApps (SharedAppsActivity) Activity showing " + list.size() + "items");
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		final SharedPreferences sharedPrefs = getSharedPreferences("Settings",
				MODE_PRIVATE);
		if(sharedPrefs.getBoolean("flurry", true)) {
			MarketingUtil.endFlurry(this);
		}
	}
	
	class CustomAdapter extends ArrayAdapter<SharedAppsListVO> {

		private List<SharedAppsListVO> list;
		private LayoutInflater layoutInflaterService;
		private int resource;

		public CustomAdapter(Context context, int resource,
				List<SharedAppsListVO> list) {
			super(context, resource, list);
			this.list = list;
			this.resource = resource;
			this.layoutInflaterService = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			SharedAppsListVO vo = getItem(position);
			
			if (convertView == null) {
				convertView = layoutInflaterService.inflate(resource, null);

				holder = new ViewHolder();
				holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
				holder.appName = (TextView) convertView.findViewById(R.id.appName);
				holder.sharedPersonName = (TextView) convertView.findViewById(R.id.sharedPersonName);
				holder.sharedPersonNumber = (TextView) convertView.findViewById(R.id.sharedPersonNumber);
				holder.isAppInstalled = (TextView) convertView.findViewById(R.id.isAppInstalled);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			holder.appName.setText(vo.appName);
			holder.sharedPersonName.setText(vo.shareReceivePersonName);
			holder.sharedPersonNumber.setText(vo.shareReceivePersonNumber);
			holder.appIcon.setImageDrawable(vo.appIcon);
			if(vo.isInstalled) {
				holder.isAppInstalled.setText("INSTALLED");
				holder.isAppInstalled.setTextColor(Color.DKGRAY);
			} else {
				holder.isAppInstalled.setText("NOT INSTALLED");
				holder.isAppInstalled.setTextColor(Color.BLUE);
			}
			return convertView;
		}
		
	}
	
	private static class ViewHolder {
		ImageView appIcon;
		TextView appName;
		TextView sharedPersonName;
		TextView sharedPersonNumber;
		TextView isAppInstalled;
	}
}