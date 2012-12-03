package ch.bedesign.android.law.access;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<Long, Object, Object> {
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final long UPDATE_INTERVALL_MILLIES = 10; //1000 * 60 * 60 * 24 * 7; Klein gehalten um zu testen SoupParser funktioniert ansonsten nicht (Da LastCheck noch nicht richtig implementiert ist todo Muriel) // once a week

	private final Context ctx;

	public LawUpdater(Context ctx) {
		super();
		this.ctx = ctx.getApplicationContext();
	}

	@Override
	protected Object doInBackground(Long... lawIds) {
		if (lawIds.length < 1) {
			return null;
		}
		for (Long lawId : lawIds) {
			if (lawId != null) {
				ContentResolver resolver = ctx.getContentResolver();
				Cursor c = null;
				try {
					c = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { Long.toString(lawId) },
							Laws.SORTORDER_DEFAULT);
					if (c != null && c.moveToFirst()) {
						updateLaw(resolver, c);
					}
				} catch (Exception e) {
					Logger.w("Cannot update law", e);
				} finally {
					if (c != null && !c.isClosed()) {
						c.close();
					}
				}
			}
		}
		return null;
	}

	private void updateLaw(ContentResolver resolver, Cursor c) throws ClientProtocolException, IOException {
		LawModel law = new LawModel(c);
		Parser parser = new Parser(resolver, law);
		String lawVersion = parser.getLawVersion();
		long now = System.currentTimeMillis();
		try {
			if (now < law.getLastCheck() + UPDATE_INTERVALL_MILLIES) {
				Logger.i("Not updating since the law is too new");
				return;
			}

		} catch (Exception e) {
			// we do not care
		}
		try {
			if (law.getVersion() != null && !law.getVersion().equals(lawVersion)) {
				law.setIsUpdating(now);
				resolver.update(Laws.CONTENT_URI, law.getValues(), DB.SELECTION_BY_ID, new String[] { Long.toString(law.getId()) });

				Logger.i("Parsing law " + law.getName() + " to version " + lawVersion);
				long start = now;
				resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
				parser.parse();

				Logger.i("Finished parsing law " + law.getName() + " to version " + lawVersion + " in " + sdf.format(now - start));
				law.setVersion(lawVersion);
			}
			law.setLastCheck(now);
		} finally {
			law.setIsUpdating(-1);
		}
		resolver.update(Laws.CONTENT_URI, law.getValues(), Laws.SELECTION_CODE, new String[] { law.getCode() });
	}


	public static void loadLaw(Context ctx, Long... lawIds) {
		LawUpdater task = new LawUpdater(ctx);
		task.execute(lawIds);
	}



}
