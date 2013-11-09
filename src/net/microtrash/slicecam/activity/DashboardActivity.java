package net.microtrash.slicecam.activity;

import java.util.Date;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.activity.DashboardActivity.CompositionAdapter;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.dialog.SendToUserPopup.UserAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DashboardActivity extends Activity {

	private ProgressbarPopup progressDialog;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = getLayoutInflater().inflate(R.layout.activity_dashboard, null);
		setContentView(v);

		Button btnNew = (Button) v.findViewById(R.id.activity_dashboard_btn_new);
		btnNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startCameraActivity(null);
			}
		});

		listView = (ListView) v.findViewById(R.id.activity_dashboard_lv_requests);

		
		progressDialog = new ProgressbarPopup(this, v); 
	}

	@Override
	protected void onResume() {
		super.onResume();
		progressDialog.show("Loading photo strips...");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Composition");
		// query.whereEqualTo("playerName", "Dan Stemkoski");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> compositions, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + compositions.size() + " scores");
					CompositionAdapter adapter = new CompositionAdapter(compositions);
					listView.setAdapter(adapter);
					progressDialog.dismiss();
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}
	private void startCameraActivity(String compositionId) {
		Intent i = new Intent(getApplicationContext(), CameraActivity.class);
		if(compositionId != null){
			i.putExtra(Static.EXTRA_COMPOSITION_ID, compositionId);
		}
		startActivity(i);
	}

	private void onCompositionSelected(ParseObject composition) {
		startCameraActivity(composition.getObjectId());

	}

	public class CompositionAdapter extends BaseAdapter {

		private List<ParseObject> list;

		private OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject composition = (ParseObject) v.getTag(R.id.tag_user);
				onCompositionSelected(composition);

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
				itemView = DashboardActivity.this.getLayoutInflater().inflate(R.layout.item_composition, parent, false);
				itemView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
						ListView.LayoutParams.WRAP_CONTENT));
				itemView.setOnClickListener(onClickListener);
			} else {
				itemView = convertView;
			}

			ParseObject object = list.get(position);
			itemView.setTag(R.id.tag_user, object);

			TextView tvUsername = (TextView) itemView.findViewById(R.id.item_composition_username);
			TextView tvSlice = (TextView) itemView.findViewById(R.id.item_composition_slice);
			tvUsername.setText(object.getString(Static.FIELD_CREATED_BY));
			Date date = object.getDate("createdAt");
			if(date != null){
				tvSlice.setText(date.toString());
			}
			tvSlice.setText("step: "+object.getInt(Static.FIELD_LAST_STEP) + "/4 \t ID: " + object.getObjectId());
			return itemView;
		}

	}
}
