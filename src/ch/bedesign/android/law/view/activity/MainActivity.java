package ch.bedesign.android.law.view.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.adapter.SectionsPagerAdapter;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;
import ch.bedesign.android.law.view.fragment.SearchFragment;

public class MainActivity extends FragmentActivity {

	private static final String STATE_FRAG_COUNT = "STATE_FRAG_COUNT";
	private static final String STATE_FRAGMENT = "STATE_FRAGMENT";
	private static final String STATE_CURRENT_ITEM = "STATE_CURRENT_ITEM";
	private static final String FRAGMENT_CLASS_NAME = "FRAGMENT_CLASS_NAME";

	SectionsPagerAdapter sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager viewPager;
	private PagerTabStrip pagerTabStrip;
	private int currentItem = -1;
	private BroadcastReceiver searchResultReceiver;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (Logger.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setSubtitle("DEBUG MODE" + " (" + SettingsLaw.getInstance(this).getVersionName() + ")");
		}
		viewPager = (ViewPager) findViewById(R.id.pager);
		pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		//		pagerTabStrip.setDrawFullUnderline(true);
		sectionsPagerAdapter = new SectionsPagerAdapter(viewPager, getSupportFragmentManager());

		viewPager.setAdapter(sectionsPagerAdapter);

		Bundle restoreBundle = savedInstanceState;
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			restoreBundle = intent.getBundleExtra(SearchManager.APP_DATA);
			SearchFragment sf = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchManager.QUERY, searchQuery);
			sf.setArguments(args);
			sectionsPagerAdapter.addFragment(sf, getString(R.string.title_search) + " " + searchQuery);
			// set current item to search
			currentItem = 1;
			//			args.remove(STATE_CURRENT_ITEM);
			//			args.putInt(STATE_CURRENT_ITEM, 1);
			//			viewPager.setCurrentItem(1);
			getIntent().setAction(Intent.ACTION_DEFAULT);
		}
		restoreFromBundle(restoreBundle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (currentItem > 0) {
			viewPager.setCurrentItem(currentItem);
			currentItem = -1;
		}
		IntentFilter displaySearchResult = new IntentFilter(SearchFragment.ACTION_DISPLAY_SEARCH_RESULTS);
		searchResultReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				addLawDisplayFragment(intent.getExtras());
			}
		};
		registerReceiver(searchResultReceiver , displaySearchResult);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(searchResultReceiver);
		super.onPause();
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

		case R.id.itemSearch:
			Bundle bundle = new Bundle();
			saveToBundle(bundle);
			String initalQuery = null;
			if (Logger.DEBUG) {
				initalQuery = "Mord";
			}
			startSearch(initalQuery, true, bundle, false);
			return true;

		case R.id.menu_settings:
			startActivity(new Intent(this, LawPreference.class));
			return true;

		}
		return false;
	}

	private void closeCurrentFragment() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem > 0) {

			//			sectionsPagerAdapter.destroyItem(viewPager, currentItem, viewPager.getChildAt(currentItem));
			//			viewPager.removeViewAt(currentItem);
			viewPager.setCurrentItem(currentItem - 1);
			sectionsPagerAdapter.removePage(currentItem);
			viewPager.setAdapter(sectionsPagerAdapter);
		}
	}

	@Override
	public void onBackPressed() {

		if (!sectionsPagerAdapter.onBackPressed()) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.titleCloseDialog);
			builder.setMessage(R.string.msgCloseDialog);
			builder.setPositiveButton(android.R.string.yes, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.setNeutralButton(R.string.buNeutralCloseDialog, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					viewPager.setCurrentItem(0);
				}
			});
			builder.setNegativeButton(android.R.string.no, null);
			builder.create().show();
		}
	}

	public void addLawDisplay(LawModel lawModel) {
		sectionsPagerAdapter.addLawDisplay(lawModel);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveToBundle(outState);
	}

	private void saveToBundle(Bundle outState) {
		outState.putInt(STATE_CURRENT_ITEM, viewPager.getCurrentItem());
		int count = sectionsPagerAdapter.getCount();
		outState.putInt(STATE_FRAG_COUNT, count);
		for (int i = 1; i < count; i++) {
			Bundle b = new Bundle();
			Fragment fragment = sectionsPagerAdapter.getItem(i);
			if (fragment != null) {
				fragment.onSaveInstanceState(b);
				String key = STATE_FRAGMENT + i;
				outState.remove(key);
				outState.putString(key + FRAGMENT_CLASS_NAME, fragment.getClass().getName());
				outState.putBundle(key, b);
			}
		}
	}

	private void restoreFromBundle(Bundle bundle) {
		if (bundle == null) {
			return;
		}
		int count = bundle.getInt(STATE_FRAG_COUNT);
		for (int i = 1; i < count; i++) {
			String key = STATE_FRAGMENT + i;
			Bundle fragmentBundle = bundle.getBundle(key);
			String fragmentClassName = bundle.getString(key + FRAGMENT_CLASS_NAME);
			if (LawDisplayFragment.class.getName().equals(fragmentClassName)) {
				addLawDisplayFragment(fragmentBundle);
			} else if (SearchFragment.class.getName().equals(fragmentClassName)) {
				SearchFragment f = new SearchFragment();
				f.setArguments(fragmentBundle);
				final String sq = fragmentBundle.getString(SearchManager.QUERY);
				sectionsPagerAdapter.addFragment(f, getString(R.string.title_search) + " " + sq);
			}
		}
		viewPager.setCurrentItem(bundle.getInt(STATE_CURRENT_ITEM));
		viewPager.getAdapter().notifyDataSetChanged();
		pagerTabStrip.setTextSpacing(pagerTabStrip.getTextSpacing());
	}

	private void addLawDisplayFragment(Bundle bundle) {
		LawDisplayFragment f = new LawDisplayFragment();
		f.setArguments(bundle);
		sectionsPagerAdapter.addFragment(f, ((LawModel) bundle.getParcelable(LawDisplayFragment.ARG_LAW)).getShortName());
	}

}
