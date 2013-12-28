package net.microtrash.slicecam.activity;

import java.util.ArrayList;

import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.ImageSaver.OnImageSavedListener;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.data.Composition;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.dialog.ProgressbarPopup.OnDialogClosedListener;
import net.microtrash.slicecam.lib.DataAccess;
import net.microtrash.slicecam.lib.DataAccess.OnCompositionLoadedListener;
import net.microtrash.slicecam.lib.ImageEffects;
import net.microtrash.slicecam.lib.PushService;
import net.microtrash.slicecam.view.ProportionalBitmapView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class PhotoStripActivity extends Activity implements OnImageSavedListener, OnCompositionLoadedListener {

	private ProgressbarPopup progressDialog;
	private String compositionId;

	private LinearLayout sliceContainer;
	private ImageSaver imageSaver;

	public static void start(Context context, String compositionId) {
		Intent intent = new Intent(context, PhotoStripActivity.class);

		intent.putExtra(Static.EXTRA_COMPOSITION_ID, compositionId);
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
		if (compositionId != null) {
			progressDialog.show("Loading photostrip");
			DataAccess.loadCurrentComposition(compositionId, this);
		}
	}

	@Override
	public void onCompositionLoaded(Composition composition) {

		progressDialog.dismiss();
		
		ArrayList<String> allFilepaths = addImageViewsToContainer(sliceContainer, composition.getSlicesFilenames());

		Bitmap bmpComposition = ImageEffects.createComposition(this, allFilepaths, 1);
		imageSaver = new ImageSaver(this);
		imageSaver
				.saveImageAsync(bmpComposition, "compositions", Static.createCompositionFilename(compositionId), this);

		Button btnSend = (Button) findViewById(R.id.activity_photostrip_btn_send);
		final String collaborator = composition.getLastUser().getString(Static.FIELD_USERNAME);
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

	public static ArrayList<String> addImageViewsToContainer(LinearLayout sliceContainer, ArrayList<String> allFilenames) {
		ArrayList<String> allFilepaths = new ArrayList<String>();
		for (String filename : allFilenames) {
			String filepath = Static.getSliceFilpath(filename);
			allFilepaths.add(filepath);
			ProportionalBitmapView iv = new ProportionalBitmapView(sliceContainer.getContext());
			Bitmap bmp = BitmapFactory.decodeFile(filepath);
			iv.setImageBitmap(bmp);
			android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					0);
			params.weight = 1;
			sliceContainer.addView(iv, params);
		}
		return allFilepaths;
	}

	@Override
	public void onImageSaved(String lastCompositionPath) {

	}

	private void onPushMessageSent() {
		progressDialog.showAndDismiss("Message has been sent", 3000, new OnDialogClosedListener() {
			@Override
			public void onDialogClosed(boolean positive) {
				finish();
			}
		});

	}

}
