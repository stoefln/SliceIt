package net.microtrash.slicedup.view;

import net.microtrash.slicedup.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PreviewMask extends RelativeLayout {
	private RelativeLayout bottomView;
	private RelativeLayout topView;
	private double ratio = 16d / 9d;
	private ImageView outline;
	// goes from 0 to 3
	private int step = 0;

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

		// topView = findViewById

		// topView = new LinearLayout(context);
		// topView.setBackgroundColor(getResources().getColor(R.color.gui_main_color));

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
		topView = (RelativeLayout) findViewById(R.id.mask_top);
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		topView = (RelativeLayout) findViewById(R.id.mask_top);
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);

		double maskHeight = (double) width / getRatio();

		// int coverHeight = (int) ((height - maskHeight) / 2);

		topView.getLayoutParams().height = (int) maskHeight;
		bottomView.getLayoutParams().height = (int) (height - 2 * maskHeight);

		positionOutline(height, width);
	}

	private void positionOutline(int height, int width) {
		double maskHeight = (double) width / getRatio();
		outline = (ImageView) findViewById(R.id.outline);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) outline.getLayoutParams();

		int offset = (int) ((1 - getStep()) * maskHeight);
		params.height = (int) (4 * maskHeight);
		params.bottomMargin = (int) (height - params.height) - offset;
		params.topMargin = offset;
		outline.setLayoutParams(params);
	}

	/*
	 * @Override protected void onLayout(boolean changed, int l, int t, int r,
	 * int b) {
	 * 
	 * double maskHeight = (double) getWidth() / getRatio();
	 * 
	 * int coverHeight = (int) ((getHeight() - maskHeight) / 2);
	 * 
	 * topView.layout(0, 0, getWidth(), coverHeight);
	 * 
	 * bottomView.layout(0, (int) (coverHeight + maskHeight), getWidth(),
	 * getHeight()); }
	 */
	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public void addPreImage(Bitmap bitmap) {
		ProportionalBitmapView image = new ProportionalBitmapView(getContext());
		image.setImageBitmap(bitmap);

		topView = (RelativeLayout) findViewById(R.id.mask_top);
		topView.addView(image);
		/*
		 * imageContainer.addView(image, new
		 * LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		 * LayoutParams.WRAP_CONTENT));
		 */

		// topView.addView(image);
		// scrollView.invalidate();

		// topView.addView(test);
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
		positionOutline(getHeight(), getWidth());
	}

}
