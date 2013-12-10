package net.microtrash.slicecam.activity;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends FragmentActivity {

	private Button bRegister;
	private EditText tbEmail;
	private EditText tbUsername;
	private EditText tbPassword;
	private TextView tvError;
	private TextView tvLogin;
	private ProgressbarPopup progressDialog;

	public static void start(Context context) {
		Intent i = new Intent(context, RegisterActivity.class);
		context.startActivity(i);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		progressDialog = new ProgressbarPopup(this);
		
		if (ParseUser.getCurrentUser() != null) {
			DashboardActivity.start(this);
			finish();
		}

		bRegister = (Button) findViewById(R.id.bLogin);
		bRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				register();

			}
		});

		tvLogin = (TextView) findViewById(R.id.activity_register_tv_login);
		tvLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(intent);

			}
		});

		tbEmail = (EditText) findViewById(R.id.tbEmail);
		tbUsername = (EditText) findViewById(R.id.tbUsername);
		tbPassword = (EditText) findViewById(R.id.tbPassword);
		tvError = (TextView) findViewById(R.id.tvError);
		tvError.setVisibility(View.GONE);
	}

	private void register() {
		tvError.setVisibility(View.GONE);

		ParseUser user = new ParseUser();
		user.setUsername(tbUsername.getText().toString());
		user.setPassword(tbPassword.getText().toString());
		user.setEmail(tbEmail.getText().toString());
		progressDialog.show("Registering");
		try {
			user.signUpInBackground(new SignUpCallback() {
				public void done(ParseException e) {
					if (e == null) {
						DashboardActivity.start(RegisterActivity.this);
						finish();
					} else {
						showError(e.getLocalizedMessage());

					}
				}
			});
		} catch (Exception e) {
			showError(e.getLocalizedMessage());
		}
	}

	private void showError(String message) {
		progressDialog.dismiss();
		tvError.setVisibility(View.VISIBLE);
		tvError.setText("Error occured while signing up:\n" + message);
	}
}
