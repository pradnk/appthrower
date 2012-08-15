package com.pradeep.appthrower.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.pradeep.appthrower.MainActivity;
import com.pradeep.appthrower.R;
import com.pradeep.appthrower.SharedAppsActivity;
import com.pradeep.appthrower.provider.CustomDatabaseHelper.AppSharedInfoColumns;

public class SMSReceiver extends BroadcastReceiver {

	private String mPackageName = "";
	private String mAppName = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		Object messages[] = (Object[]) bundle.get("pdus");
		SmsMessage smsMessage[] = new SmsMessage[messages.length];
		for (int n = 0; n < messages.length; n++) {
			smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		}

		// show first message
		String text = smsMessage[0].getMessageBody();
		if(text != null && text.contains("https://market")) {
			abortBroadcast();
			parseMessageBody(text);
			
			ContentValues values = new ContentValues();
			values.put(AppSharedInfoColumns.APP_NAME, mAppName);
			values.put(AppSharedInfoColumns.PACKAGE_NAME, mPackageName);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yy @ hh:mm");
			String sharedDate = dateFormat.format(new Date());
			values.put(AppSharedInfoColumns.SHARED_DATE, sharedDate);
			values.put(AppSharedInfoColumns.SENT_OR_RECEIVED, "1");
			values.put(AppSharedInfoColumns.FROM_TO, smsMessage[0].getOriginatingAddress());
			values.put(AppSharedInfoColumns.OPENED, "");
			context.getContentResolver().insert(AppSharedInfoColumns.CONTENT_URI, values);
			
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.download, "App \"" + mAppName + "\" caught", new Date().getTime());
			CharSequence contentTitle = "App \""+ mAppName + "\" caught";
			CharSequence contentText = "Click to view details";
			Intent notificationIntent = new Intent(context, SharedAppsActivity.class).putExtra("SENTRECEIVED", 1);
			
			PendingIntent operation = PendingIntent.getActivity(context, 100, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			notification.setLatestEventInfo(context, contentTitle, contentText, operation);
			notificationManager.notify(101, notification);
		}
	}

	private void parseMessageBody(String text) {
		String[] splitForAppName = text.split("\\|");
		mAppName = splitForAppName[0];
		String[] split = splitForAppName[1].split("=");
		if(split != null && split.length >= 1) 
			mPackageName =  split[1];
	}

}
