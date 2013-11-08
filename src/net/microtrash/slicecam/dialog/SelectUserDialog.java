package net.microtrash.slicecam.dialog;

import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SelectUserDialog extends DialogFragment {

	public interface UserAdapterListener {
		void onUserSelected(ParseUser user);
	}

	private static final String KEY_SLICE_ID = "slice_id";
	private static final String TAG = "SelectUserDialog";

	public static SelectUserDialog newInstance(String sliceId) {
		SelectUserDialog sd = new SelectUserDialog();

		Bundle args = new Bundle();
		args.putString(KEY_SLICE_ID, sliceId);
		sd.setArguments(args);

		return sd;
	}

	private ProgressbarPopup progressDialog;

	@Override
	public Dialog onCreateDialog(Bundle _savedInstanceState) {
		Bundle args = getArguments();
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_user, null);
		final ListView listView = (ListView) v.findViewById(R.id.dialog_send_to_lv);

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> objects, ParseException e) {
				if (e == null) {
					Log.v("user", "total: " + objects.size());
					listView.setAdapter(new UserAdapter(objects));
					progressDialog.dismiss();
				} else {
					// Something went wrong.
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v);
		progressDialog = new ProgressbarPopup(getActivity(), v);
		progressDialog.showDelayed();
		return builder.create();

	}

	@Override
	public void onResume() {
		super.onResume();
		getDialog().getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

	}

	private void onUserSelected(ParseUser user) {
		ParseQuery userQuery = ParseUser.getQuery();
		userQuery.whereEqualTo("username", user.getUsername());
		Log.v(TAG, "Send push to " + user.getUsername());
		// Find devices associated with these users
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereMatchesQuery("user", userQuery);
		pushQuery.whereEqualTo("deviceType", "android");
		pushQuery.whereEqualTo("channels", Static.PUSH_DEFAULT_CHANNEL_KEY);
		// Send push notification to query
		
		updateComposition(getArguments().getString(KEY_SLICE_ID), user);
		
		ParsePush push = new ParsePush();
		//push.setChannel(Static.PUSH_DEFAULT_CHANNEL_KEY);
		
		push.setQuery(pushQuery); // Set our Installation query
		push.setMessage(ParseUser.getCurrentUser().getUsername() + " sent you a photo slice!");
		//push.setData(data);
		push.sendInBackground();

	}

	private void updateComposition(String sliceId, final ParseUser user) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		 
		// Retrieve the object by id
		query.getInBackground(sliceId, new GetCallback<ParseObject>() {
		  public void done(ParseObject slice, ParseException e) {
		    if (e == null) {
		      ParseObject composition = (ParseObject) slice.get(Static.FIELD_COMPOSITION);
		      composition.put(Static.FIELD_SEND_TO_USER, user);
		      composition.saveInBackground();
		    }
		  }
		});
		
	}

	public class UserAdapter extends BaseAdapter {

		private List<ParseUser> list;

		private OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseUser user = (ParseUser) v.getTag(R.id.tag_user);
				onUserSelected(user);

			}

		};

		public UserAdapter(List<ParseUser> objects) {
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
				itemView = getActivity().getLayoutInflater().inflate(R.layout.item_user, parent, false);
				itemView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
						ListView.LayoutParams.WRAP_CONTENT));
				itemView.setOnClickListener(onClickListener);
			} else {
				itemView = convertView;
			}

			ParseUser user = list.get(position);
			itemView.setTag(R.id.tag_user, user);

			TextView username = (TextView) itemView.findViewById(R.id.item_user_username);
			username.setText(user.getUsername());

			return itemView;
		}

	}

}
