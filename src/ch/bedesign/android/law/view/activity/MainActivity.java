package ch.bedesign.android.law.view.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.adapter.SectionsPagerAdapter;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;

public class MainActivity extends FragmentActivity {

	private static final String STATE_FRAG_COUNT = "stateFragmentCount";

	private static final String STATE_FRAGMENT = "stateFragement";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager viewPager;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DbInitaliser.initDb(this);
		setContentView(R.layout.activity_main);
		if (Logger.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setSubtitle("DEBUG MODE" + " (" + SettingsLaw.getInstance(this).getVersionName() + ")");
		}
		viewPager = (ViewPager) findViewById(R.id.pager);
		sectionsPagerAdapter = new SectionsPagerAdapter(viewPager, getSupportFragmentManager());
		viewPager.setAdapter(sectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemClose:
			closeCurrentFragment();
			return true;

		case R.id.menu_settings:
			return true;

		}
		return false;
	}

	private void closeCurrentFragment() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem > 0) {
			viewPager.setCurrentItem(currentItem - 1);
			sectionsPagerAdapter.removePage(currentItem);
			viewPager.setAdapter(sectionsPagerAdapter);
		}
	}

	@Override
	public void onBackPressed() {
		if (!sectionsPagerAdapter.onBackPressed(viewPager.getCurrentItem())) {
			super.onBackPressed();
		}
	}

	public void addLawDisplay(LawModel lawModel) {
		sectionsPagerAdapter.addLawDisplay(lawModel);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int count = sectionsPagerAdapter.getCount();
		outState.putInt(STATE_FRAG_COUNT, count);
		for (int i = 1; i < count; i++) {
			Bundle b = new Bundle();
			sectionsPagerAdapter.getItem(i).onSaveInstanceState(b);
			String key = STATE_FRAGMENT + i;
			outState.remove(key);
			outState.putBundle(key, b);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int count = savedInstanceState.getInt(STATE_FRAG_COUNT);
		for (int i = 1; i < count; i++) {
			Bundle bundle = savedInstanceState.getBundle(STATE_FRAGMENT + i);
			LawDisplayFragment f = new LawDisplayFragment();
			f.setArguments(bundle);
			sectionsPagerAdapter.addFromBundle(bundle);
		}
	}

}
