package ch.bedesign.android.law.view.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.view.adapter.SectionsPagerAdapter;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
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
}
