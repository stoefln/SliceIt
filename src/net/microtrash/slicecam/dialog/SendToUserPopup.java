package net.microtrash.slicecam.dialog;

import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

public class SendToUserPopup extends PopupWindow {

	public interface SendToUserPopupListener {
		void onNotificationSent(ParseUser user);
	}

	private static final String TAG = "SelectUserPopup";

	private ProgressbarPopup progressDialog;

	private View parentView;

	private View dialogLayout;

	private GridView gridView;

	private LayoutInflater inflater;

	private SendToUserPopupListener listener;

	private ParseObject slice;

	public SendToUserPopup(Context context, View parentView, ParseObject sliceObject, SendToUserPopupListener listener) {
		super(context);

		this.parentView = parentView;
		this.slice = sliceObject;
		this.listener = listener;

		inflater = LayoutInflater.from(context);
		dialogLayout = inflater.inflate(R.layout.popup_select_user, null);
		setContentView(dialogLayout);
		gridView = (GridView) dialogLayout.findViewById(R.id.dialog_send_to_gv);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setBackgroundDrawable(new ColorDrawable());
		setFocusable(true);
		// progressDialog.dismiss();

		progressDialog = new ProgressbarPopup(context, dialogLayout);

	}

	private void updateComposition(ParseObject slice, final ParseUser user) {
		/*
		 * ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
		 * 
		 * // Retrieve the object by id
		 * query.getInBackground(slice.getObjectId(), new
		 * GetCallback<ParseObject>() { public void done(ParseObject slice,
		 * ParseException e) { if (e == null) { ParseObject composition =
		 * (ParseObject) slice.get(Static.FIELD_COMPOSITION);
		 * composition.put(Static.FIELD_SEND_TO_USER, user);
		 * composition.saveInBackground(new SaveCallback() {
		 * 
		 * @Override public void done(ParseException arg0) {
		 * compositionUpdated(user); } }); } } });
		 */

	}

	private void show() {
		parentView.post(new Runnable() {
			@Override
			public void run() {
				try {
					showAtLocation(parentView, 0, 0, 0);
				} catch (Exception e) {
				}
			}
		});

	}

	public void showUserSelection(List<ParseUser> users) {
		gridView.setAdapter(new UserAdapter(users));
		show();
	}

	public void sendToUser(final ParseUser user) {
		show();
		progressDialog.show("sending your photo\nto " + user.getUsername());
		updateSlice(slice, user);
		
		ParseQuery userQuery = ParseUser.getQuery();
		userQuery.whereEqualTo("username", user.getUsername());
		Log.v(TAG, "Send push to " + user.getUsername());

		// Find devices associated with these users
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereMatchesQuery("user", userQuery);
		pushQuery.whereEqualTo("deviceType", "android");
		pushQuery.whereEqualTo("channels", Static.PUSH_DEFAULT_CHANNEL_KEY);
		// Send push notification to query
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery);
		push.setMessage(ParseUser.getCurrentUser().getUsername() + " sent you a photo slice!");
		push.sendInBackground(new SendCallback() {

			@Override
			public void done(ParseException arg0) {
				progressDialog.dismiss();
				try {
					dismiss();
				} catch (Exception e) {
				}
				listener.onNotificationSent(user);
			}
		});
	}

	private void updateSlice(final ParseObject slice, final ParseUser user) {
		slice.put(Static.FIELD_SEND_TO_USER, user);
		slice.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException arg0) {
				onSliceUpdated(slice, user);
			}

		});
	}

	private void onSliceUpdated(ParseObject slice, final ParseUser user) {
		

	}

	public class UserAdapter extends BaseAdapter {

		private List<ParseUser> list;

		private OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseUser user = (ParseUser) v.getTag(R.id.tag_slice);
				sendToUser(user);

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
				itemView = inflater.inflate(R.layout.item_user, parent, false);
				itemView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
						ListView.LayoutParams.WRAP_CONTENT));
				itemView.setOnClickListener(onClickListener);
			} else {
				itemView = convertView;
			}

			ParseUser user = list.get(position);
			itemView.setTag(R.id.tag_slice, user);

			TextView username = (TextView) itemView.findViewById(R.id.item_user_username);
			username.setText(user.getUsername());

			return itemView;
		}

	}

}
