package ch.bedesign.android.law.view.fragment;

import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;

public class SearchFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	private static final int QUERY_EXACT = 1;
	private static final int QUERY_INEXACT = 2;
	private String searchQuery;
	private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Bundle args = getArguments();
		if (savedInstanceState != null) {
			args = savedInstanceState;
		}
		searchQuery = args.getString(SearchManager.QUERY);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShown(false);
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[] { DB.Entries.NAME_SHORT_NAME },
				new int[] { android.R.id.text1 }, 0);

		ViewBinder viewBinder = new ViewBinder() {

			public boolean setViewValue(View v, Cursor c, int idx) {
				if (idx == Entries.INDEX_SHORT_NAME) {
					((TextView) v).setText(Html.fromHtml(c.getString(idx)));
					return true;
				}
				return false;
			}
		};
		adapter.setViewBinder(viewBinder);
		setListAdapter(adapter);
		getLoaderManager().initLoader(QUERY_EXACT, null, this);
	}

	public String getName() {
		return getString(R.string.title_search) + " " + searchQuery;
	}

	public boolean onBackPressed() {
		return false;
	}

	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		String[] searchArgs;
		switch (loaderId) {
		case QUERY_INEXACT:
			searchArgs = new String[] { "% " + searchQuery + " %" };
			break;

		case QUERY_EXACT:
		default:
			searchArgs = new String[] { "%" + searchQuery + "%" };
			break;
		}
		return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.Entries.SELECTION_TEXT_SEARCH, searchArgs,
				DB.Entries.SORTORDER_DEFAULT);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (loader.getId() == QUERY_EXACT && !c.moveToFirst()) {
			getLoaderManager().initLoader(QUERY_INEXACT, null, this);
			return;
		}
		adapter.swapCursor(c);
		setListShown(true);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SearchManager.QUERY, searchQuery);
	}

}
