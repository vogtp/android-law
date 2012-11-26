package ch.bedesign.android.law.view.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

public class MainActivity extends FragmentActivity {

	private static final String STATE_FRAG_COUNT = "STATE_FRAG_COUNT";
	private static final String STATE_FRAGMENT = "STATE_FRAGMENT";
	private static final String STATE_CURRENT_ITEM = "STATE_CURRENT_ITEM";

	SectionsPagerAdapter sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager viewPager;
	private PagerTabStrip pagerTabStrip;

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
		outState.putInt(STATE_CURRENT_ITEM, viewPager.getCurrentItem());
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
		viewPager.setCurrentItem(savedInstanceState.getInt(STATE_CURRENT_ITEM));
		viewPager.getAdapter().notifyDataSetChanged();
		pagerTabStrip.setTextSpacing(pagerTabStrip.getTextSpacing());
	}

}
