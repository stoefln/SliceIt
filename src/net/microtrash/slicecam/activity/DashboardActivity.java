package net.microtrash.slicecam.activity;

import net.microtrash.slicecam.R;
import net.microtrash.slicecam.dialog.ProgressbarPopup;
import net.microtrash.slicecam.fragment.CompositionsFinishedFragment;
import net.microtrash.slicecam.fragment.CompositionsInProgressFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.ParseUser;
import com.viewpagerindicator.TitlePageIndicator;

public class DashboardActivity extends FragmentActivity {

	private ProgressbarPopup progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = getLayoutInflater().inflate(R.layout.activity_dashboard, null);
		setContentView(v);

		ViewPager pager = (ViewPager) findViewById(R.id.activity_dashboard_vp);
		pager.setAdapter(new DashboardAdapter(getSupportFragmentManager()));

		TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.activity_dashboard_vpi);
		titleIndicator.setViewPager(pager);
	}

	public static class DashboardAdapter extends FragmentPagerAdapter {
		private static final CharSequence[] TITLES = {"In Progress", "Finished"};

		public DashboardAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return CompositionsInProgressFragment.create();
			case 1:
				return CompositionsFinishedFragment.create();

			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_dashboard, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_logout:
	        	ParseUser.logOut();
	        	RegisterActivity.start(this);
	            return true;
	       
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public static void start(Context c) {
		Intent i = new Intent(c, DashboardActivity.class);
		c.startActivity(i);
	}
}
