package net.microtrash.slicecam.activity;

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
import android.widget.ImageView;
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
	public void onCompositionLoaded(final Composition composition) {

		progressDialog.dismiss();
		
		final Button btnSend = (Button) findViewById(R.id.activity_photostrip_btn_send);
		btnSend.setVisibility(View.GONE);
		
		ImageEffects.createCompositionImage(this, composition, new OnImageSavedListener() {
			
			@Override
			public void onImageSaved(String lastCompositionPath) {
				btnSend.setVisibility(View.VISIBLE);
				for (String filename : composition.getSlicesFilenames()) {
					String filepath = Static.getSliceFilpath(filename);
					addSliceToContainer(sliceContainer, filepath);
				}
			}
		});

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



	private static void addSliceToContainer(LinearLayout sliceContainer, String filepath) {
		ImageView iv = new ImageView(sliceContainer.getContext());
		Bitmap bmp = BitmapFactory.decodeFile(filepath);
		iv.setImageBitmap(bmp);
		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, sliceContainer.getHeight() / (Static.MAX_STEP + 2));
		//params.weight = 1;
		sliceContainer.addView(iv, params);
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
