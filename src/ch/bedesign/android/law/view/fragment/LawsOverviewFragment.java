package ch.bedesign.android.law.view.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.adapter.SectionsPagerAdapter;

public class LawsOverviewFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	private final SectionsPagerAdapter pagerAdapter;

	public LawsOverviewFragment(SectionsPagerAdapter pagerAdapter) {
		this.pagerAdapter = pagerAdapter;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null,
				new String[] { DB.Laws.NAME_NAME, DB.Laws.NAME_VERSION },
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		if (cursor.moveToPosition(position)) {
			pagerAdapter.addLayDisplay(new LawModel(cursor));
		}
	}

	public String getName() {
		return "Gesetze";
	}

	
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		return new CursorLoader(getActivity(), DB.Laws.CONTENT_URI, DB.Laws.PROJECTION_DEFAULT, null, null, DB.Laws.SORTORDER_DEFAULT);
	}

	
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	
	public boolean onBackPressed() {
		return false;
	}
}