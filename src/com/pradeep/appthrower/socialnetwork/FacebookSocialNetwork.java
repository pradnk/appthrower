package com.pradeep.appthrower.socialnetwork;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.pradeep.appthrower.AppThrower;
import com.pradeep.appthrower.util.Constants;
import com.pradeep.appthrower.util.Profile;
import com.pradeep.appthrower.util.SharedPreferenceManager;


public class FacebookSocialNetwork extends SocialNetwork {

	private static final String NAME = "Facebook";
	private Context context;
	String facebookId = "";

	public FacebookSocialNetwork(Context context) {
		this.context = context;
	}
	
	@Override
	public void authenticate() {
		context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
	}

	@Override
	public void authenticate(boolean fetchProfile) {
		context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
	}

	@Override
	public void postStatus(String status) {
		if(isAuthenticated()) {
			Facebook facebook = new Facebook(Constants.FACEBOOK_AUTH);
			String accessToken = getToken();
			long expires = getExpires();
			if (accessToken != null)
				facebook.setAccessToken(accessToken);
			if (expires != 0)
				facebook.setAccessExpires(expires);
			if (facebook.isSessionValid()) {
				Bundle params = new Bundle();
				params.putString("status", status);
				AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(
						facebook);
	            mAsyncRunner.request("me/feed", params, "POST", new WallPostRequestListener(), new WallPostDialogListener());
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						.putExtra("isfacebook", true).putExtra("STATUS", status));
			}
		} else {
			context.startActivity(new Intent(context,
					WebViewClientActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					.putExtra("isfacebook", true).putExtra("STATUS", status));
		}
	}
	
	public class WallPostRequestListener extends BaseRequestListener {

        public void onComplete(final String response) {
            Log.d("Facebook-Example", "Got response: " + response);
        }

		@Override
		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			
		}
    }
	
	public final class WallPostDialogListener implements DialogListener {

		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Toast.makeText(context, "Successfully posted to Facebook", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "Failure posting to Facebook", Toast.LENGTH_LONG).show();
			}
		}

		public void onCancel() {
		}

		@Override
		public void onFacebookError(FacebookError e) {
			
		}

		@Override
		public void onError(DialogError e) {
			
		}
	}
	
	public Profile fetchProfile() {
//		if(!NetworkUtil.isConnected()) {
//			return null;
//		}
		try {
			if(isAuthenticated()) {
				Facebook facebook = new Facebook(Constants.FACEBOOK_AUTH);
				String accessToken = getToken();
				long expires = getExpires();
				if(accessToken != null)
					facebook.setAccessToken(accessToken);
				if(expires != 0)
					facebook.setAccessExpires(expires);
				if(facebook.isSessionValid()) {
					String temp = facebook.request("me");
					Profile profile = new Profile();
					JSONObject me = new JSONObject(temp);
					System.out.println(me);
					if(me.has("name")) {
						profile.name = me.getString("name");
					}
					if(me.has("email")) {
						profile.email = me.getString("email");
					}
					if(me.has("bio")) {
						profile.bio= me.getString("bio");
					}
					SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(AppThrower.getAppThrowerApplicationContext());
					sharedPreferenceManager.save("ProfileName", profile.name);
					saveProfile(profile);
				} else {
					context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
				}
			} else {
				context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void saveProfile(Profile profile) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(context.openFileOutput(Constants.PROFILE, Context.MODE_PRIVATE));
			oos.writeObject(profile);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(AppThrower.getAppThrowerApplicationContext());
		sharedPreferenceManager.save("isProfile", true);
	}
	
	public Profile getProfile() {
		ObjectInputStream ois = null;
		Profile profile = null;
		try {
			ois = new ObjectInputStream(context.openFileInput(Constants.PROFILE));
			profile = (Profile)ois.readObject();
			ois.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return profile;
	}

	
	@Override
	public void addFriend(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getId() {
		return SocialNetwork.SOCIAL_NETWORK_FACEBOOK;
	}

	@Override
	public boolean isAuthenticated() {
		if(getToken() != null)
			return true;
		return false;
	}

	@Override
	public void setTokens(Object... params) {
		FacebookToken token = new FacebookToken();
		token.accessToken = (String) params[0];
		token.id = (String) params[1];
		token.expiresIn = (Long)params[2];
		try {
			ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(NAME, Context.MODE_PRIVATE));
			oos.writeObject(token);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getToken() {
		FacebookToken token = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(context.openFileInput(NAME));
			token = (FacebookToken)ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(token != null) {
			return token.accessToken;
		}
		return null;
	}
	
	public long getExpires() {
		FacebookToken token = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(context.openFileInput(NAME));
			token = (FacebookToken)ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(token != null) {
			return token.expiresIn;
		}
		return 0;
	}
}

class FacebookToken implements Serializable {
	/**
	 * Generated SerialVersionUID.
	 */
	private static final long serialVersionUID = -1358009140296532510L;
	public String accessToken;
	public String id;
	public long expiresIn;
}
