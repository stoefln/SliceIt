package net.microtrash.slicecam;

import android.graphics.Bitmap;

public class Static {

	public static final String IMAGE_FILE_EXTENSION = "jpg";
	// how many rounds will be played (aka which round will finish the composition)
	public static final int MAX_STEP = 3;
	public static final String EXTRA_COLLABORATOR_USERNAME = "collaborator_username";
	public static String PUSH_DEFAULT_CHANNEL_KEY = "push_default_channel";
	public static String FIELD_SEND_TO_USER = "send_to_user";
	public static String FIELD_COMPOSITION = "composition";
	public static String FIELD_CREATED_BY = "createdBy";
	public static String EXTRA_COMPOSITION_ID = "composition_id";
	public static String FIELD_LAST_STEP = "last_step";
	public static String FIELD_USERNAME = "username";
	public static String FIELD_STEP = "step";
	public static String FIELD_FILE = "file";
	public static String EXTRA_SLICE_FILENAMES = "slice_filenames";
	public static String SLICE_DIRECTORY_NAME = "slices";
	public static String COMPOSITION_DIRECTORY_NAME = "compositions";
	
	public static String createCompositionFilename(String compositionId) {
		return "composition_" + compositionId;
	}
	
	public static String createSliceFilename(String compositionId, int step) {
		String filename = "slice_" + compositionId + "_" + step;
		return filename;
	}
	
	public static String getSliceFilpath(String filename) {
		return Globals.getAppRootDirectoryPath() + "/" + Static.SLICE_DIRECTORY_NAME + "/" + filename;
	}
	
	public static String getCompositionFilpath(String filename) {
		return Globals.getAppRootDirectoryPath() + "/" + Static.COMPOSITION_DIRECTORY_NAME + "/" + filename;
	}
	
}
