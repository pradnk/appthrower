package com.pradeep.appthrower;

import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	private static TabHost tabHost;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(101);
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, AppThrowerActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("appthrower")
				.setIndicator("Apps to throw", res.getDrawable(R.drawable.ic_launcher))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 1);
		spec = tabHost
				.newTabSpec("receivedapps")
				.setIndicator("Apps Caught", res.getDrawable(android.R.drawable.ic_menu_save))
				.setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, SharedAppsActivity.class).putExtra("SENTRECEIVED", 0);
		spec = tabHost
				.newTabSpec("sharedapps")
				.setIndicator("Apps Thrown", res.getDrawable(android.R.drawable.ic_menu_send))
				.setContent(intent);
		tabHost.addTab(spec);

		String goTo = getIntent().getStringExtra("GOTO");
		if("RECEIVEDAPPS".equals(goTo)) {
			tabHost.setCurrentTab(1);
		} else {
			tabHost.setCurrentTab(0);
		}
	}
	
	public static TabHost getParentTabHost() {
		return tabHost;
	}
}