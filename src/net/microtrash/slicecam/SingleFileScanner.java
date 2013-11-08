/*******************************************************************************
 * Copyright (c) 2011, 2012 Stephan Petzl
 * All rights reserved.
 *******************************************************************************/
package net.microtrash.slicecam;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class SingleFileScanner implements MediaScannerConnectionClient {

	public interface SingleFileScannerCallback {
		public void onScanCompleted(String path, Uri uri);
	}
	
	private MediaScannerConnection mMs;
	private File mFile;
	private SingleFileScannerCallback callback = null;

	public SingleFileScanner(Context context, String filePath, SingleFileScannerCallback callback) {
		this.callback = callback;

		mFile = new File(filePath);
		mMs = new MediaScannerConnection(context, this);
		mMs.connect();
	}

	public SingleFileScanner(Context context, String filePath) {

		mFile = new File(filePath);
		mMs = new MediaScannerConnection(context, this);
		mMs.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		mMs.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		mMs.disconnect();
		if (callback != null) {
			callback.onScanCompleted(path, uri);
		}
	}

}