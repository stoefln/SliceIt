package net.microtrash.slicecam.view;


import net.microtrash.slicecam.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class BubbleText extends TextView {

	private Paint paint;
	private Typeface font;
	private boolean visible = true;
	private Path circlePath;
	private int savedLeft;
	private int savedTop;
	private int radius;

	public int getRadius(){
		return radius;
	}

	public BubbleText(Context context) {
		super(context);
		init(context);
	}

	public BubbleText(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
		init(attrs);
	}

	public BubbleText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
		init(attrs);
	}

	private void init(Context context) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(context.getResources().getColor(R.color.purple));
		if (isInEditMode()) {
			if (getText() == null || getText().equals("")) {
				setText("Bubbleitius test text for displaying");
			}
		} else {
			font = Typeface.createFromAsset(context.getAssets(), "fonts/Capsuula/Capsuula.ttf");
		}
		if (font != null) {
			setTypeface(font);
		}
		circlePath = new Path();
	}

	private void init(AttributeSet attrs) {
/* doesnt work- why?
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IconButton);

		int theColor = a.getColor(R.styleable.IconButton_circleColor, Color.BLACK);
		if (theColor != 0) {
			paint.setColor(theColor);
		}
		a.recycle();*/
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		int padding = 0;
		float paddingFactor = 2.7f;

		if (width <= height || height == 0) {
			setMeasuredDimension(width, width);
			radius = width / 2;
		} else {
			setMeasuredDimension(height, height);
			radius = height / 2;
		}
		if(radius == 0){
			if (getLayoutParams().width > 0) {
				radius = getLayoutParams().width / 2;
			}
			if (getLayoutParams().height > 0) {
				radius = getLayoutParams().height / 2;
			}
		}
		padding = (int) (radius / paddingFactor);
		// int padding = getWidth()/2;//(int)
		// context.getResources().getDimension(R.dimen.bubble_text_padding);
		Log.v("BubbleText", "radius: "+radius);
		this.setPadding(padding, padding, padding, padding);

	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.rotate(-45, this.getWidth() / 2, this.getHeight() / 2);
		canvas.save();

		circlePath.reset();
		circlePath.addCircle(getWidth() / 2, getHeight() / 2, radius, Path.Direction.CW);

		canvas.drawPath(circlePath, paint);
		super.onDraw(canvas);
		canvas.restore();
	}

	public void setVisibility(int visibility) {
		if (visibility == GONE || visibility == INVISIBLE) {
			this.visible = false;
		} else {
			this.visible = true;
		}
		super.setVisibility(visibility);
	}

	public void hide() {
		this.hide(true);
	}

	public void hide(boolean withAnimation) {
		Log.v("BubbleText", "hide: widthAnimation: "+withAnimation+ " width: "+getWidth()+ " height: "+getHeight()+ " radius: "+radius);
		if (this.visible == true) {
			savedLeft = getLeft();
			savedTop = getTop();
			if (!withAnimation) {
				this.visible = false;
				//layout(0, 0, 0, 0);
				this.setVisibility(INVISIBLE);
			} else {
				ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, getWidth() / 2, getHeight() / 2);
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
						//layout(0, 0, 0, 0);
						setAnimation(null);
						setVisibility(INVISIBLE);
					}
				});
				this.startAnimation(anim);
			}

		}
	}

	public void show() {
		show(true);
	}

	public void show(boolean withAnimation) {
		Log.v("BubbleText", "show: widthAnimation: "+withAnimation+ " width: "+getWidth()+ " height: "+getHeight()+ " radius: "+radius+ " savedLeft: "+savedLeft+" savedTop: "+savedTop);
		if (this.visible == false) {
			this.setVisibility(VISIBLE);
			//this.layout(this.savedLeft, this.savedTop, this.savedLeft + 2 * this.radius, this.savedTop + 2* this.radius);
			if (withAnimation) {
				OvershootInterpolator inter = new OvershootInterpolator();
				ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, radius, radius);
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
			
		}
		this.visible = true;
	}

}
