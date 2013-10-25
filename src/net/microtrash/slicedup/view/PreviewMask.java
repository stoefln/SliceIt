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
		topView = findViewById(R.id.mask_top);
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		topView = findViewById(R.id.mask_top);
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);

		double maskHeight = (double) width / getRatio();

		int coverHeight = (int) ((height - maskHeight) / 2);

		topView.getLayoutParams().height = coverHeight;
		bottomView.getLayoutParams().height = coverHeight;

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

		LinearLayout imageContainer = (LinearLayout) findViewById(R.id.image_container);
		imageContainer.addView(image, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		// topView.addView(image);
		// scrollView.invalidate();

		// topView.addView(test);
	}

}
