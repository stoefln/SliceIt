package net.microtrash.slicedup.view;

import net.microtrash.slicedup.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class PreviewMask extends ViewGroup {
	private LinearLayout bottomView;
	private View topView;
	private double ratio = 16d / 9d;

	public PreviewMask(Context context) {
		super(context);
		init(context);
	}

	public PreviewMask(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PreviewMask(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		topView = inflater.inflate(R.layout.top_view, null, false);
		
		//topView = new LinearLayout(context);
		//topView.setBackgroundColor(getResources().getColor(R.color.gui_main_color));
		
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
		
				 
		addView(topView, params);
		
		// topView.setBackgroundColor(getResources().getColor(R.color.test_color));

		// topView.addView(scrollView, LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT);
		// addView(topView);
		/*
		 * TextView test = new TextView(getContext()); test.setText("xxxxxxx");
		 * params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		 * LayoutParams.WRAP_CONTENT); test.setLayoutParams(params);
		 * topView.addView(test);
		 */
		bottomView = new LinearLayout(context);
		bottomView.setBackgroundColor(getResources().getColor(R.color.gui_main_color));
		addView(bottomView);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		double maskHeight = (double) getWidth() / getRatio();

		int coverHeight = (int) ((getHeight() - maskHeight) / 2);

		topView.layout(0, 0, getWidth(), coverHeight);
		
		bottomView.layout(0, (int) (coverHeight + maskHeight), getWidth(), getHeight());
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public void addPreImage(Bitmap bitmap) {
		ImageView image = new ImageView(getContext());
		image.setImageBitmap(bitmap);
		LayoutParams params = new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
		image.setLayoutParams(params);
		// topView.addView(image);
		// scrollView.invalidate();

		// topView.addView(test);
	}

}
