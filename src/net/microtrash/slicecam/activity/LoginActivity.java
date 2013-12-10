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

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends FragmentActivity {
	private Button bLogin;
	private EditText tbUsername;
	private EditText tbPassword;
	private TextView tvError;
	private TextView tvRegister;
	private ProgressbarPopup progressDialog;

	public static void start(Context context) {
		Intent i = new Intent(context, RegisterActivity.class);
		context.startActivity(i);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		progressDialog = new ProgressbarPopup(this);
		bLogin = (Button) findViewById(R.id.bLogin);
		bLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();

			}
		});

		tvRegister = (TextView) findViewById(R.id.activity_register_tv_register);
		tvRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);

			}
		});

		// tbEmail = (EditText) findViewById(R.id.tbEmail);
		tbUsername = (EditText) findViewById(R.id.tbUsername);
		tbPassword = (EditText) findViewById(R.id.tbPassword);
		tvError = (TextView) findViewById(R.id.tvError);
		tvError.setVisibility(View.GONE);
	}

	private void login() {
		tvError.setVisibility(View.GONE);

		progressDialog.show("Logging in");
		try{
		ParseUser.logInInBackground(tbUsername.getText().toString(), tbPassword.getText().toString(),
				new LogInCallback() {

					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null) {
							DashboardActivity.start(LoginActivity.this);
							finish();
						} else {
							showError(e.getLocalizedMessage());
						}

					}
				});
		}catch(Exception e){
			showError(e.getLocalizedMessage());
		}
		
	}

	protected void showError(String message) {
		progressDialog.dismiss();
		tvError.setVisibility(View.VISIBLE);
		tvError.setText("Error occured while logging in:\n" + message);
	}

}
