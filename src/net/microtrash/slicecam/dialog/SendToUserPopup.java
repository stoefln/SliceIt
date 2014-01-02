package net.microtrash.slicecam.dialog;

import java.util.List;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.Static;
import net.microtrash.slicecam.data.Composition;
import net.microtrash.slicecam.lib.PushService;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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

import com.parse.ParseException;
import com.parse.ParseObject;
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

	private Composition composition;

	public SendToUserPopup(Context context, View parentView, Composition composition, ParseObject sliceObject, SendToUserPopupListener listener) {
		super(context);

		this.parentView = parentView;
		this.composition = composition;
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
		String myUsername = ParseUser.getCurrentUser().getUsername();
		ParseObject parseComposition = composition.getParseObject();
		
		if (parseComposition.getParseUser(Static.FIELD_PLAYER2) == null
				&& parseComposition.getParseUser(Static.FIELD_PLAYER1) != user) {
			parseComposition.put(Static.FIELD_PLAYER2, user);
			parseComposition.saveInBackground();
		}
		
		updateSlice(slice, user);

		PushService.sendPushMessage(user.getUsername(), myUsername + " sent you a photo slice!", new SendCallback() {

			@Override
			public void done(ParseException arg0) {
				// progressDialog.dismiss();
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
