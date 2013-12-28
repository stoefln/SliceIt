package net.microtrash.slicecam.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.activity.CameraActivity;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.dialog.ProgressbarPopup.OnDialogClosedListener;
import net.microtrash.slicecam.view.SliceView;
import net.microtrash.slicecam.view.SliceView.SliceViewListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CompositionsInProgressFragment extends Fragment {
	private ListView listView;
	private ProgressbarPopup progressDialog;
	private LayoutInflater inflater;
	protected static final String TAG = "CompositionsInProgressFragment";

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
		//ParseQuery<ParseObject> compositionQuery1 = ParseQuery.getQuery("Composition");
		ParseQuery<ParseObject> compositionQuery1 = ParseQuery.getQuery("Composition");
		compositionQuery1.whereEqualTo(Static.FIELD_PLAYER1, ParseUser.getCurrentUser());
		ParseQuery<ParseObject> compositionQuery2 = ParseQuery.getQuery("Composition");
		compositionQuery2.whereEqualTo(Static.FIELD_PLAYER2, ParseUser.getCurrentUser());
		
		ArrayList<ParseQuery<ParseObject>> queryList = new ArrayList<ParseQuery<ParseObject>>();
		queryList.add(compositionQuery1);
		queryList.add(compositionQuery2);
		
		ParseQuery<ParseObject> compositionQuery = ParseQuery.or(queryList);
		compositionQuery.whereLessThan(Static.FIELD_LAST_STEP, Static.MAX_STEP);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		query.whereMatchesQuery(Static.FIELD_COMPOSITION, compositionQuery);
		
		query.include(Static.FIELD_COMPOSITION);
		query.include(Static.FIELD_CREATED_BY);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> slices, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + slices.size() + " slices");
					List<ParseObject> uniqueSliceList = getUniqueSliceList(slices);
					
					CompositionAdapter adapter = new CompositionAdapter(uniqueSliceList);
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
			// only show if this is the "cover slice" of this composition
			if(composition.getInt(Static.FIELD_LAST_STEP) == slice.getInt(Static.FIELD_STEP)){
				sliceMap.put(composition.getObjectId(), slice);
			}
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

	public class CompositionAdapter extends BaseAdapter implements SliceViewListener{

	
		private List<ParseObject> list;

		private OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				final ParseObject slice = (ParseObject) v.getTag(R.id.tag_slice);
				progressDialog.showAndDismiss("hang on", 1000, new OnDialogClosedListener() {
					
					@Override
					public void onDialogClosed(boolean positive) {
						onCompositionSelected(slice.getParseObject(Static.FIELD_COMPOSITION));
					}
				});
				
				v.setVisibility(View.GONE);
				
			}

		};

		private ImageSaver imageSaver;

		public CompositionAdapter(List<ParseObject> objects) {
			list = objects;
			imageSaver = new ImageSaver(getActivity());
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

			ParseObject slice = list.get(position);
			
			SliceView sliceView = (SliceView)itemView.findViewById(R.id.item_slice_sv);
			sliceView.setSlice(slice);
			sliceView.setListener(this);
			
			Button btContinue = (Button) itemView.findViewById(R.id.view_slice_bt_continue);
			btContinue.setTag(R.id.tag_slice, slice);
			btContinue.setOnClickListener(onClickListener);
			//btContinue.setVisibility(View.GONE);
			return itemView;
		}

		@Override
		public void onImageDownloaded(Bitmap bitmap, String filename) {
			imageSaver.saveImageAsync(bitmap, Static.SLICE_DIRECTORY_NAME, filename);
		}

	}
	
	

}
