package ch.bedesign.android.law.view.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;

public class DbUpdateProgressBar extends ProgressBar {

	private final ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			evaluateState();
		};

	};
	private long dbId = -1;
	private String[] selectionArgs;

	public DbUpdateProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialise();
	}

	public DbUpdateProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise();
	}

	public DbUpdateProgressBar(Context context) {
		super(context);
		initialise();
	}

	private void initialise() {
		//setVisibility(View.GONE);
	}

	public void listenForChange(long id) {
		if (id == dbId) {
			Logger.i("DbUpdateProgressBar already observing...");
			return;
		}
		this.dbId = id;
		this.selectionArgs = new String[] {Long.toString(dbId)};
		evaluateState();
		getContext().getContentResolver().registerContentObserver(Laws.CONTENT_URI, true, observer);
	}

	private void evaluateState() {
		Cursor c = null;
		try {
			c = getContext().getContentResolver().query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, selectionArgs, null);
			if (c != null && c.moveToFirst()) {
				LawModel l = new LawModel(c);
				boolean updating = l.isUpdating();
				if (Logger.DEBUG) {
					Logger.v("DbUpdateProgressBar.evaluateState updating: " + updating);
				}
				setIsUpdating(updating);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}

	public void setIsUpdating(boolean updating) {
		setVisibility(updating ? View.VISIBLE : View.GONE);
	}

}
