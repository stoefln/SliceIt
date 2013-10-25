package net.microtrash.slicedup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class BitmapDecodingTask extends AsyncTask<byte[], Void, String> {

	public interface BitmapDecodingListener {
		void onBitmapDecoded(Bitmap bitmap, Exception exception, String message);
	}

	private static final String TAG = "ImageProcessingTask";
	private String message = null;
	private Exception exception = null;
	private BitmapFactory.Options options;
	private BitmapDecodingListener listener;
	private Bitmap photo;

	// private CutoutComposition composition;

	public BitmapDecodingTask(BitmapDecodingListener listener) {
		this.options = new BitmapFactory.Options();
		//options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		this.listener = listener;

	}

	@Override
	protected String doInBackground(byte[]... params) {
		byte[] data = params[0];

		

		System.gc();
		Log.d(TAG, "after gc");

		photo = null;
		// if something goes wrong while creating the image - try lower
		// resolution
		for (int i = 0; i < 6; i++) {
			try {
				Log.v(TAG, "downscalingFactor: " + options.inSampleSize);
				photo = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				break;
			} catch (OutOfMemoryError ex) {
				Log.e(TAG, "outofMemory!");
				options.inSampleSize++;
			}
		}

		data = null;

		
		return "Executed";
	}

	@Override
	protected void onPostExecute(String result) {

		if (listener != null && photo != null) {
			listener.onBitmapDecoded(photo, exception, message);
		}

	}
}