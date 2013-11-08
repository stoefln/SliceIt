package net.microtrash.slicecam;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Debug;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraController implements Callback {

	private static final String TAG = "CameraController";
	private Camera camera;
	private int fullWidth, fullHeight;
	private Size bestPictureSize = null;
	private Size bestPreviewSize = null;
	private String allResolutions;
	private double previewTargetRatio;
	private double downscalingFactor = 1;
	private int layers = 1;
	private SurfaceHolder holder;
	private SurfaceView surfaceView;
	private Context context;

	public CameraController(Context context, double previewTargetRatio, SurfaceView cameraView) {
		this.previewTargetRatio = previewTargetRatio;
		this.context = context;
		surfaceView = cameraView;
		holder = cameraView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public Size getBestPictureSize() {
		if (this.bestPictureSize == null) {
			this.calculateOptimalPictureAndPreviewSizes(previewTargetRatio);
		}
		return bestPictureSize;
	}

	public Size getBestPreviewSize() {
		if (this.bestPreviewSize == null) {
			this.calculateOptimalPictureAndPreviewSizes(previewTargetRatio);
		}
		return bestPreviewSize;
	}

	Hashtable<Double, Size> bestPictureSizes = new Hashtable<Double, Size>();
	Hashtable<Double, Size> bestPreviewSizes = new Hashtable<Double, Size>();

	private Size highestCameraSize;
	private boolean previewRunning;
	private PictureCallback takePhotoCallback;

	private void setCameraPreviewSize() {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.getPreviewSize() != this.getBestPreviewSize()) {
			parameters.setPreviewSize(this.getBestPreviewSize().width, this.getBestPreviewSize().height);
			camera.setParameters(parameters);
		}
	}

	private void setCameraPictureSize() {
		Camera.Parameters parameters = this.camera.getParameters();
		if (parameters.getPictureSize() != this.getBestPictureSize()) {
			parameters.setPictureSize(getBestPictureSize().width, getBestPictureSize().height);
			camera.setParameters(parameters);
		}
	}

	private void calculateOptimalPictureAndPreviewSizes(double targetRatio) {

		/*
		 * double fullWidth = (double) cameraActivity.display.getWidth(); double
		 * fullHeight = (double) cameraActivity.display.getHeight();
		 */

		Log.v(TAG, "calculateOptimalPictureAndPreviewSizes() width targetRatio: " + previewTargetRatio + " fullWidth:"
				+ fullWidth + " fullHeight:" + fullHeight);

		if (bestPreviewSize == null) {
			allResolutions = "";
			List<Size> pictureSizes = this.camera.getParameters().getSupportedPictureSizes();
			List<Size> previewSizes = this.camera.getParameters().getSupportedPreviewSizes();
			Collections.sort(pictureSizes, new Comparator<Size>() {
				public int compare(Size s1, Size s2) {
					return s2.width - s1.width;
				}
			});

			highestCameraSize = pictureSizes.get(0);
			Collections.sort(previewSizes, new Comparator<Size>() {
				public int compare(Size s1, Size s2) {
					return s2.width - s1.width;
				}
			});

			allResolutions += "picture sizes:\n";
			for (Size size : pictureSizes) {
				allResolutions += String.valueOf(size.width) + 'x' + String.valueOf(size.height) + ", ratio: "
						+ ((double) size.width / (double) size.height) + ";\n";
			}

			allResolutions += "preview sizes:\n";
			for (Size size : previewSizes) {
				allResolutions += String.valueOf(size.width) + 'x' + String.valueOf(size.height) + ", ratio: "
						+ ((double) size.width / (double) size.height) + ";\n";
			}
			Log.v(TAG, "allResolutions: \n" + allResolutions);
			double bestRatio = 0;
			boolean matchingFinished = false;
			Log.v(TAG, "start matching picture and preview size...");
			for (Size pictureSize : pictureSizes) {
				double pictureRatio = (double) pictureSize.width / (double) pictureSize.height;
				Log.v(TAG, "size: " + pictureSize.width + "x" + pictureSize.height + " " + pictureRatio);
				double previewRatio;
				for (Size previewSize : previewSizes) {
					previewRatio = (double) previewSize.width / (double) previewSize.height;
					if (previewRatio == pictureRatio) {

						if (bestPreviewSize == null) {
							bestPreviewSize = previewSize;
							bestPictureSize = pictureSize;
							bestRatio = pictureRatio;
							Log.v(TAG, "found picture size:" + bestPictureSize.width + "x" + bestPictureSize.height
									+ " ratio: " + ((double) bestPictureSize.width / (double) bestPictureSize.height));
							Log.v(TAG, "...continue searching...");
							break;
						} else {
							Log.v(TAG, "  pixels: " + pictureSize.width * pictureSize.height);
							Log.v(TAG, "  thresh: " + (double) bestPictureSize.width * (double) bestPictureSize.height
									* 0.75D);
							if (Math.abs(targetRatio - bestRatio) > Math.abs(targetRatio - pictureRatio)
									&& pictureSize.width * pictureSize.height >= (double) bestPictureSize.width
											* (double) bestPictureSize.height * 0.75D) {
								bestPreviewSize = previewSize;
								bestPictureSize = pictureSize;
								bestRatio = pictureRatio;
								matchingFinished = true;
								Log.v(TAG, "found even better match:" + bestPictureSize.width + "x"
										+ bestPictureSize.height + " ratio: "
										+ ((double) bestPictureSize.width / (double) bestPictureSize.height));
							}
						}
					}
				}
				if (matchingFinished) {
					break;
				}
			}

			if (bestPreviewSize == null) {
				bestPictureSize = pictureSizes.get(0);
				bestPreviewSize = previewSizes.get(0);
				Log.v(TAG, "no match found!");
			}

			downscalingFactor = getDownscalingFactor(bestPictureSize.width, bestPictureSize.height, layers);

			Log.v(TAG, "choosen picture size:" + bestPictureSize.width + "x" + bestPictureSize.height + " ratio: "
					+ ((double) bestPictureSize.width / (double) bestPictureSize.height));
			Log.v(TAG, "choosen preview size:" + bestPreviewSize.width + "x" + bestPreviewSize.height + " ratio: "
					+ ((double) bestPreviewSize.width / (double) bestPreviewSize.height));
			Log.v(TAG, "choosen downScalingFactor: " + downscalingFactor);

		}

	}

	public void releaseCamera() {
		Log.d(TAG, "releaseCamera()");
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

	public double getDownscalingFactor() {
		return downscalingFactor;
	}

	public double getWorkingImageWidth() {
		return getBestPictureSize().width / getDownscalingFactor();
	}

	public double getWorkingImageHeight() {
		return getBestPictureSize().height / getDownscalingFactor();
	}

	public Size getHighestCameraSize() {
		return highestCameraSize;
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
	private static boolean checkBitmapFitsInMemory(long bmpwidth, long bmpheight, int bmpdensity) {
		long required = bmpwidth * bmpheight * bmpdensity;
		long allocated = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
				+ Debug.getNativeHeapAllocatedSize() + getHeapPad();

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

	private static long getHeapPad() {
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
	private static double getDownscalingFactor(int width, int height, int layers) {
		int downscalingFactor;
		for (downscalingFactor = 1; downscalingFactor < 16; downscalingFactor++) {
			double w = (double) width / downscalingFactor;
			double h = (double) height / downscalingFactor;
			// 4 channels (RGBA) * amount of layers
			if (checkBitmapFitsInMemory((int) w, (int) h, 4 * layers)) {
				break;
			} else {

			}
		}

		return downscalingFactor;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();

		try {
			camera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			setupAndStartPreview();

		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

	}

	private void log(String string) {
		Log.v(TAG, string);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v(TAG, "surfaceChanged");

		if (previewRunning) {
			camera.stopPreview();
		}

		try {
			setupAndStartPreview();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

	}

	private void setupAndStartPreview() throws IOException {
		double ratio = (double) getBestPreviewSize().width / (double) getBestPreviewSize().height;
		Configuration cfg = context.getResources().getConfiguration();
		int width = surfaceView.getWidth();
		int height = (int) ((double) width * ratio);
		if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
			camera.setDisplayOrientation(90);
		}

		// surfaceView.getLayoutParams().height = (int) ((double)
		// surfaceView.getWidth() / ratio);

		log("Setting up surfaceView with targetRatio " + ratio + ", width" + width + ", height: " + height);
		surfaceView.getLayoutParams().height = height;
		surfaceView.getLayoutParams().width = width;
		setCameraPreviewSize();
		setCameraPictureSize();
		camera.setPreviewDisplay(holder);
		camera.startPreview();
		previewRunning = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(TAG, "surfaceDestroyed");

		if (camera != null) {
			previewRunning = false;
			// camera.lock();
			camera.release();
		}

	}

	public void shootFoto(PictureCallback takePhotoCallback) {
		this.takePhotoCallback = takePhotoCallback;
		if (camera != null) {
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}

	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		// never worked in android
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	PictureCallback jpegCallback = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(takePhotoCallback != null){
				camera.startPreview();
				takePhotoCallback.onPictureTaken(data, camera);
			}
		}
	};
	
	public void focus() {
		if (camera != null) {
			try {
				camera.autoFocus(new AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {

					}
				});
			} catch (Exception e) {
			}
		}

	}

}
