package net.microtrash.slicecam.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

public class TextButton extends BubbleText {

	public TextButton(Context context) {
		super(context);
	}

	public TextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		setOnTouchListener(onTouchListener);
	}

	public TextButton setOnClickCallback(OnClickListener l) {
		super.setOnClickListener(l);
		return this;
	}

	private OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// Log.v("Button","onTouch()");
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				OvershootInterpolator inter = new OvershootInterpolator();
				ScaleAnimation anim = new ScaleAnimation(1, (float) 1.5, 1, (float) 1.5, getRadius(), getRadius());
				anim.setInterpolator(inter);
				anim.setDuration(200);
				anim.setFillAfter(true);
				TextButton.this.startAnimation(anim);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				ScaleAnimation anim = new ScaleAnimation((float) 1.5, 1, (float) 1.5, 1, getRadius(), getRadius());
				anim.setDuration(300);
				anim.setFillAfter(true);
				TextButton.this.startAnimation(anim);
			}
			return false;
		}
	};

}
