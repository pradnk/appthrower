package com.pradeep.appthrower.util;

import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.flurry.android.FlurryAgent;

public class MarketingUtil {
	
	// dev
//	private static final String flurryID = "CHTYJ2248X869BNLZQNE";
	// prod
	private static final String flurryID = "P4K5KE4C5XTBFAHRWV1H";
	private static String deviceUUID;

	public static void startFlurry(Context context) {
		Log.v("Flurry", "Started flurry");
		FlurryAgent.setReportLocation(false);
		FlurryAgent.setUserId(getDeviceUUID(context));
		FlurryAgent.setUseHttps(true);
		FlurryAgent.onStartSession(context, flurryID);
	}

	public static void endFlurry(Context context) {
		Log.v("Flurry", "Ended flurry");
		FlurryAgent.onEndSession(context);
	}

	public static void logFlurryEvent(String eventName) {
		FlurryAgent.logEvent(eventName);
	}
	
	public static void logFlurryEvent(String eventName, Map<String, String> params) {
		FlurryAgent.logEvent(eventName, params);
	}
	
	public static String getDeviceUUID(Context context) {

		if (deviceUUID == null) {
			initializeDeviceUUID(context);
		}
		return deviceUUID;
	}

	public static void initializeDeviceUUID(Context context) {
		deviceUUID = getAndroidId(context);
	}
	
	public static String getAndroidId(Context context) {
		if (deviceUUID == null) {
			deviceUUID = getDeviceId(context);
			if (deviceUUID == null) {
				deviceUUID = UUID.randomUUID().toString();
				setDeviceId(context, deviceUUID);
			}
		}
		return deviceUUID;
	}
	
	public static void setDeviceId(Context context, String deviceId) {
		SharedPreferences dataPersistor = context.getSharedPreferences(
				"Flurry", Context.MODE_PRIVATE);
		Editor editor = dataPersistor.edit();
		editor.putString("DEVICE_ID", deviceId);
		editor.commit();
	}
	
	public static String getDeviceId(Context context) {
		SharedPreferences dataPersistor = context.getSharedPreferences(
				"Flurry", Context.MODE_PRIVATE);
		return dataPersistor.getString("DEVICE_ID", null);
	}
}