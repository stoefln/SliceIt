package net.microtrash.slicedup.dialog;

import net.microtrash.slicecam.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;

@SuppressLint("ViewConstructor")
public class ProgressbarPopup extends PopupWindow {

	public interface OnDialogClosedListener {
		void onDialogClosed(boolean positive);
	}

	private View dialogLayout;
	private View parentView;
	private ImageView spinner;
	private Animation rotateAnim;

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

		spinner = (ImageView) dialogLayout.findViewById(R.id.popup_progressbar_iv);
		rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate);
		rotateAnim.setRepeatCount(Integer.MAX_VALUE);
		spinner.startAnimation(rotateAnim);
	}

	@Override
	public void dismiss() {
		spinner.setAnimation(null);
		super.dismiss();
	}

	public void show() {
		parentView.post(new Runnable() {
			@Override
			public void run() {
				spinner.startAnimation(rotateAnim);
				showAtLocation(parentView, 0, 0, 0);
			}
		});

	}

	public void showDelayed() {
		parentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				show();

			}
		}, 500);

	}
}
