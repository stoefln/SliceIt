package net.microtrash.slicecam.lib;

import java.util.List;

import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.data.Composition;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DataAccess {
	
	public interface OnCompositionLoadedListener{
		void onCompositionLoaded(Composition composition);
	}

	protected static final String TAG = "DataAccess";
	
	public static void loadCurrentComposition(final String compositionId, final OnCompositionLoadedListener listener) {
		
		ParseQuery<ParseObject> compositionQuery = ParseQuery.getQuery("Composition");
		compositionQuery.whereEqualTo("objectId", compositionId);

		ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Slice");
		query2.whereMatchesQuery(Static.FIELD_COMPOSITION, compositionQuery);
		query2.include(Static.FIELD_COMPOSITION);
		query2.include(Static.FIELD_CREATED_BY);
		query2.findInBackground(new FindCallback<ParseObject>() {

			public void done(List<ParseObject> slices, ParseException e) {
				if (e == null) {
					if (slices.size() > 0) {
						Composition comp = new Composition();
						comp.setParseObject(slices.get(0).getParseObject(Static.FIELD_COMPOSITION));
						comp.setSlices(slices);
						
						Log.v(TAG, "Loaded " + slices.size() + " slices");
						if(listener != null){
							listener.onCompositionLoaded(comp);
						}
				
					} else {
						Log.e(TAG, "No slices for composition " + compositionId + " found!");
					}

				} else {
					Log.e(TAG, "Error while trying to load composition: " + e.getMessage());
				}

			}

		});
	}
}
