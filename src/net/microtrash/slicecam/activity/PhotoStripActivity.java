package net.microtrash.slicecam.activity;

import java.io.File;
import java.util.ArrayList;

import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.ImageSaver.OnImageSavedListener;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.dialog.ProgressbarPopup.OnDialogClosedListener;
import net.microtrash.slicecam.lib.ImageEffects;
import net.microtrash.slicecam.lib.PushService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class PhotoStripActivity extends Activity implements OnImageSavedListener {

	private ProgressbarPopup progressDialog;
	private String compositionId;
	private ArrayList<String> allFilenames;

	private LinearLayout sliceContainer;
	private ImageSaver imageSaver;
	private String collaborator;

	public static void start(Context context, String compositionId, ArrayList<String> sliceFilenames,
			String collaboratorUsername) {
		Intent intent = new Intent(context, PhotoStripActivity.class);

		intent.putExtra(Static.EXTRA_COMPOSITION_ID, compositionId);
		intent.putExtra(Static.EXTRA_COLLABORATOR_USERNAME, collaboratorUsername);
		intent.putStringArrayListExtra(Static.EXTRA_SLICE_FILENAMES, sliceFilenames);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = getLayoutInflater().inflate(R.layout.activity_photostrip, null);
		setContentView(v);

		progressDialog = new ProgressbarPopup(this);
		sliceContainer = (LinearLayout) v.findViewById(R.id.activity_photostrip_ll_composition);

		compositionId = getIntent().getStringExtra(Static.EXTRA_COMPOSITION_ID);
		allFilenames = getIntent().getStringArrayListExtra(Static.EXTRA_SLICE_FILENAMES);
		collaborator = getIntent().getStringExtra(Static.EXTRA_COLLABORATOR_USERNAME);
		ArrayList<String> allFilepaths = addImageViewsToContainer(sliceContainer, allFilenames);

		Bitmap composition = ImageEffects.createComposition(this, allFilepaths);
		imageSaver = new ImageSaver(this);
		imageSaver.saveImageAsync(composition, "compositions", Static.createCompositionFilename(compositionId), this);

		Button btnSend = (Button) v.findViewById(R.id.activity_photostrip_btn_send);
		btnSend.setText("Send this to " + collaborator);
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				PushService.sendPushMessage(collaborator, ParseUser.getCurrentUser().getUsername()
						+ " sent you the finished photo strip!", new SendCallback() {
					@Override
					public void done(ParseException arg0) {
						onPushMessageSent();
					}

				});
			}
		});
	}

	private void onPushMessageSent() {
		progressDialog.showAndDismiss("Message has been sent", 3000, new OnDialogClosedListener() {

			@Override
			public void onDialogClosed(boolean positive) {
				finish();

			}
		});

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
