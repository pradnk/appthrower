package com.pradeep.appthrower.socialnetwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.pradeep.appthrower.AppThrower;
import com.pradeep.appthrower.util.Constants;

public class WebViewClientActivity extends Activity {
	protected static final int COMPLETE = 1;

	protected static final int PROGRESS_DIALOG_ACTIVITY = 100;

	private WebView webview;

	Facebook facebook = new Facebook(Constants.FACEBOOK_AUTH);
	
	public final Handler webpageDownloadCompleteHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == COMPLETE) {
				webview.setFocusable(true);
				webview.requestFocus();
			}

		}
	};

	protected String returnURL;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		facebook.authorizeCallback(requestCode, resultCode, data);

		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String status = getIntent().getStringExtra("STATUS");
		
		final boolean isFacebook = getIntent().getBooleanExtra("isfacebook", false);
		if(isFacebook) {
			facebook.authorize(this, new String[]{"publish_stream"}, new DialogListener() {
	            @Override
	            public void onComplete(Bundle values) {
	            	FacebookSocialNetwork socialNetwork = new FacebookSocialNetwork(AppThrower.getAppThrowerApplicationContext());//(FacebookSocialNetwork)SocialNetwork.getProvider(SocialNetwork.SOCIAL_NETWORK_FACEBOOK);
					socialNetwork.setTokens(facebook.getAccessToken(), (values != null ? values.get("id") : null), facebook.getAccessExpires());
					//socialNetwork.fetchProfile();
					if(status != null) {
						socialNetwork.postStatus(status);
					}
					finish();
	            }

	            @Override
	            public void onCancel() {
	            	Toast.makeText(WebViewClientActivity.this, "Cancel!", Toast.LENGTH_LONG).show();
	            }

				@Override
				public void onFacebookError(FacebookError e) {
					Toast.makeText(WebViewClientActivity.this, "FacebookError "+e, Toast.LENGTH_LONG).show();
				}

				@Override
				public void onError(DialogError e) {
					Toast.makeText(WebViewClientActivity.this, "Error "+e, Toast.LENGTH_LONG).show();
					
				}
	        });
		}
	}
}