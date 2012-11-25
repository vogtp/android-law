package ch.bedesign.android.law.view.adapter;

import java.util.LinkedList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.fragment.ILawFragment;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;
import ch.bedesign.android.law.view.fragment.LawsOverviewFragment;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

	private final LinkedList<Fragment> pages = new LinkedList<Fragment>();
	private final ViewPager viewPager;
	private final FragmentManager fragmentManager;

	public SectionsPagerAdapter(ViewPager viewPager, FragmentManager fm) {
		super(fm);
		this.fragmentManager = fm;
		this.viewPager = viewPager;
		pages.clear();
		pages.add(0, new LawsOverviewFragment());
	}

	@Override
	public Fragment getItem(int i) {
		return pages.get(i);
	}

	@Override
	public int getCount() {
		return pages.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return ((ILawFragment) getItem(position)).getName();
	}

	public void addLawDisplay(LawModel lawModel) {
		Bundle args = new Bundle();
		Fragment fragment = new LawDisplayFragment();
		args.putParcelable(LawDisplayFragment.ARG_LAW, lawModel);
		fragment.setArguments(args);
		addFragment(fragment);
	}

	private void addFragment(Fragment fragment) {
		int pos = 1;
		if (SettingsLaw.getInstance().isInsertPageAtEnd()) {
			pos = pages.size();
		}
		pages.add(pos, fragment);
		viewPager.setCurrentItem(pos, true);
		notifyDataSetChanged();
	}

	public void addFromBundle(Bundle args) {
		Fragment fragment = new LawDisplayFragment();
		fragment.setArguments(args);
		fragment.setInitialSavedState(null);
		addFragment(fragment);
	}

	public boolean onBackPressed(int position) {
		return ((ILawFragment) getItem(position)).onBackPressed();
	}

	public void removePage(int pos) {
		if (pos > 0 && pos < pages.size()) {
			Fragment fragment = pages.remove(pos);
			destroyItem(viewPager, pos, fragment);
			//			FragmentTransaction transaction = fragmentManager.beginTransaction();
			//			transaction.remove(fragment);
			//			transaction.commitAllowingStateLoss();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

}