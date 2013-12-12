package net.microtrash.slicecam.dialog;

import net.microtrash.slicecam.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class ProgressbarPopup extends PopupWindow {

	public interface OnDialogClosedListener {
		void onDialogClosed(boolean positive);
	}

	private View dialogLayout;
	private View parentView;
	private ImageView spinner;
	private Animation rotateAnim;
	private TextView label;

	public ProgressbarPopup(Context context, View parentView) {
		super(context);

		this.parentView = parentView;
		LayoutInflater inflater = LayoutInflater.from(context);
		dialogLayout = inflater.inflate(R.layout.popup_progressbar, null);
		setContentView(dialogLayout);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setBackgroundDrawable(new ColorDrawable());
		setFocusable(true);

		label = (TextView) dialogLayout.findViewById(R.id.popup_progressbar_tv_label);
		spinner = (ImageView) dialogLayout.findViewById(R.id.popup_progressbar_iv);
		rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate);
		rotateAnim.setRepeatCount(Integer.MAX_VALUE);
		spinner.startAnimation(rotateAnim);
	}

	public ProgressbarPopup(Activity activity) {
		this(activity, activity.findViewById(android.R.id.content));
	}

	@Override
	public void dismiss() {
		try {
			spinner.setAnimation(null);
			super.dismiss();
		} catch (Exception ex) {

		}
	}

	public void show(String string) {
		label.setText(string);
		parentView.post(new Runnable() {
			@Override
			public void run() {
				spinner.startAnimation(rotateAnim);
				if (parentView != null) {
					try {
						showAtLocation(parentView, 0, 0, 0);
					} catch (Exception e) {
						
					}
				}
			}
		});
	}

	public void show() {
		show("");
	}

	public void showDelayed() {
		parentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				show();

			}
		}, 500);

	}

	
	public void showAndDismiss(String message, int durationInMilliseconds, final OnDialogClosedListener listener) {
		show(message);
		parentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				dismiss();
				if(listener != null){
					listener.onDialogClosed(true);
				}
			}
		}, durationInMilliseconds);
		
	}

}
