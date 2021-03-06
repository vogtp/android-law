package ch.bedesign.android.law.view.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.activity.MainActivity;
import ch.bedesign.android.law.view.widget.DbUpdateProgressBar;

public class LawsOverviewFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		setListShown(false);
		getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.lawRed)));
		//		getListView().setDivider(getResources().getDrawable(R.drawable.divider_shape));
		//		int[] colors = { 380003, getResources().getColor(R.color.lawRed), 380003 }; // red for the example
		//		GradientDrawable divider = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		//		getListView().setDivider(divider);
		getListView().setDividerHeight(1);
		getListView().setBackgroundColor(Color.BLACK);

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.law_overview_list_item, null,
				new String[] { DB.Laws.NAME_NAME, DB.Laws.NAME_VERSION },
				new int[] { R.id.tvLawTitle, R.id.tvInfo }, 0);
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View v, Cursor c, int colIdx) {
				if (colIdx == DB.Laws.INDEX_VERSION) {
					LawModel law = new LawModel(c);
					if (law.isUpdating()) {
						((TextView) v).setText(R.string.msg_updating);
						return true;
					} else if (!law.isLoaded()) {
						((TextView) v).setText(R.string.msg_law_not_yet_loaded);
					}
					DbUpdateProgressBar pb = (DbUpdateProgressBar) ((ViewGroup) v.getParent()).findViewById(R.id.dbUpdateProgressBar);
					pb.setIsUpdating(law.isUpdating());
				}
				return false;
			}
		});
		setListAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
		//		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		if (cursor.moveToPosition(position)) {
			Activity act = getActivity();
			if (act != null && act instanceof MainActivity) {
				((MainActivity) act).addLawDisplay(new LawModel(cursor));
			}

		}
	}

	public String getName(Context ctx) {
		return ctx.getString(R.string.frag_title_gesetze);
	}

	
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		return new CursorLoader(getActivity(), DB.Laws.CONTENT_URI, DB.Laws.PROJECTION_DEFAULT, null, null, DB.Laws.SORTORDER_DEFAULT);
	}

	
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);

		while (c.moveToNext()) {
			LawModel law = new LawModel(c);
			if (law.isUpdating()) {
				if (SettingsLaw.getInstance(getActivity()).isContinueUpdatesAtStartup()) {
					LawUpdater.loadLaw(getActivity(), law.getId());
				}
			}
		}

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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.overview_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Logger.e("bad menuInfo", e);
			return false;
		}

		//		final Uri uri = ContentUris.withAppendedId(Laws.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.itemClearCache:
			ContentResolver resolver = getActivity().getContentResolver();
			String[] selectionArgs = new String[] { Long.toString(info.id) };
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, selectionArgs);
			Cursor cursor = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				LawModel law = new LawModel(cursor);
				law.setVersion(getString(R.string.msg_law_not_yet_loaded));
				law.setIsUpdating(-1);
				law.setLastCheck(-1);
				resolver.update(Laws.CONTENT_URI, law.getValues(), DB.SELECTION_BY_ID, selectionArgs);
			}

			return true;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.law_add, menu);
	};
}
