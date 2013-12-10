package net.microtrash.slicecam.task;

import java.util.ArrayList;
import java.util.HashMap;

import net.microtrash.slicecam.ImageSaver;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.AsyncTask;

class ImageSaverTask extends AsyncTask<Object, Integer, Long> {

	private Bitmap bitmap;
	private String filepath;
	private ImageSaver imageSaver;

	public void SaveImageTask(ImageSaver imageSaver, String filepath, Bitmap bitmap){
		this.imageSaver = imageSaver;
		this.filepath = filepath;
		this.bitmap = bitmap;
		
	}
	private String svgFilePath = null;

	protected void onProgressUpdate(Integer... progress) {
	}

	@Override
	protected Long doInBackground(Object... params) {
		
		return null;
	}

	protected void onPostExecute(Long result) {
		
	}
}
