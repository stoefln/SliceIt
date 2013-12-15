package net.microtrash.slicecam.view;

import com.parse.ParseObject;

import net.microtrash.slicecam.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PreviewMask extends RelativeLayout {
	private RelativeLayout bottomView;
	private SliceView topView;
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

		
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		topView = (SliceView) findViewById(R.id.view_slice);
		bottomView = (RelativeLayout) findViewById(R.id.mask_bottom);

		double maskHeight = (double) width / getRatio();

		// int coverHeight = (int) ((height - maskHeight) / 2);

		topView.getLayoutParams().height = (int) maskHeight;
		bottomView.getLayoutParams().height = (int) (height - 2 * maskHeight);

		positionOutline(height, width);
	}

	private void positionOutline(int height, int width) {
		outline = (ImageView) findViewById(R.id.outline);
		outline.setVisibility(View.VISIBLE);
		double maskHeight = (double) width / getRatio();
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) outline.getLayoutParams();

		int offset = (int) ((1 - getStep()) * maskHeight);
		params.height = (int) (4 * maskHeight);
		params.bottomMargin = (int) (height - params.height) - offset;
		params.topMargin = offset;
		outline.setLayoutParams(params);
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public void setSlice(ParseObject slice) {
		topView = (SliceView) findViewById(R.id.view_slice);
		topView.setSlice(slice);

	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
		positionOutline(getHeight(), getWidth());
	}



}
