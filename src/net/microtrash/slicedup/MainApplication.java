package net.microtrash.slicedup;


import net.microtrash.slicedup.activity.DashboardActivity;
import net.microtrash.slicedup.activity.CameraActivity;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class MainApplication extends Application {
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		
		Parse.initialize(this, "SKR868agRvAf4bSBRe8afyM6rG1ZqIR8LV4z9rqQ", "vwWTh7uMPb1t6I2jhKjy3jaZMtVi6dn0FuYR3SY2");
		PushService.setDefaultPushCallback(this, DashboardActivity.class);
		PushService.subscribe(this, Static.PUSH_DEFAULT_CHANNEL_KEY, DashboardActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
		super.onCreate();
	}

	public static void initImageLoader(Context context) {
		
	}
}