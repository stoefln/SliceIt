package net.microtrash.slicedup.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class ProportionalBitmapView extends android.widget.ImageView {


	private double mAspectRatio = 1;
	

	public ProportionalBitmapView(Context _context) {
		super(_context);

	}

	public ProportionalBitmapView(Context _context, AttributeSet _attrs) {
		super(_context, _attrs);
		init(_attrs);
	}

	public ProportionalBitmapView(Context _context, AttributeSet _attrs, int _defStyle) {
		super(_context, _attrs, _defStyle);
		init(_attrs);
	}

	private void init(AttributeSet _attrs) {

		
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mAspectRatio  = (double) bm.getWidth() / (double) bm.getHeight();
	}

	@SuppressLint("WrongCall")
	@Override
	protected void onMeasure(int _widthMeasureSpec, int _heightMeasureSpec) {
		super.onMeasure(_widthMeasureSpec, _heightMeasureSpec);
		int width = MeasureSpec.getSize(_widthMeasureSpec);
		
		setMeasuredDimension(width, (int) (width / mAspectRatio));

		

	}

}
