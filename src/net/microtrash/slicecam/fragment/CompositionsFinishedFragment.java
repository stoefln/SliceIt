package net.microtrash.slicecam.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.activity.CameraActivity;
import net.microtrash.slicecam.activity.PhotoStripActivity;
import net.microtrash.slicecam.data.Composition;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.fragment.CompositionsInProgressFragment.CompositionAdapter;
import net.microtrash.slicecam.lib.ImageEffects;
import net.microtrash.slicecam.view.TextButton;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class CompositionsFinishedFragment extends Fragment {

	private LayoutInflater inflater;
	private LinearLayout sliceContainer;
	private ListView listView;
	private ProgressbarPopup progressDialog;

	public static Fragment create() {

		return new CompositionsFinishedFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		View v = inflater.inflate(R.layout.fragment_compositions_finished, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = (ListView) getView().findViewById(R.id.fragment_compositions_finished_lv);
		progressDialog = new ProgressbarPopup(getActivity(), getView());
	}

	@Override
	public void onResume() {
		super.onResume();

		ParseQuery<ParseObject> compositionQuery = ParseQuery.getQuery("Composition");
		compositionQuery.whereGreaterThan(Static.FIELD_LAST_STEP, Static.MAX_STEP - 1);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		query.whereMatchesQuery(Static.FIELD_COMPOSITION, compositionQuery);
		query.whereEqualTo(Static.FIELD_SEND_TO_USER, ParseUser.getCurrentUser());

		query.include(Static.FIELD_COMPOSITION);
		query.include(Static.FIELD_CREATED_BY);

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> slices, ParseException e) {
				if (e == null) {
					log("Retrieved " + slices.size() + " scores");

					onCompositionsLoaded(getCompositionListFromSliceList(slices));

				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}

		});
	}

	private List<Composition> getCompositionListFromSliceList(List<ParseObject> slices) {
		HashMap<String, Composition> compositions = new HashMap<String, Composition>();
		for (ParseObject slice : slices) {
			ParseObject parseComp = (ParseObject) slice.getParseObject(Static.FIELD_COMPOSITION);
			Composition composition = compositions.get(parseComp.getObjectId());
			if(composition == null){
				composition = new Composition();
				composition.setParseObject(parseComp);
			}
			composition.getSlices().add(slice);
			compositions.put(parseComp.getObjectId(), composition);
		}

		ArrayList<Composition> compositionList = new ArrayList<Composition>();
		for (String key : compositions.keySet()) {
			compositionList.add(compositions.get(key));
		}
		return compositionList;
	}

	private void onCompositionsLoaded(List<Composition> list) {

		
		CompositionAdapter adapter = new CompositionAdapter(list);
		listView.setAdapter(adapter);
	}

	
	protected void onCompositionLoaded(ParseObject composition, List<ParseObject> slices) {
		ArrayList<String> filenames = new ArrayList<String>();
		for (ParseObject slice : slices) {
			String filename = Static.createSliceFilename(composition.getObjectId(), slice.getInt(Static.FIELD_STEP));
			log(filename);
			filenames.add(filename);
		}
		PhotoStripActivity.addImageViewsToContainer(sliceContainer, filenames);
	}

	private void log(String string) {
		Log.v("CompositionFinishedFragment", string);

	}

	public class CompositionAdapter extends BaseAdapter implements OnClickListener {

		private List<Composition> list;

		public CompositionAdapter(List<Composition> list2) {
			list = list2;

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
				itemView = inflater.inflate(R.layout.item_composition, parent, false);
				LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
						ListView.LayoutParams.WRAP_CONTENT);
				
				itemView.setLayoutParams(params);
			} else {
				itemView = convertView;
			}
			ImageView iv = (ImageView) itemView.findViewById(R.id.item_composition_iv);

			Composition composition = list.get(position);
			
			String filepath = Static.getCompositionFilpath(Static.createCompositionFilename(composition.getParseObjectId()
					+ ".jpg"));
			File file = new File(filepath);
			log("filepath: " + filepath + " exists: " + file.exists());
			if (file.exists()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bmpComposition = BitmapFactory.decodeFile(filepath, options);
				iv.setImageBitmap(bmpComposition);
				iv.setTag(composition);
				iv.setOnClickListener(this);
			}

			return itemView;
		}

		@Override
		public void onClick(View v) {
			/*String filepath = (String) v.getTag();
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" +filepath), "image/*");
			startActivity(intent);*/
			ParseObject composition = (ParseObject) v.getTag();
			PhotoStripActivity.start(getActivity(), composition.getObjectId());
		}

	}

}
