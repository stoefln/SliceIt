package net.microtrash.slicedup.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicedup.BitmapDecodingTask;
import net.microtrash.slicedup.BitmapDecodingTask.BitmapDecodingListener;
import net.microtrash.slicedup.CameraController;
import net.microtrash.slicedup.ImageSaver;
import net.microtrash.slicedup.ImageSaver.OnImageSavedListener;
import net.microtrash.slicedup.Static;
import net.microtrash.slicedup.dialog.ProgressbarPopup;
import net.microtrash.slicedup.dialog.SelectUserDialog;
import net.microtrash.slicedup.lib.ImageEffects;
import net.microtrash.slicedup.lib.Tools;
import net.microtrash.slicedup.view.IconButton;
import net.microtrash.slicedup.view.PreviewMask;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CameraActivity extends FragmentActivity implements BitmapDecodingListener, OnImageSavedListener,
		PictureCallback {

	public static final String LOGTAG = "VIDEOCAPTURE";

	private static final String TAG = "MainActivity";

	protected static final String TAG_SELECT_USER = "select_user";

	private int sessionNumber;

	private int step;

	private IconButton shootButton;

	private ImageSaver imageSaver, compositionSaver;

	private View shutterLayer;

	private CameraController cameraController;

	private SurfaceView cameraView;

	private PreviewMask mask;

	private double ratio = 16d / 9d;

	private ArrayList<String> imageFilenames;

	private ParseObject currentComposition;

	private ProgressbarPopup progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_main);

		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put("user", ParseUser.getCurrentUser());
		installation.saveInBackground();

		cameraView = (SurfaceView) findViewById(R.id.preview);
		cameraView.setClickable(true);
		cameraView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cameraController.focus();
			}
		});
		mask = (PreviewMask) findViewById(R.id.mask);
		mask.setRatio(ratio);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		sessionNumber = preferences.getInt("session_num", 0);
		sessionNumber++;
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("session_num", sessionNumber);
		editor.commit();
		step = 0;

		shootButton = (IconButton) findViewById(R.id.bt_shoot);
		shootButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				cameraController.shootFoto(CameraActivity.this);
				shutterLayer.setVisibility(View.VISIBLE);
				shutterLayer.postDelayed(new Runnable() {

					@Override
					public void run() {
						shutterLayer.setVisibility(View.GONE);

					}
				}, 100);
			}

		});
		cameraController = new CameraController(this, 16d / 9d, cameraView);
		imageSaver = new ImageSaver(this, this);
		compositionSaver = new ImageSaver(this, this);
		shutterLayer = findViewById(R.id.shutter_layer);
		imageFilenames = new ArrayList<String>();

		progressDialog = new ProgressbarPopup(this, shutterLayer);
		if(getIntent().getExtras() != null){
			loadCurrentComposition(getIntent().getExtras().getString(Static.EXTRA_COMPOSITION_ID));
			progressDialog.show();
		}
	}
	
	

	private void loadCurrentComposition(String compositionId) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Composition");
		query.whereEqualTo("objectId", compositionId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> compositions, ParseException e) {
				if (e == null && compositions.size() > 0) {
					Log.d("score", "Loaded " + compositions.size() + " composition");
					currentComposition = compositions.get(0);
					step = currentComposition.getInt(Static.FIELD_LAST_STEP);
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
				progressDialog.dismiss();
				mask.setStep(step);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	}

	private void message(String string) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		message("shot photo!");
		new BitmapDecodingTask(CameraActivity.this).execute(data);

	}

	@Override
	public void onBitmapDecoded(Bitmap bitmap, Exception exception, String message) {

		int sliceWidth = bitmap.getHeight();
		int sliceHeight = (int) (sliceWidth / ratio);
		Log.v(TAG, "sliceHeight: " + sliceWidth + " sliceWidth: " + sliceWidth);
		Bitmap slicedBitmap = Bitmap.createBitmap(sliceWidth, sliceHeight, Bitmap.Config.ARGB_8888);
		Matrix m = new Matrix();
		int topOffset = (bitmap.getWidth() - sliceHeight) / 2;

		m.postRotate(90, 0, 0);
		m.postTranslate(sliceWidth, -topOffset);
		Canvas c = new Canvas(slicedBitmap);
		c.drawColor(getResources().getColor(R.color.test_color));

		c.drawBitmap(bitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		// imageSaver.saveImageAsync(slicedBitmap, "slice_L_" + shotNumber);

		double downScaling = 5;
		double visibleOfSlice = 0.2;
		Bitmap miniSlice = Bitmap.createBitmap((int) ((double) sliceWidth / downScaling),
				(int) ((double) sliceHeight / downScaling), Bitmap.Config.ARGB_8888);
		c = new Canvas(miniSlice);
		m = new Matrix();

		m.postScale((float) (1d / downScaling), (float) (1d / downScaling));
		c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		imageSaver.saveImageAsync(miniSlice, "slices", "slice_S_" + step);

		/*
		 * Bitmap miniSlice = Bitmap.createBitmap((int) ((double) sliceWidth /
		 * downScaling), (int)((double) sliceHeight * visibleOfSlice /
		 * downScaling ), Bitmap.Config.ARGB_8888); c = new Canvas(miniSlice); m
		 * = new Matrix(); m.postTranslate(0, -(float)
		 * (sliceHeight*(1-visibleOfSlice))); m.postScale((float)
		 * (1d/downScaling), (float) (1d/downScaling));
		 * c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		 * imageSaver.saveImageAsync(miniSlice, "sliceUp_" +
		 * shotNumber+"_mini");
		 */
		Bitmap blurredSlice = ImageEffects.fastblur(miniSlice, 100, miniSlice.getWidth(),
				(int) (miniSlice.getHeight() * (1d - visibleOfSlice)));
		mask.addPreImage(blurredSlice);

	}

	@Override
	public void onImageSaved(String filepath) {
		if (filepath.contains("slice_S_")) {
			imageFilenames.add(filepath);
			step++;
			mask.setStep(step);
			Log.v(TAG, "shotNumber: " + step);
			uploadSlice(step, filepath);
			if (imageFilenames.size() >= 4) {
				Bitmap composition = ImageEffects.createComposition(this, imageFilenames);
				imageSaver.saveImageAsync(composition, "compositions", "composition_" + step);
				imageFilenames = new ArrayList<String>();

			}
		} else if (filepath.contains("composition_")) {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filepath)), "image/png");
			startActivity(intent);
		}

	}

	private void uploadSlice(final int step, String scaledCompositionPath) {

		String[] filepath = scaledCompositionPath.split("/");
		String filename = filepath[filepath.length - 1];
		final ParseFile upload = new ParseFile(filename, Tools.getByteArrayFromFile(scaledCompositionPath));

		final ParseObject slice = new ParseObject("Slice");

		final SaveCallback onSliceSaved = new SaveCallback() {
			@Override
			public void done(ParseException ex) {
				if (ex == null) {
					Toast.makeText(CameraActivity.this, "uploaded!", Toast.LENGTH_LONG).show();
					Log.v(TAG, "uploaded!");
					SelectUserDialog.newInstance(slice.getObjectId())
							.show(getSupportFragmentManager(), TAG_SELECT_USER);
				}
			}

		};

		final SaveCallback onImageUploaded = new SaveCallback() {

			private ParseObject composition;

			@Override
			public void done(ParseException ex) {
				if (ex == null) {
					composition = getCurrentComposition();
					
					composition.put(Static.FIELD_LAST_STEP, step);
					
					slice.put("file", upload);
					slice.put("createdBy", ParseUser.getCurrentUser().getUsername());
					slice.put("step", step);
					slice.put(Static.FIELD_COMPOSITION, composition);

					ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
					acl.setWriteAccess(ParseUser.getCurrentUser(), true);
					acl.setPublicReadAccess(true);
					slice.setACL(acl);
					slice.saveInBackground(onSliceSaved);

					Log.v(TAG, "upload done");
				} else {
					String msg = "error occured while uploading: " + ex.getMessage();
					Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_LONG).show();
					Log.v(TAG, msg);
				}
			}
		};

		upload.saveInBackground(onImageUploaded);
	}

	private ParseObject getCurrentComposition() {
		if (currentComposition == null) {
			Log.v(TAG, "creating new composition");
			currentComposition = new ParseObject("Composition");
			currentComposition.put("createdBy", ParseUser.getCurrentUser().getUsername());
		} else {
			Log.v(TAG, "continuing composition " + currentComposition.getObjectId());
		}
		return currentComposition;
	}

}