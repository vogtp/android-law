package ch.bedesign.android.law.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.model.CountryModel;
import ch.bedesign.android.law.model.LawModel;

public class DbInitaliser {

	public static final String CODE_VERFASSUNG = "SR 101";
	public static final String CODE_OR = "220";

	public static final void initDb(Context ctx) {
		ContentResolver resolver = ctx.getContentResolver();

		// TODO make more robust
		

		if (hasThisLaw(resolver, CODE_VERFASSUNG)) {
			LawModel verfassung = new LawModel(CODE_VERFASSUNG, "Bundesverfassung  der Schweizerischen Eidgenossenschaft", CountryModel.CH_de.getId(),
					"18. April 1999 (Stand am 11. März 2012)", "http://www.admin.ch/ch/d/sr/101/index.html", -1);
			resolver.insert(Laws.CONTENT_URI, verfassung.getValues());
		}

		if (hasThisLaw(resolver, CODE_OR)) {
			LawModel or = new LawModel(CODE_OR, "Obligationenrecht", CountryModel.CH_de.getId(),
					"30. März 1911 (Stand am 1. März 2012)", "http://www.admin.ch/ch/d/sr/220/index.html", -1);
			resolver.insert(Laws.CONTENT_URI, or.getValues());
		}
		

	}

	private static boolean hasThisLaw(ContentResolver resolver, String code) {
		Cursor cursor = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, Laws.SELECTION_CODE, new String[] { code }, Laws.SORTORDER_DEFAULT);
		return cursor == null || cursor.getCount() == 0;
	}

}
