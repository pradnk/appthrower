package com.pradeep.appthrower;

import android.app.Application;
import android.content.Context;

public class AppThrower extends Application {
	private static AppThrower context = null;

	public AppThrower() {
		if (context == null) {
			context = this;
		}
	}

	public static Context getAppThrowerApplicationContext() {
		return context;
	}
	
}
