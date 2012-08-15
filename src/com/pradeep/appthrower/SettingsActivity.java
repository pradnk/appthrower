package com.pradeep.appthrower;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.pradeep.appthrower.util.MarketingUtil;

public class SettingsActivity extends PreferenceActivity {

	private Preference profile;
	private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("AppThrower! - Settings");
		addPreferencesFromResource(R.xml.settings);

		sharedPrefs = getSharedPreferences("Settings",
				MODE_PRIVATE);
		
		final CheckBoxPreference sync = (CheckBoxPreference) findPreference("flurry");
		sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean value = (Boolean) newValue;
				sharedPrefs.edit().putBoolean("flurry", value).commit();
				if (value) {
					sync.setSummary("Analytics ON");
				} else {
					sync.setSummary("Analytics OFF");
				}
				if(sharedPrefs.getBoolean("flurry", true)) {
					MarketingUtil.logFlurryEvent("Made Analytics Setting as "+value);
				}
				return true;
			}
		});

		final Preference share = (Preference) findPreference("share");
		share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent newIntent = new Intent(Intent.ACTION_SEND);
				newIntent.setType("text/plain");
				newIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this cool new app AppThrower!\nYou can share apps easily thru SMS! Check out more info at http://www.smore.com/xd3f-appthrower");
				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(newIntent);
				MarketingUtil.logFlurryEvent("Sharing AppThrower socially!");
				return true;
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(sharedPrefs.getBoolean("flurry", true)) {
			MarketingUtil.startFlurry(this);
			ComponentName componentName = getComponentName();
			MarketingUtil.logFlurryEvent("Entered " + componentName.getClassName());
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(sharedPrefs.getBoolean("flurry", true)) {
			MarketingUtil.endFlurry(this);
		}
	}		
}