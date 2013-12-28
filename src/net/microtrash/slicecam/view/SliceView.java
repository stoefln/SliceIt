package net.microtrash.slicecam.view;

import java.io.File;
import java.util.Date;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.lib.Tools;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class SliceView extends RelativeLayout {
	private static final String TAG = "SliceView";
	private ParseObject slice;
	private RelativeLayout infoView;
	private ProportionalBitmapView imageView;
	private TextView tvUsername;
	private TextView tvId;
	private SliceViewListener listener;
	private Paint arrowPaint;
	private View contentView;

	public interface SliceViewListener {
		void onImageDownloaded(Bitmap bitmap, String filename);
	}

	public SliceView(Context context) {
		super(context);
		init(context);
	}

	public SliceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SliceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		contentView = inflater.inflate(R.layout.view_slice, this, true);
		//addView(contentView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//setWillNotDraw(false);
		arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		arrowPaint.setColor(Color.BLACK);
		arrowPaint.setStyle(Paint.Style.FILL);
		
		contentView.findViewById(R.id.view_slice_label).setVisibility(View.GONE);
		// arrowPaint.setStrokeWidth(10);
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int) ((float) width / Static.SLICE_ASPECT_RATIO);
		setMeasuredDimension(width, height);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		infoView = (RelativeLayout) findViewById(R.id.view_slice_rl_info);
		View arrowView = findViewById(R.id.view_slice_av_arrow);
		int infoHeight = (int) ((float) getWidth() / Static.SLICE_ASPECT_RATIO * Static.HIDE_RATIO);
		infoView.layout(0, 0, getWidth(), infoHeight);
		float arrowLeft = getWidth() * 0.5f;
		int arrowSize = Tools.dip2Pixels(20, getContext());
		arrowView.layout((int) arrowLeft, infoHeight, (int) (arrowLeft + arrowSize * 2), infoHeight + arrowSize);

	}

	public ParseObject getSlice() {
		return slice;
	}

	public void setSlice(ParseObject slice) {
		this.slice = slice;
		infoView = (RelativeLayout) findViewById(R.id.view_slice_rl_info);
		imageView = (ProportionalBitmapView) findViewById(R.id.view_slice_iv_slice);
		tvUsername = (TextView) findViewById(R.id.view_slice_tv_username);
		tvId = (TextView) findViewById(R.id.view_slice_tv_id);
		contentView.findViewById(R.id.view_slice_label).setVisibility(View.VISIBLE);
		final String compositionId = slice.getParseObject(Static.FIELD_COMPOSITION).getObjectId();
		final String filename = Static.createSliceFilename(compositionId, slice.getInt(Static.FIELD_STEP));
		final String filepath = Static.getFullSliceFilepath(slice);

		File f = new File(filepath);
		if (f.exists()) {
			Log.v(TAG, "loading from disk: " + filepath);
			Bitmap bmp = BitmapFactory.decodeFile(filepath);
			imageView.setImageBitmap(bmp);
		} else {
			Log.v(TAG, "loading from backend: " + filepath);
			ParseFile sliceFile = (ParseFile) slice.get(Static.FIELD_FILE);
			sliceFile.getDataInBackground(new GetDataCallback() {
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

						/*
						 * Bitmap blurredSlice = ImageEffects.fastblur(bmp, 100,
						 * bmp.getWidth(), (int) (bmp.getHeight() * (1d -
						 * 0.2)));
						 */
						imageView.setImageBitmap(bmp);
						if (getListener() != null) {
							getListener().onImageDownloaded(bmp, filename);
						}
					} else {
						e.printStackTrace();
					}
				}

			});
		}

		tvUsername.setText(slice.getParseObject(Static.FIELD_CREATED_BY).getString(Static.FIELD_USERNAME));
		Date date = slice.getDate("createdAt");

		tvId.setText("step: " + (slice.getInt(Static.FIELD_STEP) + 1) + "/4 \t ID: " + slice.getObjectId());
	}

	public SliceViewListener getListener() {
		return listener;
	}

	public void setListener(SliceViewListener listener) {
		this.listener = listener;
	}

}
