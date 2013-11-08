package net.microtrash.slicecam;

import android.os.Environment;

public class Globals {

	public static final String APP_FOLDERNAME = "slicecam";

	public static String getAppRootDirectoryPath() {
		return Environment.getExternalStorageDirectory().getPath() + "/" + Globals.APP_FOLDERNAME + "/";
	}
}
