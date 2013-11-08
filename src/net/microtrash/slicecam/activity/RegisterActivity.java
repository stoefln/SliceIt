package net.microtrash.slicecam.activity;

import net.microtrash.slicecam.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends Activity {

	private Button bRegister;
	private EditText tbEmail;
	private EditText tbUsername;
	private EditText tbPassword;
	private TextView tvError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		if(ParseUser.getCurrentUser() != null){
			startDashboardActivity();
		}
		
		bRegister = (Button) findViewById(R.id.bRegister);
		bRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				register();
				
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

		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				if (e == null) {
					startDashboardActivity();
				} else {
					tvError.setVisibility(View.VISIBLE);
					tvError.setText("Error occured while signing up: "+e.getLocalizedMessage());
					
				}
			}

			

		});
	}
	
	private void startDashboardActivity() {
		Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
		startActivity(i);
		finish();
	}
}
