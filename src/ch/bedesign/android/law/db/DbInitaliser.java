package ch.bedesign.android.law.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.model.CountryModel;
import ch.bedesign.android.law.model.LawModel;

public class DbInitaliser {

	private static final String CODE_VERFASSUNG = "101";
	private static final String CODE_OR = "220";
	private static final String CODE_ZGB = "210";
	private static final String CODE_STGB = "311_0";

	public static final void initDb(Context ctx) {

		insertLawIfNotExists(ctx, "0_101", "Menschenrechte", "EMRK", "http://www.admin.ch/ch/d/sr/0_101/");
		insertLawIfNotExists(ctx, CODE_VERFASSUNG, "Bundesverfassung  der Schweizerischen Eidgenossenschaft", "BV", "http://www.admin.ch/ch/d/sr/101/");
		insertLawIfNotExists(ctx, CODE_OR, "Obligationenrecht", "OR", "http://www.admin.ch/ch/d/sr/220/index.html");
		insertLawIfNotExists(ctx, CODE_ZGB, "Schweizerisches Zivilgesetzbuch", "ZGB", "http://www.admin.ch/ch/d/sr/210/");
		insertLawIfNotExists(ctx, CODE_STGB, "Schweizerisches Strafgesetzbuch", "StGB", "http://www.admin.ch/ch/d/sr/311_0/");

	}

	private static void insertLawIfNotExists(Context ctx, String code, String name, String shortName, String url) {
		ContentResolver resolver = ctx.getContentResolver();
		if (hasThisLaw(resolver, code)) {
			LawModel verfassung = new LawModel(code, name, shortName, CountryModel.CH_de.getId(), ctx.getString(R.string.msg_law_not_yet_loaded), url, -1);
			resolver.insert(Laws.CONTENT_URI, verfassung.getValues());
		}
	}

	private static boolean hasThisLaw(ContentResolver resolver, String code) {
		Cursor cursor = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, Laws.SELECTION_CODE, new String[] { code }, Laws.SORTORDER_DEFAULT);
		return cursor == null || cursor.getCount() == 0;
	}

}
