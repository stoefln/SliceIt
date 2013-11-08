package net.microtrash.slicecam.lib;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

public class Tools {

	public static String exception2String(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static int dip2Pixels(int dip, Context context) {
		Resources r = context.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return px;
	}

	public static String getRealPathFromURI(Uri contentUri, Activity activity) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static void restartActivity(Activity activity) {
		Intent intent = activity.getIntent();
		activity.finish();
		activity.startActivity(intent);

	}

	public static void setPreference(String key, String value, Context context) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getPreference(String key, String defaultValue, Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(key, defaultValue);
	}

	public static void printFreeRam(String tag, Context c) {
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);

		final long mb = 1024L * 1024L;
		Log.v(tag, String.format(
				"availMem: %d, freeMemory:%d, maxMemory:%d, totalMemory:%d, mativeHeapAllocatedSize:%d, heapPad:%d",
				mi.availMem / mb, Runtime.getRuntime().freeMemory() / mb, Runtime.getRuntime().maxMemory() / mb,
				Runtime.getRuntime().totalMemory() / mb, Debug.getNativeHeapAllocatedSize() / mb, Tools.getHeapPad()
						/ mb));
		// log(String.format("sys free ram: %d, gc free:%d max:%d used:%d",
		// availableMegs, gcfree / mb, gcmax / mb, gcused / mb));
	}

	/**
	 * Checks if a bitmap with the specified size fits in memory
	 * 
	 * @param bmpwidth
	 *            Bitmap width
	 * @param bmpheight
	 *            Bitmap height
	 * @param bmpdensity
	 *            Bitmap bpp (use 2 as default)
	 * @return true if the bitmap fits in memory false otherwise
	 */
	public static boolean checkBitmapFitsInMemory(long bmpwidth, long bmpheight, int bmpdensity) {
		long required = bmpwidth * bmpheight * bmpdensity;
		long allocated = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
				+ Debug.getNativeHeapAllocatedSize() + Tools.getHeapPad();

		final long mb = 1024L * 1024L;
		Log.v("memdebug", "format: " + bmpwidth + "x" + bmpheight + " density:" + bmpdensity);
		Log.v("memdebug", String.format("allocated: %d", allocated / mb));
		Log.v("memdebug", String.format("required:  %d", required / mb));
		Log.v("memdebug", String.format("sum:  %d", (required + allocated) / mb));
		Log.v("memdebug", String.format("max:  %d", Runtime.getRuntime().maxMemory() / mb));
		if ((required + allocated) >= Runtime.getRuntime().maxMemory()) {
			Log.v("memdebug", "no");
			return false;
		}
		Log.v("memdebug", "yes");
		return true;
	}

	public static long getHeapPad() {
		return (long) Math.max(4 * 1024 * 1024, Runtime.getRuntime().maxMemory() * 0.1);
	}

	/**
	 * find how much we have to downsample the image so it fits into memory
	 * 
	 * @param width
	 * @param height
	 * @param layers
	 * @return
	 */
	public static double getDownscalingFactor(int width, int height, int layers) {
		int downscalingFactor;
		for (downscalingFactor = 1; downscalingFactor < 16; downscalingFactor++) {
			double w = (double) width / downscalingFactor;
			double h = (double) height / downscalingFactor;
			if (Tools.checkBitmapFitsInMemory((int) w, (int) h, 4 * layers)) { // 4
																				// channels
																				// (RGBA)
																				// *
																				// amount
																				// of
																				// layers

				break;
			} else {

			}
		}
		// don't allow that high resolutions- it makes the app slow
		if (width > 3400 && downscalingFactor == 1) {
			downscalingFactor = 2;
		}
		return downscalingFactor;
	}

	/**
	 * we have to downsample big images, otherwise we will get an
	 * OutOfMemoryException so lets look how big the image is, which we are
	 * going to load. after that do some calculation and downsample the image
	 * while loading
	 * 
	 * @param loadImageUri
	 * @param activity
	 * @param workingImageWidth
	 * @param workingImageHeight
	 * @return
	 */
	public static Bitmap loadImage(Uri loadImageUri, Activity activity, int workingImageWidth, int workingImageHeight) {

		InputStream in;
		try {
			in = activity.getContentResolver().openInputStream(loadImageUri);
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] bMapArray = new byte[buf.available()];
			buf.read(bMapArray);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(Tools.getRealPathFromURI(loadImageUri, activity), options);

			int bytesPerLayer = workingImageWidth * workingImageHeight * 4;
			int imageSize = options.outHeight * options.outWidth * 4;
			int downSample = (int) ((float) imageSize / (float) bytesPerLayer);

			options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			if (downSample <= 0) {
				downSample = 1;
			}
			options.inSampleSize = downSample;

			return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length, options);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static byte[] getByteArrayFromFile(String filePath) {
		File file = new File(filePath);
		int size = (int) file.length();
		byte[] bytes = new byte[size];
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * checks whether the source image is not too big if the source image width
	 * acceeds the maxWidth parameter, it creates a temp image with
	 * width=maxWidth and returns the filepath of it
	 * 
	 * @param lastCompositionPath
	 * @param activity
	 * @return
	 */
	public static String getUploadImage(String lastCompositionPath, Activity activity, int maxWidth) {

		try {
			String filename = lastCompositionPath;
			InputStream in;
			in = activity.getContentResolver().openInputStream(Uri.fromFile(new File(lastCompositionPath)));
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] bMapArray = new byte[buf.available()];
			buf.read(bMapArray);
			BitmapFactory.Options options = new BitmapFactory.Options();
			BitmapFactory.decodeFile(lastCompositionPath, options);
			Bitmap image = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length, options);

			if (image.getWidth() > maxWidth) {
				float ratio = (float) image.getWidth() / (float) image.getHeight();
				int scaledHeight = (int) ((float) maxWidth / ratio);
				Bitmap result = Bitmap.createBitmap(maxWidth, scaledHeight, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(result);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				c.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()),
						new Rect(0, 0, result.getWidth(), result.getHeight()), paint);
				String dir = "/sdcard/cutoutcam/temp/upload/";
				File imageDirectory = new File(dir);
				imageDirectory.mkdirs();
				filename = String.format(dir + "cutoutCam_%d.jpg", System.currentTimeMillis());
				OutputStream stream = new FileOutputStream(filename);
				result.compress(CompressFormat.JPEG, 70, stream);
				result.recycle();
				result = null;
				System.gc();
			}

			return filename;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/** Read the object from Base64 string. */
	public static Object deserialize(String s) throws IOException, ClassNotFoundException {
		if (s.equals("")) {
			return null;
		}
		byte[] data = Base64.decodeFast(s.getBytes());
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	public static String serialize(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.encodeToString(baos.toByteArray(), false);
	}

	public static String stacktraceToString(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		String s = writer.toString();
		return s;
	}

}
