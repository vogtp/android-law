package ch.bedesign.android.law.view.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.model.LawModel;

public class SearchFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	private static final int QUERY_EXACT = 1;
	private static final int QUERY_INEXACT = 2;
	public static final String ACTION_DISPLAY_SEARCH_RESULTS = "ACTION_DISPLAY_SEARCH_RESULTS";
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
		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null,
				new String[] { DB.Entries.NAME_FULL_NAME, DB.Entries.NAME_NAME },
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);

		ViewBinder viewBinder = new ViewBinder() {

			public boolean setViewValue(View v, Cursor c, int idx) {
				if (idx == Entries.INDEX_NAME) {
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

	public String getName(Context ctx) {
		return ctx.getString(R.string.title_search) + " " + searchQuery;
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		CursorLoader entriesCursorLoader = new CursorLoader(getActivity(), Entries.CONTENT_URI, Entries.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
				new String[] { Long.toString(id) }, null);
		Cursor entriesCursor = entriesCursorLoader.loadInBackground();
		if (entriesCursor.moveToFirst()) {
			CursorLoader lawCursorLoader = new CursorLoader(getActivity(), Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
					new String[] { Long.toString(entriesCursor.getLong(Entries.INDEX_LAW_ID)) }, null);
			Cursor lawCursor = lawCursorLoader.loadInBackground();
			if (lawCursor.moveToFirst()) {
				LawModel law = new LawModel(lawCursor);
				Intent intent = new Intent(ACTION_DISPLAY_SEARCH_RESULTS);
				intent.putExtra(LawDisplayFragment.ARG_LAW, law);
				intent.putExtra(LawDisplayFragment.ARG_PARENT_ID, entriesCursor.getLong(Entries.INDEX_PARENT_ID));
				getActivity().sendBroadcast(intent);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.close_fragment_option, menu);
	};
}
