package ch.bedesign.android.law.products;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.model.CountryModel;
import ch.bedesign.android.law.model.LawModel;

public class FreeLawProducts {

	/**
	 * Initialise law products which are free of chrage here
	 * 
	 * @param ctx
	 *            Context as from getContext()
	 */
	public static final void initDb(Context ctx) {
		// free laws:
		insertLawIfNotExists(ctx, LawCodes.EMRK, "Menschenrechte", "EMRK", "http://www.admin.ch/ch/d/sr/0_101/");
		insertLawIfNotExists(ctx, LawCodes.VERFASSUNG, "Bundesverfassung  der Schweizerischen Eidgenossenschaft", "BV", "http://www.admin.ch/ch/d/sr/101/");
		insertLawIfNotExists(ctx, LawCodes.OR, "Obligationenrecht", "OR", "http://www.admin.ch/ch/d/sr/220/");
		insertLawIfNotExists(ctx, LawCodes.ZGB, "Schweizerisches Zivilgesetzbuch", "ZGB", "http://www.admin.ch/ch/d/sr/210/");
		insertLawIfNotExists(ctx, LawCodes.STGB, "Schweizerisches Strafgesetzbuch", "StGB", "http://www.admin.ch/ch/d/sr/311_0/");

	}

	/**
	 * Helper to insert a law
	 * 
	 * @param ctx
	 *            Context as from getContext()
	 * @param code
	 *            a constant from {@link LawCodes} with the value of the
	 *            SR-Nummer having the name of the abbreviation of the law (e.g.
	 *            OR)
	 * @param name
	 *            The name of the law (e.g. Obligationenrecht)
	 * @param shortName
	 *            the abbreviation of the law (e.g. OR)
	 * @param url
	 *            A url string pointing to the page of the law (without
	 *            index.html if possible) (e.g.
	 *            http://www.admin.ch/ch/d/sr/220/)
	 */
	private static void insertLawIfNotExists(Context ctx, String code, String name, String shortName, String url) {
		ContentResolver resolver = ctx.getContentResolver();
		if (hasThisLaw(resolver, code)) {
			LawModel verfassung = new LawModel(code, name, shortName, CountryModel.CH_de.getId(), ctx.getString(R.string.msg_law_not_yet_loaded), url, -1);
			resolver.insert(Laws.CONTENT_URI, verfassung.getValues());
		}
	}

	/**
	 * Check if the law already exists
	 * 
	 * @param resolver
	 *            DB {@link ContentResolver} get with
	 *            getContext().getContentResolver() {@link ContentResolver}
	 * @param code
	 * @return
	 */
	private static boolean hasThisLaw(ContentResolver resolver, String code) {
		Cursor cursor = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, Laws.SELECTION_CODE, new String[] { code }, Laws.SORTORDER_DEFAULT);
		return cursor == null || cursor.getCount() == 0;
	}

}
