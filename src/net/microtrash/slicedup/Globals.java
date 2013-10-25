package net.microtrash.slicedup;

import android.os.Environment;

public class Globals {

	public static final String APP_FOLDERNAME = "SlicedUp";

	public static String getAppRootDirectoryPath() {
		return Environment.getExternalStorageDirectory().getPath() + "/" + Globals.APP_FOLDERNAME + "/";
	}
}
