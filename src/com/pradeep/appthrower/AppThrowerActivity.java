package com.pradeep.appthrower;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.pradeep.appthrower.adapter.ContactListAdapter;
import com.pradeep.appthrower.provider.CustomDatabaseHelper.AppSharedInfoColumns;
import com.pradeep.appthrower.socialnetwork.FacebookSocialNetwork;
import com.pradeep.appthrower.util.MarketingUtil;
import com.pradeep.appthrower.vo.SharedAppsListVO;

public class AppThrowerActivity extends Activity {

	private List<String> listOfPackages = new ArrayList<String>();
	private List<String> listOfAppNames = new ArrayList<String>();
	String SENT = "SMS_SENT";
	String DELIVERED = "SMS_DELIVERED";
	private ListView listView;
	private ViewGroup sendToView;
	private AutoCompleteTextView sendTo;
	private PackageManager packageManager;
	
	public int COLUMN_NUMBER;

	public String COLUMN_NAME;

	ContactListAdapter adapter;

	protected ContentResolver content;

	protected Cursor cursor;

	private static final String GOOGLE_MARKET = "com.android.vending";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(101);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_thrower);

		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
		actionbar.setHomeLogo(R.drawable.appthrower_home);
		actionbar.addAction(new Action() {

			@Override
			public void performAction(View view) {
				startActivity(new Intent(AppThrowerActivity.this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 0));
			}

			@Override
			public int getDrawable() {
				return R.drawable.share;
			}
		});
		actionbar.addAction(new Action() {

			@Override
			public void performAction(View view) {
				startActivity(new Intent(AppThrowerActivity.this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 1));
			}

			@Override
			public int getDrawable() {
				return R.drawable.download;
			}
		});
		actionbar.addAction(new Action() {

			@Override
			public void performAction(View view) {
				startActivity(new Intent(AppThrowerActivity.this, SettingsActivity.class));
			}

			@Override
			public int getDrawable() {
				return R.drawable.settings;
			}
		});
		// ******** End of Action Bar configuration
		
		listView = (ListView) findViewById(R.id.list);

		List<SharedAppsListVO> appsList = new ArrayList<SharedAppsListVO>();
		
		packageManager = getPackageManager();
		List<ApplicationInfo> list = packageManager
				.getInstalledApplications(PackageManager.GET_META_DATA);
		for (int i = 0; i < list.size(); i++) {
			ApplicationInfo appInfo = list.get(i);
			if (appInfo != null && appInfo.className != null && appInfo.packageName != null) {
				if (packageManager.getInstallerPackageName(appInfo.packageName) != null) {
					listOfPackages.add(appInfo.packageName);
					listOfAppNames.add(packageManager.getApplicationLabel(appInfo).toString());
					
					SharedAppsListVO vo = new SharedAppsListVO();
					vo.appName = packageManager.getApplicationLabel(appInfo).toString();
					vo.packageName = appInfo.packageName;
					try {
						vo.appIcon = getPackageManager().getApplicationIcon(appInfo.packageName);
					} catch (NameNotFoundException e) {
						//Default fallback icon
						vo.appIcon = getResources().getDrawable(android.R.drawable.ic_dialog_info);
					}
					appsList.add(vo);
				}
			}
		}
		
		sendToView = (ViewGroup)findViewById(R.id.shareToView);
		sendTo = (AutoCompleteTextView)findViewById(R.id.shareTo);
		
		
		CustomAdapter adapter = new CustomAdapter(getApplicationContext(), R.layout.appthrower_list_item, appsList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, final int pos,
					long duration) {

				sendToView.setVisibility(View.VISIBLE);
				sendToView.startAnimation(AnimationUtils.loadAnimation(AppThrowerActivity.this, R.anim.push_up_in));
				sendTo.requestFocus();
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
						InputMethodManager.HIDE_IMPLICIT_ONLY);
				

				COLUMN_NUMBER = 1;
				COLUMN_NAME = ContactsContract.Data.DISPLAY_NAME;
				sendTo.setThreshold(1);
				content = getContentResolver();
				
				String[] PROJECTION = new String[] { Contacts._ID,
						Contacts.DISPLAY_NAME, Phone.NUMBER };
				Cursor cursor = content.query(Phone.CONTENT_URI, PROJECTION, null,
						null, null);
				
				ContactListAdapter contactsAdapter = new ContactListAdapter(AppThrowerActivity.this, cursor, COLUMN_NUMBER, COLUMN_NAME);
				sendTo.setAdapter(contactsAdapter);

				Button shareBtn = (Button)findViewById(R.id.shareBtn);
				shareBtn.setOnClickListener(new View.OnClickListener() {
					
					private String senderName;
					private String senderNumber;

					@Override
					public void onClick(View v) {
						
						InputMethodManager imm = (InputMethodManager)
								getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						
						sendToView.startAnimation(AnimationUtils.loadAnimation(AppThrowerActivity.this, R.anim.push_down_out));
						sendToView.setVisibility(View.GONE);
						
						String text = sendTo.getText().toString();
						
						if(!TextUtils.isEmpty(text) && hasNumeric(text)) {
							SmsManager sms = SmsManager.getDefault();
							sms.sendTextMessage(
									text,
									null,
									listOfAppNames.get(pos)+"|https://market.android.com/details?id="
											+ listOfPackages.get(pos)+"|thrown via AppThrower!", null,
									null);
							final SharedPreferences sharedPrefs = getSharedPreferences("Settings",
									MODE_PRIVATE);
							if(sharedPrefs.getBoolean("flurry", true)) {
								MarketingUtil.logFlurryEvent("Threw app "+listOfAppNames.get(pos));
								FacebookSocialNetwork facebook = new FacebookSocialNetwork(AppThrowerActivity.this);
								facebook.postStatus("Threw app "+listOfAppNames.get(pos));
							}
							ContentValues values = new ContentValues();
							values.put(AppSharedInfoColumns.APP_NAME, listOfAppNames.get(pos));
							values.put(AppSharedInfoColumns.PACKAGE_NAME, listOfPackages.get(pos));
							SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yy @ hh:mm");
							String sharedDate = dateFormat.format(new Date());
							values.put(AppSharedInfoColumns.SHARED_DATE, sharedDate);
							values.put(AppSharedInfoColumns.SENT_OR_RECEIVED, "0");
							if(text != null && text.indexOf("<") != -1) {
								String[] splitSenderName = text.split("<");
								senderName = splitSenderName[0];
								senderNumber = splitSenderName[1].substring(0, splitSenderName[1].length() - 1);
							}
							if(TextUtils.isEmpty(senderName)) {
								senderName = "Unknown";
								senderNumber = text;
							}
							values.put(AppSharedInfoColumns.FROM_TO, senderName);
							values.put(AppSharedInfoColumns.FROM_TO_NUMBER, senderNumber);
							values.put(AppSharedInfoColumns.OPENED, "");
							getContentResolver().insert(AppSharedInfoColumns.CONTENT_URI, values);
							Toast.makeText(AppThrowerActivity.this, listOfAppNames.get(pos) + " successfully shared with "+senderName, Toast.LENGTH_LONG).show();
						}
					}
				});
				
			}
		});
		
		if(isFirstRun()) {
			Toast.makeText(this, "Touch any app to throw!", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		MarketingUtil.startFlurry(this);
		ComponentName componentName = getComponentName();
		final SharedPreferences sharedPrefs = getSharedPreferences("Settings",
				MODE_PRIVATE);
		if(sharedPrefs.getBoolean("flurry", true)) {
			MarketingUtil.logFlurryEvent("Entered " + componentName.getClassName());
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
	
	private boolean isFirstRun() {
		SharedPreferences prefs = getSharedPreferences("FirstRun", MODE_PRIVATE);
		if(!prefs.getBoolean("IsFirst", false)) {
			prefs.edit().putBoolean("IsFirst", true).commit();
			return true;
		}
		return false;
	}

	public static boolean hasNumeric(String str) {
		boolean hasNumbers = false;
		char[] charArray = str.toCharArray();
		for (char c : charArray) {
			if (Character.isDigit(c)) {
				hasNumbers = true;
			}
		}
		return hasNumbers;
	}
	
	@Override
	public void onBackPressed() {
		if(sendToView != null && sendToView.getVisibility() == View.VISIBLE) { 
			sendToView.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
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
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			holder.appName.setText(vo.appName);
			holder.appIcon.setImageDrawable(vo.appIcon);
			return convertView;
		}
		
	}
	
	private static class ViewHolder {
		ImageView appIcon;
		TextView appName;
	}
}