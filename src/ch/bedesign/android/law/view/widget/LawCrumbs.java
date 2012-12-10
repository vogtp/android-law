package ch.bedesign.android.law.view.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.almana.android.util.StringUtils;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.model.EntriesModel;

public class LawCrumbs extends LinearLayout {

	private StringBuilder sbTemp;

	public LawCrumbs(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise();
	}

	public LawCrumbs(Context context) {
		super(context);
		initialise();
	}

	private void initialise() {
		setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
	}

	public void setEntityId(final long id) {
		CursorLoader clEntity = new CursorLoader(getContext(), Entries.CONTENT_URI, Entries.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { Long.toString(id) },
				Entries.SORTORDER_DEFAULT);
		Cursor cursorEntity = null;
		Cursor cursorLaw = null;
		sbTemp = new StringBuilder();
		removeAllViews();
		int i = 0;
		try {
			cursorEntity = clEntity.loadInBackground();

			if (cursorEntity.moveToFirst()) {
				long pid = cursorEntity.getLong(Entries.INDEX_PARENT_ID);
				EntriesModel em = new EntriesModel(cursorEntity);
				//				if (pid < 0) {
				//					// get law and return
				//					long lawId = cursorEntity.getLong(Entries.INDEX_LAW_ID);
				//					CursorLoader clLaw = new CursorLoader(getContext(), Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
				//							new String[] { Long.toString(lawId) }, Laws.SORTORDER_DEFAULT);
				//					cursorLaw = clLaw.loadInBackground();
				//					if (cursorLaw.moveToFirst()) {
				//						sbTemp.append(" ").append(cursorLaw.getString(Laws.INDEX_SHORT_NAME));
				//						addView(getShortTextView(sbTemp.toString()));
				//						//						addView(getShortTextView(cursorLaw.getString(Laws.INDEX_SHORT_NAME)), i++);
				//						return;
				//					}
				//				} else {
				String sn = cursorEntity.getString(Entries.INDEX_SHORT_NAME);
				if (StringUtils.isEmpty(sn)) {
					sn = cursorEntity.getString(Entries.INDEX_NAME);
				}
				sbTemp.append(" ").append(sn);
				//					addView(getShortTextView(cursorEntity.getString(Entries.INDEX_SHORT_NAME)), i++);
				setEntityId(pid);
				//				}
			}
			addView(getShortTextView(sbTemp.toString()));
		} finally {
			if (cursorEntity != null && !cursorEntity.isClosed()) {
				cursorEntity.close();
			}
			if (cursorLaw != null && !cursorLaw.isClosed()) {
				cursorLaw.close();
			}
		}
	}

	private View getShortTextView(String text) {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		TextView tv = new TextView(getContext());
		tv.setPadding(0, 2, 7, 2);
		tv.setEllipsize(TruncateAt.END);

		SpannableString content = new SpannableString(text);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tv.setEllipsize(TruncateAt.END);
		tv.setSingleLine(true);

		tv.setText(content);
		tv.setLayoutParams(layoutParams);
		tv.setTextColor(getContext().getResources().getColor(android.R.color.primary_text_light));
		return tv;
	}

}
