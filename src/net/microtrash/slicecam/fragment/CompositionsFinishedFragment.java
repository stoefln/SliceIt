package net.microtrash.slicecam.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.microtrash.slicecam.ImageSaver;
import net.microtrash.slicecam.ImageSaver.OnImageSavedListener;
import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.activity.PhotoStripActivity;
import net.microtrash.slicecam.data.Composition;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.lib.ImageTools;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CompositionsFinishedFragment extends Fragment implements OnImageSavedListener {

	private static final String TAG = "CompositionsFinishedFragment";
	private LayoutInflater inflater;
	private ListView listView;
	private ProgressbarPopup progressDialog;
	private ImageSaver imageSaver;
	private int downloads;
	private List<Composition> loadedCompositions;
	private net.microtrash.slicecam.fragment.CompositionsFinishedFragment.CompositionAdapter adapter;

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
		imageSaver = new ImageSaver(getActivity());
		ParseQuery<ParseObject> compositionQuery1 = ParseQuery.getQuery("Composition");
		compositionQuery1.whereEqualTo(Static.FIELD_PLAYER1, ParseUser.getCurrentUser());
		ParseQuery<ParseObject> compositionQuery2 = ParseQuery.getQuery("Composition");
		compositionQuery2.whereEqualTo(Static.FIELD_PLAYER2, ParseUser.getCurrentUser());
		
		ArrayList<ParseQuery<ParseObject>> queryList = new ArrayList<ParseQuery<ParseObject>>();
		queryList.add(compositionQuery1);
		queryList.add(compositionQuery2);
		
		ParseQuery<ParseObject> compositionQuery = ParseQuery.or(queryList);
		compositionQuery.whereGreaterThan(Static.FIELD_LAST_STEP, Static.MAX_STEP - 1);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		query.whereMatchesQuery(Static.FIELD_COMPOSITION, compositionQuery);
		//query.whereEqualTo(Static.FIELD_SEND_TO_USER, ParseUser.getCurrentUser());

		query.include(Static.FIELD_COMPOSITION);
		query.include(Static.FIELD_CREATED_BY);

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> slices, ParseException e) {
				if (e == null) {
					log("Retrieved " + slices.size() + " slices (finished composition)");

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
		loadedCompositions = list;
		for(Composition composition : list){
			downloads = 0;
			for (ParseObject slice : composition.getSlices()) {
				String filename = Static.createSliceFilename(composition.getParseObjectId(), slice.getInt(Static.FIELD_STEP));
				File file = new File(Static.getFullSliceFilepath(slice));
				if(!file.exists()){
					downloads++;
					Log.v(TAG, "downloading slice "+filename);
					new SliceDownloader(imageSaver, slice, filename, this);
				}
				log(filename);

			}
		}
		if(downloads == 0){
			onAllImagesDownloaded();
		}
		
		adapter = new CompositionAdapter(list);
		listView.setAdapter(adapter);
	}

	
	private class SliceDownloader{
		
		public SliceDownloader(final ImageSaver imageSaver, final ParseObject slice, final String filename, OnImageSavedListener listener) {

			ParseFile sliceFile = (ParseFile) slice.get(Static.FIELD_FILE);
			sliceFile.getDataInBackground(new GetDataCallback() {
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						imageSaver.saveImageAsync(bitmap, Static.SLICE_DIRECTORY_NAME, filename, CompositionsFinishedFragment.this);
					} else {
						e.printStackTrace();
					}
				}

			});
			
		}
	}
	

	@Override
	public void onImageSaved(String lastCompositionPath) {
		downloads--;
		if(downloads == 0){
			onAllImagesDownloaded();
		}
		
	}

	private void onAllImagesDownloaded() {
		for(Composition composition : loadedCompositions){
			ImageTools.createCompositionImage(getActivity(), composition, new OnImageSavedListener() {
				
				@Override
				public void onImageSaved(String compositionPath) {
					adapter.notifyDataSetChanged();
					// if composition image was created for the first time -> show it
					ImageTools.startShowImageIntent(getActivity(), compositionPath);
				}
			});

		}
		
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
				options.inSampleSize = 2;
				Bitmap bmpComposition = BitmapFactory.decodeFile(filepath, options);
				iv.setImageBitmap(bmpComposition);
				iv.getLayoutParams().height = listView.getHeight();
				
			}
			iv.setTag(composition);
			iv.setOnClickListener(this);
			return itemView;
		}

		@Override
		public void onClick(View v) {
			
			Composition composition = (Composition) v.getTag();
			PhotoStripActivity.start(getActivity(), composition.getParseObjectId());
		}

	}


}
