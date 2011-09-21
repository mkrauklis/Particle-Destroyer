package com.michaelkrauklis.android.lastdefender;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.michaelkrauklis.android.lastdefender.preferences.Preferences;
import com.michaelkrauklis.android.lastdefender.view.LastDefenderRunView;

public class LastDefender extends Activity {
	private static final int NEW_GAME = 0;
	private static final int PREFERENCES = 1;

	private LastDefenderRunView lastDefenderView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.landing_page);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.size() == 0) {
			menu.add(0, NEW_GAME, 0, "New Game");
			menu.add(0, PREFERENCES, 1, "Preferences");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == NEW_GAME) {
			if (lastDefenderView == null) {
				lastDefenderView = new LastDefenderRunView(
						getApplicationContext(), null);
				setContentView(lastDefenderView);
			} else {
				lastDefenderView.reinit();
			}
		} else if (item.getItemId() == PREFERENCES) {
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
		}
		return true;
	}
}