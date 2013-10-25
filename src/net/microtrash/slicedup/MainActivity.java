package net.microtrash.slicedup;

import net.microtrash.slicedup.BitmapDecodingTask.BitmapDecodingListener;
import net.microtrash.slicedup.ImageSaver.OnImageSavedListener;
import net.microtrash.slicedup.view.PreviewMask;
import android.app.Activity;
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

	private Button shootButton;

	private ImageSaver imageSaver;

	private View shutterLayer;

	private CameraController cameraController;

	private SurfaceView cameraView;

	private PreviewMask mask;

	private double ratio = 16d / 9d;

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
		shotNumber = 1;

		shootButton = (Button) findViewById(R.id.bt_shoot);
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
		shutterLayer = findViewById(R.id.shutter_layer);

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
		imageSaver.saveImageAsync(slicedBitmap, "sliceUp_" + shotNumber);
		
		
		double downScaling = 5;
		double visibleOfSlice = 0.1;
		
		Bitmap miniSlice = Bitmap.createBitmap((int) ((double) sliceWidth / downScaling), (int)((double) sliceHeight * visibleOfSlice / downScaling ), Bitmap.Config.ARGB_8888);
		c = new Canvas(miniSlice);
		m = new Matrix();
		m.postTranslate(0, -(float) (sliceHeight*(1-visibleOfSlice)));
		m.postScale((float) (1d/downScaling), (float) (1d/downScaling));
		c.drawBitmap(slicedBitmap, m, new Paint(Paint.ANTI_ALIAS_FLAG));
		imageSaver.saveImageAsync(miniSlice, "sliceUp_" + shotNumber+"_mini");
		mask.addPreImage(miniSlice);
		shotNumber++;
	}

	@Override
	public void onImageSaved(String filepath) {

	}

}