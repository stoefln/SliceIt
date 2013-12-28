package net.microtrash.slicecam.view;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.lib.Tools;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FigureView extends View {

	private Bitmap bitmap;
	private Paint paint;
	private Matrix matrix;
	private int step = 0;
	private Paint linePaint;

	private int strokeWidth;

	public FigureView(Context context) {
		super(context);
		init(context);
	}

	public FigureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FigureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		strokeWidth = Tools.dip2Pixels(3, getContext());
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.figure);
		matrix = new Matrix();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		linePaint.setStrokeWidth(strokeWidth);
		linePaint.setColor(Color.WHITE);
		linePaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		float height = (float) width / Static.SLICE_ASPECT_RATIO * (Static.MAX_STEP + 1);
		setMeasuredDimension(width, (int) height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		matrix.reset();

		float s = (float) getWidth() / (float) bitmap.getWidth();
		matrix.postScale(s, s);
		canvas.drawBitmap(bitmap, matrix, paint);
		int windowHeight = getHeight() / (Static.MAX_STEP + 1);
		int top = getStep() * windowHeight;
		canvas.drawRect(strokeWidth / 2, top+ strokeWidth / 2, getWidth() - strokeWidth / 2, top + windowHeight, linePaint);
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
		invalidate();
	}

}
