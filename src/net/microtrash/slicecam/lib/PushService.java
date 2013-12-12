package net.microtrash.slicecam.lib;

import net.microtrash.slicecam.Static;
import android.util.Log;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class PushService {

	private static final String TAG = "PushService";

	public static void sendPushMessage(String receiversUsername, String message, SendCallback sendCallback) {
		ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
		userQuery.whereEqualTo("username", receiversUsername);
		Log.v(TAG, "Send push to " + receiversUsername);

		// Find devices associated with these users
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereMatchesQuery("user", userQuery);
		pushQuery.whereEqualTo("deviceType", "android");
		pushQuery.whereEqualTo("channels", Static.PUSH_DEFAULT_CHANNEL_KEY);
		// Send push notification to query
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery);
		push.setMessage(message);
		push.sendInBackground(sendCallback);
		
	}

}
