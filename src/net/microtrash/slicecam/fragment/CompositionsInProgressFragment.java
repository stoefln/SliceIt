package net.microtrash.slicecam.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.activity.CameraActivity;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.view.TextButton;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CompositionsInProgressFragment extends Fragment {
	private ListView listView;
	private ProgressbarPopup progressDialog;
	private LayoutInflater inflater;

	public static CompositionsInProgressFragment create() {

		return new CompositionsInProgressFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		View v = inflater.inflate(R.layout.fragment_compositions_in_progress, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = (ListView) getView().findViewById(R.id.activity_dashboard_lv);
		progressDialog = new ProgressbarPopup(getActivity(), getView());
	}

	@Override
	public void onResume() {
		super.onResume();
		progressDialog.show("Loading photo strips...");
		ParseQuery<ParseObject> compositionQuery = ParseQuery.getQuery("Composition");
		compositionQuery.whereLessThan(Static.FIELD_LAST_STEP, Static.MAX_STEPS);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		query.whereMatchesQuery(Static.FIELD_COMPOSITION, compositionQuery);
		query.whereEqualTo(Static.FIELD_SEND_TO_USER, ParseUser.getCurrentUser());
		
		query.include(Static.FIELD_COMPOSITION);
		query.include(Static.FIELD_CREATED_BY);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> slices, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + slices.size() + " scores");
					CompositionAdapter adapter = new CompositionAdapter(getUniqueSliceList(slices));
					listView.setAdapter(adapter);
					progressDialog.dismiss();
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}

	private List<ParseObject> getUniqueSliceList(List<ParseObject> slices) {
		HashMap<String, ParseObject> sliceMap = new HashMap<String, ParseObject>();
		for (ParseObject slice : slices) {
			ParseObject composition = (ParseObject) slice.getParseObject(Static.FIELD_COMPOSITION);
			sliceMap.put(composition.getObjectId(), slice);
		}

		ArrayList<ParseObject> sliceList = new ArrayList<ParseObject>();
		for (String key : sliceMap.keySet()) {
			sliceList.add(sliceMap.get(key));
		}
		return sliceList;
	}
	
	private void onCompositionSelected(ParseObject composition) {
		CameraActivity.start(composition.getObjectId(), getActivity());
	}

	public class CompositionAdapter extends BaseAdapter {

		private List<ParseObject> list;

		private OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject slice = (ParseObject) v.getTag(R.id.tag_slice);
				onCompositionSelected(slice.getParseObject(Static.FIELD_COMPOSITION));
			}

		};

		public CompositionAdapter(List<ParseObject> objects) {
			list = objects;

		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final View itemView;

			if (convertView == null) {
				itemView = inflater.inflate(R.layout.item_slice, parent, false);
				itemView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
						ListView.LayoutParams.WRAP_CONTENT));
			} else {
				itemView = convertView;
			}

			TextView tvUsername = (TextView) itemView.findViewById(R.id.item_composition_username);
			TextView tvSlice = (TextView) itemView.findViewById(R.id.item_composition_slice);
			final ImageView ivImage = (ImageView) itemView.findViewById(R.id.item_composition_iv_slice);
			TextButton btContinue = (TextButton) itemView.findViewById(R.id.item_composition_bt_continue);
			ParseObject slice = list.get(position);
			btContinue.setTag(R.id.tag_slice, slice);
			btContinue.setOnClickListener(onClickListener);

			final String compositionId = slice.getParseObject(Static.FIELD_COMPOSITION).getObjectId();
			final String filename = Static.createSliceFilename(compositionId, slice.getInt(Static.FIELD_STEP));

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
						ivImage.setImageBitmap(bmp);
						// imageSaver.saveImageAsync(bmp,
						// Static.SLICE_DIRECTORY_NAME, filename);

					} else {
						e.printStackTrace();
					}
				}

			});

			tvUsername.setText(slice.getParseObject(Static.FIELD_CREATED_BY).getString(Static.FIELD_USERNAME));
			Date date = slice.getDate("createdAt");
			if (date != null) {
				tvSlice.setText(date.toString());
			}
			tvSlice.setText("step: " + (slice.getInt(Static.FIELD_STEP) + 1) + "/4 \t ID: " + slice.getObjectId());
			return itemView;
		}

	}

}
