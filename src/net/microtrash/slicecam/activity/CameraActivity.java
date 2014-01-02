package net.microtrash.slicecam.activity;

import java.util.List;

import net.microtrash.slicecam.BitmapDecodingTask;
import net.microtrash.slicecam.BitmapDecodingTask.BitmapDecodingListener;
import net.microtrash.slicecam.CameraController;
import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.ImageSaver.OnImageSavedListener;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.data.Composition;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.dialog.SendToUserPopup;
import net.microtrash.slicecam.dialog.SendToUserPopup.SendToUserPopupListener;
import net.microtrash.slicecam.lib.DataAccess;
import net.microtrash.slicecam.lib.DataAccess.OnCompositionLoadedListener;
import net.microtrash.slicecam.lib.Tools;
import net.microtrash.slicecam.view.FigureView;
import net.microtrash.slicecam.view.IconButton;
import net.microtrash.slicecam.view.PreviewMask;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
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
		PictureCallback, SendToUserPopupListener {

	public static final String LOGTAG = "VIDEOCAPTURE";

	private static final String TAG = "MainActivity";

	protected static final String TAG_SELECT_USER = "select_user";

	private static final int SLICE_IMAGE_WIDTH = 1000;
	private int sessionNumber;
	private int step;
	private IconButton shootButton;
	private ImageSaver imageSaver;
	private View shutterLayer;
	private CameraController cameraController;
	private SurfaceView cameraView;
	private PreviewMask mask;
	private double ratio = 16d / 9d;

	private Composition currentComposition;


	private ProgressbarPopup progressDialog;

	//private Bitmap freshImage;

	private ParseObject freshSliceObject;

	private Bitmap slicedBitmap;

	private FigureView figureView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_camera);

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
		/*cameraView.postDelayed(new Runnable() {

			@Override
			public void run() {
				cameraController = new CameraController(CameraActivity.this, 16d / 9d, cameraView);
				cameraController.start();
			}
		}, 500);
*/
		cameraController = new CameraController(CameraActivity.this, 16d / 9d, cameraView);
		//cameraController.start();
		
		mask = (PreviewMask) findViewById(R.id.mask);
		mask.setRatio(ratio);

		figureView = (FigureView) findViewById(R.id.activity_camera_fv_figure);
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

		imageSaver = new ImageSaver(this);

		shutterLayer = findViewById(R.id.shutter_layer);

		progressDialog = new ProgressbarPopup(this, shutterLayer);
		if (getIntent().getExtras() == null) {

		} else {

			DataAccess.loadCurrentComposition(getIntent().getExtras().getString(Static.EXTRA_COMPOSITION_ID), new OnCompositionLoadedListener() {
				

				@Override
				public void onCompositionLoaded(Composition composition) {
					currentComposition = composition;
					String filepath = Static.getFullSliceFilepath(currentComposition.getLastSlice());
					Bitmap bmp = BitmapFactory.decodeFile(filepath);
					/*Bitmap blurredSlice = ImageEffects.fastblur(bmp, 100, bmp.getWidth(),
							(int) (bmp.getHeight() * (1d - 0.2)));*/
					mask.setSlice(currentComposition.getLastSlice());
					step = currentComposition.getLastSlice().getInt("step") + 1;
					progressDialog.dismiss();
					mask.setStep(step);
					figureView.setStep(step);
					/*imageSaver.saveImageAsync(bmp, Static.SLICE_DIRECTORY_NAME,
							Static.createSliceFilename(composition.getParseObjectId(), step));*/
					
				}
			});
			progressDialog.show("loading last photo slice...");
		}
	}

	@Override
	protected void onDestroy() {
		if(imageSaver != null){
			
		}
		super.onDestroy();
	}

	private void log(String string) {
		Log.v(TAG, string);

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
		new BitmapDecodingTask(CameraActivity.this).execute(data);
		progressDialog.show("saving image...");
	}

	@Override
	public void onBitmapDecoded(Bitmap bitmap, Exception exception, String message) {

		int sliceWidth = SLICE_IMAGE_WIDTH;
		int sliceHeight = (int) (sliceWidth / ratio);
		//Log.v(TAG, "sliceHeight: " + sliceWidth + " sliceWidth: " + sliceWidth);
		slicedBitmap = Bitmap.createBitmap(sliceWidth, sliceHeight, Bitmap.Config.ARGB_8888);
		Matrix m = new Matrix();
		int topOffset = (int) ((double) bitmap.getHeight() / ratio);
		float s = (float) sliceWidth / (float) bitmap.getHeight();
		Log.v(TAG, "scale: " + s );
		m.postScale(s, s);
		m.postRotate(90, 0, 0);
		m.postTranslate((float) bitmap.getHeight() * s, -  (float) topOffset * s);
		Canvas c = new Canvas(slicedBitmap);
		c.drawColor(getResources().getColor(R.color.test_color));

		c.drawBitmap(bitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		// imageSaver.saveImageAsync(slicedBitmap, "slice_L_" + shotNumber);

		/*double downScaling = 5;
		double visibleOfSlice = 0.2;
		freshImage = Bitmap.createBitmap((int) ((double) sliceWidth / downScaling),
				(int) ((double) sliceHeight / downScaling), Bitmap.Config.ARGB_8888);
		c = new Canvas(freshImage);
		m = new Matrix();

		m.postScale((float) (1d / downScaling), (float) (1d / downScaling));
		c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
*/
		if (currentComposition == null) {
			createNewCompositionAndSaveImageToSd();
		} else {
			saveImageToSd();
		}

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

	}

	private void createNewCompositionAndSaveImageToSd() {
		Log.v(TAG, "creating new composition");
		final ParseObject parseObject = new ParseObject("Composition");
		parseObject.put(Static.FIELD_CREATED_BY, ParseUser.getCurrentUser());
		parseObject.put(Static.FIELD_PLAYER1, ParseUser.getCurrentUser());
		//parseObject.put(Static.FIELD_PLAYER2, ParseUser.getCurrentUser());
		parseObject.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException arg0) {
				currentComposition = new Composition();
				currentComposition.setParseObject(parseObject);
				saveImageToSd();
			}
		});
	}

	protected void saveImageToSd() {
		log("onCompositionSaved(): " + currentComposition.getParseObjectId());
		imageSaver.saveImageAsync(slicedBitmap, "slices",
				Static.createSliceFilename(currentComposition.getParseObjectId(), step), this);
		
	}

	@Override
	public void onImageSaved(String filepath) {
		if (filepath.contains("slice_")) {
			Log.v(TAG, "shotNumber: " + step);
			uploadSliceObject(step, filepath);
			slicedBitmap.recycle();
			slicedBitmap = null;
		}
	}

	private void uploadSliceObject(final int step, String scaledCompositionPath) {

		String[] filepath = scaledCompositionPath.split("/");
		String filename = filepath[filepath.length - 1];
		final ParseFile upload = new ParseFile(filename, Tools.getByteArrayFromFile(scaledCompositionPath));

		freshSliceObject = new ParseObject("Slice");

		final SaveCallback onImageUploaded = new SaveCallback() {

			@Override
			public void done(ParseException ex) {
				if (ex == null) {
					ParseObject composition = currentComposition.getParseObject();

					composition.put(Static.FIELD_LAST_STEP, step);
					if(composition.getParseUser(Static.FIELD_PLAYER1) != ParseUser.getCurrentUser()){
						composition.put(Static.FIELD_PLAYER2, ParseUser.getCurrentUser());
					}

					freshSliceObject.put(Static.FIELD_FILE, upload);
					freshSliceObject.put(Static.FIELD_CREATED_BY, ParseUser.getCurrentUser());
					freshSliceObject.put(Static.FIELD_STEP, step);
					freshSliceObject.put(Static.FIELD_COMPOSITION, composition);

					ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
					acl.setWriteAccess(ParseUser.getCurrentUser(), true);
					acl.setPublicReadAccess(true);
					freshSliceObject.setACL(acl);
					freshSliceObject.saveInBackground(onSliceObjectUploaded);

					Log.v(TAG, "upload done");
				} else {
					String msg = "error occured while uploading: " + ex.getMessage();
					Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_LONG).show();
					Log.v(TAG, msg);
				}
			}
		};

		upload.saveInBackground(onImageUploaded);
		if(currentComposition != null && currentComposition.getLastUser() != null){
			String username = currentComposition.getLastUser().getUsername();
			if (step < Static.MAX_STEP) {
				progressDialog.show("sending your photo slice\nto " + username);
			}else{
				progressDialog.show("generating photostrip");
			}
		}
	}

	final SaveCallback onSliceObjectUploaded = new SaveCallback() {
		@Override
		public void done(final ParseException ex) {
			if (ex == null) {

				Log.v(TAG, "onSliceSaved()");

				final SendToUserPopup sendToUserPopup = new SendToUserPopup(CameraActivity.this,
						findViewById(R.id.activity_camera), currentComposition, freshSliceObject, CameraActivity.this);
				if(currentComposition.getLastUser() == null){
					ParseQuery<ParseUser> query = ParseUser.getQuery();
					query.findInBackground(new FindCallback<ParseUser>() {
						public void done(List<ParseUser> users, ParseException e) {
							if (e == null) {
								Log.v("user", "total: " + users.size());
								progressDialog.dismiss();
								sendToUserPopup.showUserSelection(users);
							} else {

								// Something went wrong -> show "retry"
								// button.
							}
						}
					});
				} else {
					
					if (step >= Static.MAX_STEP) {
						PhotoStripActivity.start(CameraActivity.this, currentComposition.getParseObjectId());
						finish();
					} else {
						sendToUserPopup.sendToUser(currentComposition.getLastUser());
					}

				}

			} else {
				ex.printStackTrace();
			}
		}

	};


	@Override
	public void onNotificationSent(ParseUser user) {
		Toast.makeText(this, "Your photo was sent to "+user.getString(Static.FIELD_USERNAME)+"!", Toast.LENGTH_LONG).show();
		log("Everything was saved. Compositionid: " + currentComposition.getParseObjectId());
		// TODO: update filename of first slice which was shot -> we need the
		// compositionId there
		finish();
	}

	public static void start(String compositionId, Context c) {
		Intent i = new Intent(c, CameraActivity.class);
		if (compositionId != null) {
			i.putExtra(Static.EXTRA_COMPOSITION_ID, compositionId);
		}
		c.startActivity(i);
	}

}