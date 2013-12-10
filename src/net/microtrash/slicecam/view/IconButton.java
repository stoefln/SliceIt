/*******************************************************************************
public CircleButton(Context context) {
	    super(context);
		init(context);
	}

	public CircleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CircleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	} * Copyright (c) 2011, 2012 Stephan Petzl
 * All rights reserved.
 *******************************************************************************/
package net.microtrash.slicecam.view;


import net.microtrash.slicecam.R;
import net.microtrash.slicecam.lib.Tools;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

public class IconButton extends View {

	static final int StateDefault = 0;
	static final int StateFocused = 1;
	static final int StatePressed = 2;

	protected Bitmap mBitmapDefault;

	protected int radius;
	protected int color = 0xff000000;
	private boolean visible = true;
	private int savedLeft, savedTop;
	// indicates whether the button is visible when the menu is rendered
	public boolean visibleInMenu = true;
	protected Paint paint;

	protected Bitmap bitmap;
	private Paint linePaint;
	private Drawable drawable;

	public IconButton setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		return this;
	}

	public int getRadius() {
		return radius;
	}

	public IconButton setRadiusDip(int radius) {
		this.radius = Tools.dip2Pixels(radius, getContext());
		this.setRadius(this.radius);
		return this;
	}

	public IconButton setRadius(int radius) {
		this.radius = radius;
		this.setMinimumWidth(this.getTotalWidth());
		this.setMinimumHeight(this.getTotalHeight());
		return this;
	}

	public int getTotalWidth() {
		return 2 * radius + 2 * padding;
	}

	public int getTotalHeight() {
		return 2 * radius + 2 * padding;
	}

	public IconButton setColor(int color) {
		this.color = color;
		return this;
	}

	public IconButton(Context context) {
		super(context);
		init(context);
	}

	public IconButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
		init(attrs);
	}

	public IconButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
		init(attrs);
	}

	public IconButton(Context context, int id) {
		super(context);
		this.bitmap = BitmapFactory.decodeResource(context.getResources(), id);
		init(context);
	}

	private void init(AttributeSet attrs) {

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IconButton);

		int theColor = a.getColor(R.styleable.IconButton_circleColor, Color.BLACK);
		if (theColor != 0) {
			this.setColor(theColor);
		}

		int theRadius = a.getDimensionPixelSize(R.styleable.IconButton_circleRadius, Tools.dip2Pixels(50, getContext()));
		if (theRadius != 0) {
			this.setRadius(theRadius);
		}
		this.drawable = a.getDrawable(R.styleable.IconButton_icon);
		if(drawable == null){
			// hack: why is this not working?!
			drawable = getResources().getDrawable(R.drawable.camera);
		}
		// this.bitmap = drawableToBitmap(drawable);
		// Don't forget this
		a.recycle();
	}

	private void init(Context context) {
		this.setRadiusDip(30);

		setClickable(true);
		setBackgroundColor(0x00000000);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setColor(0xFFFFFFFF);
		// font1 = Typeface.createFromAsset(context.getAssets(),
		// "fonts/Airstream/Airstream.ttf");
		// font1 = Typeface.createFromAsset(context.getAssets(),
		// "fonts/Droid/droid.otf");
		// font1 = Typeface.createFromAsset(context.getAssets(),
		// "fonts/Bebas/BEBAS___.ttf");
		// font1 = Typeface.createFromAsset(context.getAssets(),
		// "fonts/Roboto/Roboto-Thin.ttf");

		setOnClickListener(onClickListener);
		setOnTouchListener(onTouchListener);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public void setVisibility(int visibility) {
		if (visibility == GONE || visibility == INVISIBLE) {
			this.visible = false;
		} else {
			this.visible = true;
		}
		super.setVisibility(visibility);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(radius * 2, radius * 2);
	}

	protected Matrix matrix = new Matrix();
	protected Canvas canvas;
	/**
	 * draws the normal button (without rollover)
	 * 
	 * @param canvas
	 */

	int padding = 0;
	private Point center;
	protected boolean dirty = false;

	protected Point getCenter() {
		if (center == null) {
			center = new Point(this.radius + padding, this.radius + padding);
		}
		return center;
	}

	protected Bitmap drawDefault() {
		// canvas.drawARGB(255, 255, 0, 0);
		if (this.mBitmapDefault == null) {
			mBitmapDefault = Bitmap.createBitmap(this.getTotalWidth(), this.getTotalHeight(), Config.ARGB_8888);
			canvas = new Canvas(mBitmapDefault);
		}

		paint.setColor(color);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL);

		/*
		 * Paint p = new Paint(); p.setColor(0xFF00FF00);
		 * p.setStyle(Style.FILL); canvas.drawRect(new Rect(0,0,
		 * canvas.getWidth(), canvas.getHeight()), p);
		 */
		Path path = new Path();
		path.addCircle(getCenter().x, getCenter().y, radius, Path.Direction.CW);
		canvas.drawPath(path, paint);
		// linePaint.setStrokeWidth(4);
		// canvas.drawCircle(getCenter().x, getCenter().y, radius, linePaint);
		if (this.drawable != null) {
			this.bitmap = drawableToBitmap(this.drawable);
		}
		if (this.bitmap != null) {

			float scaleFactor = 0.5f * (float) this.radius * 2 / (float) this.bitmap.getWidth();
			matrix.reset();
			matrix.postScale(scaleFactor, scaleFactor);
			float scaledWidth = this.bitmap.getWidth() * scaleFactor;
			float scaledHeight = this.bitmap.getHeight() * scaleFactor;
		
			matrix.postTranslate((this.getTotalWidth() - scaledWidth) / 2, (this.getTotalHeight() - scaledHeight) / 2);

			canvas.save();
			canvas.rotate(currentRotation, this.getCenter().x, this.getCenter().y);
			canvas.drawBitmap(this.bitmap, this.matrix, paint);
			canvas.restore();
		}

		return this.mBitmapDefault;
	}

	protected Bitmap getDefaultBitmap() {
		if (mBitmapDefault == null || dirty ) {
			mBitmapDefault = this.drawDefault();
		}
		return mBitmapDefault;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Log.v("Button","onDraw(): "+this.getWidth()+"x"+this.getHeight()+" "+canvas.getWidth()+"x"+canvas.getHeight());
		super.onDraw(canvas);
		canvas.drawBitmap(this.getDefaultBitmap(), 0, 0, paint);
		// super.onDraw(canvas);
		// canvas.drawBitmap(this.getDefaultBitmap(), new Rect(0, 0,
		// this.radius*8, this.radius*8), new Rect(0,0, this.radius*2,
		// this.radius*2), null);
	}

	public void recycle() {
		if (mBitmapDefault != null) {
			mBitmapDefault.recycle();
			mBitmapDefault = null;
		}
		if (this.bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	public void hide() {
		this.hide(true);
	}

	public void hide(boolean withAnimation) {
		if (this.visible == true) {
			savedLeft = getLeft();
			savedTop = getTop();
			if (!withAnimation) {
				this.visible = false;
				layout(0, 0, 0, 0);
				this.setVisibility(GONE);
			} else {
				ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, this.getCenter().x, this.getCenter().y);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						visible = false;
						layout(0, 0, 0, 0);
						setAnimation(null);
						setVisibility(GONE);
					}
				});
				this.startAnimation(anim);
			}

		}

	}

	protected int currentRotation = 0;

	public void rotate(int degrees) {

		if (this.visible) {
			OvershootInterpolator inter = new OvershootInterpolator();
			RotateAnimation anim = new RotateAnimation(currentRotation - degrees, 0, this.getCenter().x,
					this.getCenter().y);
			anim.setDuration(500);
			anim.setFillAfter(false);
			anim.setInterpolator(inter);
			currentRotation = degrees;
			drawDefault();
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}
			});
			this.startAnimation(anim);
		} else {
			currentRotation = degrees;
			drawDefault();
		}

	}

	public void show() {
		show(true);
	}

	public void show(boolean withAnimation) {
		if (this.visible == false) {
			this.setVisibility(VISIBLE);

			if (withAnimation) {
				OvershootInterpolator inter = new OvershootInterpolator();
				ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, this.getRadius(), this.getRadius());
				anim.setDuration(300);
				anim.setInterpolator(inter);
				anim.setFillAfter(true);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						visible = true;
						setAnimation(null);
					}
				});
				this.startAnimation(anim);
			}
			this.layout(this.savedLeft, this.savedTop, this.savedLeft + 2 * this.radius, this.savedTop + 2
					* this.radius);
		}
		this.visible = true;
	}

	public IconButton setOnClickCallback(OnClickListener l) {
		super.setOnClickListener(l);
		return this;
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {

		}
	};

	private OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// Log.v("Button","onTouch()");
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				OvershootInterpolator inter = new OvershootInterpolator();
				ScaleAnimation anim = new ScaleAnimation(1, (float) 1.5, 1, (float) 1.5, IconButton.this.getRadius(),
						IconButton.this.getRadius());
				anim.setInterpolator(inter);
				anim.setDuration(200);
				anim.setFillAfter(true);
				IconButton.this.startAnimation(anim);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				ScaleAnimation anim = new ScaleAnimation((float) 1.5, 1, (float) 1.5, 1, IconButton.this.getRadius(),
						IconButton.this.getRadius());
				anim.setDuration(300);
				anim.setFillAfter(true);
				IconButton.this.startAnimation(anim);
			}
			return false;
		}
	};

	public IconButton setCaption(String string) {
		return this;
	}

	public IconButton setTextSize(int i) {
		return this;

	}

	

}
