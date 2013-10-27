package net.microtrash.slicedup;

import java.io.File;
import java.util.ArrayList;

import net.microtrash.slicedup.BitmapDecodingTask.BitmapDecodingListener;
import net.microtrash.slicedup.ImageSaver.OnImageSavedListener;
import net.microtrash.slicedup.lib.ImageEffects;
import net.microtrash.slicedup.view.IconButton;
import net.microtrash.slicedup.view.PreviewMask;
import android.app.Activity;
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
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements BitmapDecodingListener, OnImageSavedListener, PictureCallback {

	public static final String LOGTAG = "VIDEOCAPTURE";

	private static final String TAG = "MainActivity";

	private int sessionNumber;

	private int shotNumber;

	private IconButton shootButton;

	private ImageSaver imageSaver, compositionSaver;

	private View shutterLayer;

	private CameraController cameraController;

	private SurfaceView cameraView;

	private PreviewMask mask;

	private double ratio = 16d / 9d;

	private ArrayList<String> imageFilenames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_main);

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
		shotNumber = 0;

		shootButton = (IconButton) findViewById(R.id.bt_shoot);
		shootButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				cameraController.shootFoto(MainActivity.this);
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
		new BitmapDecodingTask(MainActivity.this).execute(data);

	}

	@Override
	public void onBitmapDecoded(Bitmap bitmap, Exception exception, String message) {
		
		int sliceWidth = bitmap.getHeight();
		int sliceHeight = (int) (sliceWidth / ratio);
		Log.v(TAG, "sliceHeight: "+sliceWidth+" sliceWidth: "+sliceWidth);
		Bitmap slicedBitmap = Bitmap.createBitmap(sliceWidth, sliceHeight, Bitmap.Config.ARGB_8888);
		Matrix m = new Matrix();
		int topOffset = (bitmap.getWidth() - sliceHeight) / 2;
		
		m.postRotate(90, 0, 0);
		m.postTranslate(sliceWidth, -topOffset);
		Canvas c = new Canvas(slicedBitmap);
		c.drawColor(getResources().getColor(R.color.test_color));
		
		c.drawBitmap(bitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		//imageSaver.saveImageAsync(slicedBitmap, "slice_L_" + shotNumber);
		
		
		double downScaling = 5;
		double visibleOfSlice = 0.2;
		Bitmap miniSlice = Bitmap.createBitmap((int) ((double) sliceWidth / downScaling), (int)((double) sliceHeight / downScaling ), Bitmap.Config.ARGB_8888);
		c = new Canvas(miniSlice);
		m = new Matrix();
		
		m.postScale((float) (1d/downScaling), (float) (1d/downScaling));
		c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		imageSaver.saveImageAsync(miniSlice, "slice_S_" + shotNumber);
		
		/*
		Bitmap miniSlice = Bitmap.createBitmap((int) ((double) sliceWidth / downScaling), (int)((double) sliceHeight * visibleOfSlice / downScaling ), Bitmap.Config.ARGB_8888);
		c = new Canvas(miniSlice);
		m = new Matrix();
		m.postTranslate(0, -(float) (sliceHeight*(1-visibleOfSlice)));
		m.postScale((float) (1d/downScaling), (float) (1d/downScaling));
		c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		imageSaver.saveImageAsync(miniSlice, "sliceUp_" + shotNumber+"_mini");*/
		Bitmap blurredSlice = ImageEffects.fastblur(miniSlice, 100, miniSlice.getWidth(), (int) (miniSlice.getHeight() * (1d - visibleOfSlice)));
		mask.addPreImage(blurredSlice);
		
	}

	

	@Override
	public void onImageSaved(String filepath) {
		if(filepath.contains("slice_S_")){
			imageFilenames.add(filepath);
			shotNumber++;
			mask.setStep(shotNumber);
			Log.v(TAG, "shotNumber: "+shotNumber);
			if(imageFilenames.size() >= 4){
				Bitmap composition = ImageEffects.createComposition(this, imageFilenames);
				imageSaver.saveImageAsync(composition, "composition_" + shotNumber);
				imageFilenames = new ArrayList<String>();
				
			}
		} else if(filepath.contains("composition_")){
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filepath)), "image/png");
			startActivity(intent);
		}
		
	}

}