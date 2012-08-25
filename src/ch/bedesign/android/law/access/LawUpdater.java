package ch.bedesign.android.law.access;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import ch.bedesign.android.law.access.LawUpdater.LawUpdateCallback;
import ch.bedesign.android.law.access.LawUpdater.LoadResult;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<LawModel, LawUpdateCallback, LoadResult> {

	public interface LawUpdateCallback {
		public Context getContext();
	}


	public class LoadResult {
		private boolean ok = false;

		public LoadResult(boolean ok) {
			super();
			this.ok = ok;
		}

	}

	private final LawUpdateCallback callback;

	public LawUpdater(LawUpdateCallback callback) {
		super();
		this.callback = callback;
	}

	@Override
	protected LoadResult doInBackground(LawModel... params) {
		if (params.length < 1) {
			return new LoadResult(false);
		}
		for (LawModel param : params) {
			if (param != null) {
				updateLaw(param);
			}
		}
		return new LoadResult(true);
	}

	private void updateLaw(LawModel law) {
		ContentResolver resolver = callback.getContext().getContentResolver();
		long lawId = law.getId();
		if (DbInitaliser.CODE_VERFASSUNG.equals(law.getCode())) {
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			long p = insert(resolver, new EntriesModel(lawId, -1, "Präambel", "Präambel", null, 0));
			insert(resolver, new EntriesModel(lawId, p, "Präambel", "Präambel", "Im Namen Gottes des Allmächtigen!\n" +
					"\n" +
					"Das Schweizervolk und die Kantone,\n" +
					"\n" +
					"in der Verantwortung gegenüber der Schöpfung,\n" +
					"\n" +
					"im Bestreben, den Bund zu erneuern, um Freiheit und Demokratie, Unabhängigkeit und Frieden in Solidarität und Offenheit gegenüber der Welt zu stärken,\n" +
					"\n" +
					"im Willen, in gegenseitiger Rücksichtnahme und Achtung ihre Vielfalt in der Einheit zu leben,\n" +
					"\n" +
					"im Bewusstsein der gemeinsamen Errungenschaften und der Verantwortung gegenüber den künftigen Generationen,\n" +
					"\n" +
					"gewiss, dass frei nur ist, wer seine Freiheit gebraucht, und dass die Stärke des Volkes sich misst am Wohl der Schwachen,\n" +
					"\n" +
					"geben sich folgende Verfassung:", 0));
			long t1 = insert(resolver, new EntriesModel(lawId, -1, "1. Titel", "1. Titel: Allgemeine Bestimmungen", null, 1));
			long t2 = insert(resolver, new EntriesModel(lawId, -1, "2. Titel",
					"2. Titel: Grundrechte, Bürgerrechte und Sozialziele",
					"ein text", 2));
			long t2_k1 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 1. Kapitel", "1. Kapitel: Grundrechte", null, 3));
			long t2_k2 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 2. Kapitel", "2. Kapitel: Bürgerrecht und politische Rechte",
					null, 4));
			long t2_k3 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 3. Kapitel", "3. Kapitel: Sozialziele", null, 5));
			long t3 = insert(resolver, new EntriesModel(lawId, -1, "3. Titel", "3. Titel: Bund, Kantone und Gemeinden", "text", 6));
			long t3_k1 = insert(resolver, new EntriesModel(lawId, t3, "3. Titel 1. Kapitel", "1. Kapitel: Verhältnis von Bund und Kantonen",
					null, 7));
			long t3_k2 = insert(resolver, new EntriesModel(lawId, t3, "3. Titel 2. Kapitel", "2. Kapitel: Zuständigkeiten", null, 8));
		}

	}

	private long insert(ContentResolver resolver, EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	public static void loadLaw(LawUpdateCallback callback, LawModel... laws) {
		LawUpdater task = new LawUpdater(callback);
		task.execute(laws);
	}

}
