package ch.bedesign.android.law.view.fragment;

import java.util.Stack;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;

public class LawDisplayFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	public static class ParentIdList extends Stack<Long> implements Parcelable {

		private static final long serialVersionUID = -2403247172596842895L;

		public ParentIdList() {
			super();
		}

		private ParentIdList(Parcel in) {
			this();
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				push(in.readLong());
			}
		}

		@Override
		public synchronized String toString() {
			return getClass().toString() + " #Entries: " + size();
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			int size = size();
			out.writeInt(size);
			for (int i = 0; i < size; i++) {
				out.writeLong(get(i));
			}
		}

		public static final Parcelable.Creator<ParentIdList> CREATOR = new Parcelable.Creator<ParentIdList>() {
			public ParentIdList createFromParcel(Parcel in) {
				return new ParentIdList(in);
			}

			public ParentIdList[] newArray(int size) {
				return new ParentIdList[size];
			}
		};
	}

	public static final String ARG_LAW_ID = "lawId";
	public static final String ARG_LAW_NAME = "lawName";
	public static final String ARG_LAW = "lawParcel";
	private static final String ARG_PARENT_ID = "parentId";
	private static final String ARG_PARENT_ID_STACK = "parentIdStack";
	private SimpleCursorAdapter adapter;
	private LawModel law = LawModel.DUMMY;
	private long lawId;
	private String lawName;
	private long parentId = -1;
	private ParentIdList parentIds = new ParentIdList();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
		Bundle args;
		if (savedInstanceState != null) {
			args = savedInstanceState;
		} else {
			args = getArguments();
		}
		law = (LawModel) args.getParcelable(ARG_LAW);
		if (law == null) {
			law = LawModel.DUMMY;
		}
		lawId = args.getLong(ARG_LAW_ID);
		lawName = args.getString(ARG_LAW_NAME);
		if (args.containsKey(ARG_PARENT_ID)) {
			parentId = args.getLong(ARG_PARENT_ID);
			Logger.v("got parentID=" + parentId);
		}
		if (args.containsKey(ARG_PARENT_ID_STACK)) {
			Parcelable parcel = args.getParcelable(ARG_PARENT_ID_STACK);
			if (parcel instanceof ParentIdList) {
				parentIds = (ParentIdList) parcel;
			} else {
				String msg = "Something went wrong with the parent IDs ->" + parcel.getClass().toString();
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				Logger.e(msg);
			}
		}
		LawUpdater.loadLaw(getActivity().getApplicationContext(), getLawId());

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.law_display_list_item, null,
				new String[] { DB.Entries.NAME_SHORT_NAME, DB.Entries.NAME_TEXT },
				new int[] { R.id.tvLawTitle, R.id.tvLawText }, 0);
		ViewBinder binder = new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int idx) {
				if (DB.Entries.INDEX_TEXT == idx) {
					if (view instanceof TextView) {
						String string = cursor.getString(idx);
						if (string != null) {
							Spanned fromHtml = Html.fromHtml(string);
							TextView tv = (TextView) view;
							tv.setText(fromHtml);
							view.setVisibility(View.VISIBLE);
							((View) view.getParent()).findViewById(R.id.tvLawTitle).setVisibility(View.GONE);
							return true;
						}
					}
				} else if (DB.Entries.INDEX_SHORT_NAME == idx) {
					if (view instanceof TextView) {
						String string = cursor.getString(idx);
						if (string != null) {
							Spanned fromHtml = Html.fromHtml(string);
							TextView tv = (TextView) view;
							tv.setText(fromHtml);
							view.setVisibility(View.VISIBLE);
							((View) view.getParent()).findViewById(R.id.tvLawText).setVisibility(View.GONE);
							return true;
						}
					}
				}
				return false;
			}

		};
		adapter.setViewBinder(binder);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		parentIds.push(parentId);
		parentId = id;
		getLoaderManager().restartLoader(0, null, this);
	}

	public String getName() {
		return getLawName();
	}

	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		String[] args = new String[] { Long.toString(getLawId()), Long.toString(parentId) };
		return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.Entries.SELECTION_LAW_PARENT, args, DB.Entries.SORTORDER_DEFAULT);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (c == null || c.getCount() < 1) {
			getLoaderManager().restartLoader(0, null, this);
			return;
		}
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	private long getLastParent() {
		if (parentIds.size() < 1) {
			return parentId;
		}
		return parentIds.pop();
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	public boolean onBackPressed() {
		if (parentId == -1) {
			return false;
		}
		parentId = getLastParent();
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ARG_LAW, law);
		outState.putLong(ARG_LAW_ID, getLawId());
		outState.putLong(ARG_PARENT_ID, parentId);
		outState.putString(ARG_LAW_NAME, getLawName());
		outState.putParcelable(ARG_PARENT_ID_STACK, parentIds);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list_option, menu);
	};

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getLawName();
	}

	private Long getLawId() {
		return law.getId();
	}

	private String getLawName() {
		return law.getName();
	}
}
