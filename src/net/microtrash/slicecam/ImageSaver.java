package net.microtrash.slicecam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

public class ImageSaver {

	public interface OnImageSavedListener {
		void onImageSaved(String lastCompositionPath);
	}

	private Context context;
	private int imageQuality = 45;
	private String defaultImageFormat = "JPEG";

	
	
	private static final String TAG = "ImageSaver";

	public String getDefaultImageFormat() {
		return defaultImageFormat;
	}

	public void setDefaultImageFormat(String defaultImageFormat) {
		this.defaultImageFormat = defaultImageFormat;
	}

	public int getImageQuality() {
		return imageQuality;
	}

	public void setImageQuality(int imageQuality) {
		this.imageQuality = imageQuality;
	}

	public ImageSaver(Context context) {
		this.context = context;
	}

	public void saveImageAsync(Bitmap bitmap, String directoryName, String imageBaseName) {
		saveImageAsync(bitmap, directoryName, imageBaseName, null);
	}

	public void saveImageAsync(Bitmap bitmap, String directoryName, String imageBaseName, OnImageSavedListener listener) {
		
		SaveCompositionTask saveImageTask = new SaveCompositionTask(directoryName, imageBaseName, listener);
		saveImageTask.execute(bitmap);

	}

	private class SaveCompositionTask extends AsyncTask<Object, Integer, Long> {

		private String lastCompositionPath;
		private String directoryName;
		private String imageBaseName;
		private OnImageSavedListener listener;
		
		public SaveCompositionTask(String directoryName, String imageBaseName, OnImageSavedListener listener) {
			this.imageBaseName = imageBaseName;
			this.directoryName = directoryName;
			this.listener = listener;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		@Override
		protected Long doInBackground(Object... params) {
			Bitmap image = (Bitmap) params[0];
			long currentTime = System.currentTimeMillis();
			String imageFilename = imageBaseName + ".jpg";

			lastCompositionPath = saveImage(directoryName, imageFilename, image, false);
			return null;
		}

		protected void onPostExecute(Long result) {

			if (listener != null) {
				listener.onImageSaved(lastCompositionPath);
			}
		}
	}

	private String saveImage(String directoryName, String filename, Bitmap image, boolean hasTransparency) {

		try {
			String dir = Globals.getAppRootDirectoryPath() + "/" + directoryName + "/";
			File directory = new File(dir);
			directory.mkdirs();
			File noMedia = new File(dir + ".nomedia"); // don't know why, but
														// something created a
														// .nomedia file in my
														// dir. so make shure
														// each time that it's
														// not there
			noMedia.delete();
			OutputStream stream = null;
			Bitmap processedImage = null;

			Log.v(TAG, "saving image without transparency");
			processedImage = image; // sensorImageRotator.rotateBitmap(image);

			if (context instanceof Activity && ((Activity) context).getIntent().getAction() != null
					&& ((Activity) context).getIntent().getAction().equals("android.media.action.IMAGE_CAPTURE")
					&& ((Activity) context).getIntent().getExtras() != null) {
				Uri saveUri = (Uri) ((Activity) context).getIntent().getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
				if (saveUri != null) {
					// Save the bitmap to the specified URI (use a try/catch
					// block)
					stream = context.getContentResolver().openOutputStream(saveUri);
					processedImage.compress(CompressFormat.JPEG, this.imageQuality, stream);
					Log.v(TAG, "returning via reference");
					((Activity) context).setResult(Activity.RESULT_OK);
					stream.flush();
					stream.close();
				} else {
					Log.v(TAG, "returning via putExtra()");
					// If the intent doesn't contain an URI, send the bitmap as
					// a Parcelable
					// (it is a good idea to reduce its size to ~50k pixels
					// before)
					((Activity) context).setResult(Activity.RESULT_OK, new Intent("inline-data").putExtra("data", processedImage));
				}
				((Activity) context).finish();
				return saveUri.toString();
			} else {

				String filePath = dir + filename;
				stream = new FileOutputStream(filePath);
				processedImage.compress(CompressFormat.JPEG, this.imageQuality, stream);

				Log.v(TAG, "image saved to " + filePath);
				stream.flush();
				stream.close();

				processedImage.recycle();
				processedImage = null;
				// TODO: move the filescanner into its own service / thread
				new SingleFileScanner(context, filePath);
				return filePath;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}


}
