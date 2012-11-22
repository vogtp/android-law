package ch.bedesign.android.law.view.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DbInitaliser;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		DbInitaliser.initDb(this);
        setContentView(R.layout.activity_main);
		viewPager = (ViewPager) findViewById(R.id.pager);
		sectionsPagerAdapter = new SectionsPagerAdapter(viewPager, getSupportFragmentManager());
		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
		}
		viewPager.setAdapter(sectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
//			Parcelable state = sectionsPagerAdapter.saveState();
//			outState.putParcelable(STATE_FRAGMENT, state);
			int count = sectionsPagerAdapter.getCount();
			outState.putInt(STATE_FRAG_COUNT, count);
			for (int i = 1; i < count; i++) {
				Bundle b = new Bundle();
				sectionsPagerAdapter.getItem(i).onSaveInstanceState(b);
				outState.putBundle(STATE_FRAGMENT + i, b);
		}
		}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//			sectionsPagerAdapter.restoreState(savedInstanceState.getParcelable(STATE_FRAGMENT), getClassLoader());
		int count = savedInstanceState.getInt(STATE_FRAG_COUNT);
		for (int i = 1; i < count; i++) {
			Bundle bundle = savedInstanceState.getBundle(STATE_FRAGMENT + i);
			LawDisplayFragment f = new LawDisplayFragment();
			f.setArguments(bundle);
			sectionsPagerAdapter.addFromBundle(bundle);
		}
	}

}
