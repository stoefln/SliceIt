package net.microtrash.slicecam;

public class Static {

	public static final String IMAGE_FILE_EXTENSION = "jpg";
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
	public static String createSliceFilename(String compositionId, int step) {
		String filename = "slice_" + compositionId + "_" + step;
		return filename;
	}
}
