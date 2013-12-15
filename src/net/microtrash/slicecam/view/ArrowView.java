package net.microtrash.slicecam.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class ArrowView extends View{

	private Paint arrowPaint;
	private Path arrowPath;
	
	public ArrowView(Context context) {
		super(context);
		init(context);
	}
	public ArrowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public ArrowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	private void init(Context context) {
		arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		arrowPaint.setColor(Color.BLACK);
		arrowPaint.setStyle(Paint.Style.FILL);
		
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		arrowPath = new Path();
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width / 2;
		setMeasuredDimension(width, height);
		arrowPath.moveTo(0, 0);
		arrowPath.lineTo(width, 0);
		arrowPath.lineTo(width / 2f, height);
		arrowPath.close();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(arrowPath, arrowPaint);
		//canvas.drawRect(0, 0, 200, 200, arrowPaint);
		super.onDraw(canvas);
	}

}
