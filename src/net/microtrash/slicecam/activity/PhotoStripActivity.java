package net.microtrash.slicecam.activity;

import java.io.File;
import java.util.ArrayList;

import net.microtrash.slicecam.Globals;
import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.ImageSaver.OnImageSavedListener;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.lib.ImageEffects;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PhotoStripActivity extends Activity implements OnImageSavedListener {

	private ProgressbarPopup progressDialog;
	private String compositionId;
	private ArrayList<String> allFilenames;
	
	private LinearLayout sliceContainer;
	private ImageSaver imageSaver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = getLayoutInflater().inflate(R.layout.activity_photostrip, null);
		setContentView(v);

		sliceContainer = (LinearLayout) v.findViewById(R.id.activity_photostrip_ll_composition);
		Button btnSend = (Button) v.findViewById(R.id.activity_photostrip_btn_send);
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			}
		});

		compositionId = getIntent().getStringExtra(Static.EXTRA_COMPOSITION_ID);
		// progressDialog = new ProgressbarPopup(this, v);
		allFilenames = getIntent().getStringArrayListExtra(Static.EXTRA_SLICE_FILENAMES);
		ArrayList<String> allFilepaths = addImageViewsToContainer(sliceContainer, allFilenames);

		Bitmap composition = ImageEffects.createComposition(this, allFilepaths);
		imageSaver = new ImageSaver(this);
		imageSaver.saveImageAsync(composition, "compositions", Static.createCompositionFilename(compositionId), this);
	}

	public static ArrayList<String> addImageViewsToContainer(LinearLayout sliceContainer, ArrayList<String> allFilenames) {
		ArrayList<String> allFilepaths = new ArrayList<String>();
		for (String filename : allFilenames) {

			BitmapFactory.Options options = new BitmapFactory.Options();
			String filepath = Static.getSliceFilpath(filename);
			allFilepaths.add(filepath);
			File file = new File(filepath);
			Log.v("addImageViewsToContainer", "filepath: " + filepath + " exists: " + file.exists());
			if (file.exists()) {
				Bitmap bmpSlice = BitmapFactory.decodeFile(filepath, options);

				ImageView iv = new ImageView(sliceContainer.getContext());
				iv.setImageBitmap(bmpSlice);
				sliceContainer.addView(iv, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
		}
		return allFilepaths;
	}

	private void log(String string) {
		Log.v("PhotoStripActivity", string);

	}

	@Override
	public void onImageSaved(String lastCompositionPath) {

	}

}
