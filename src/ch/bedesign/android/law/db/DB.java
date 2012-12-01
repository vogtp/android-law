package ch.bedesign.android.law.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import ch.bedesign.android.law.db.DBProvider.UriTableMapping;
import ch.bedesign.android.law.log.Logger;

public interface DB {

	public static final String AUTHORITY = "ch.bedesign.android.law";
	public static final String DATABASE_NAME = "swissLaw";

	public static final String NAME_ID = "_id";
	public static final int INDEX_ID = 0;

	public static final String[] PROJECTION_ID = new String[] { NAME_ID };
	public static final String SELECTION_BY_ID = NAME_ID + "=?";

	public class UriTableConfig {

		public static UriTableMapping[] map = new UriTableMapping[] {
				Laws.URI_TABLE_MAPPING,
				Entries.URI_TABLE_MAPPING

		};

	}

	public class OpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 2;

		//		private static final String CREATE_COUNTRIES_TABLE = "create table if not exists " + Countries.TABLE_NAME + " (" + DB.NAME_ID
		//				+ " integer primary key, " + Countries.NAME_NAME + " text," + Countries.NAME_ISO2_CODE + " text);";

		private static final String CREATE_LAWS_TABLE = "create table if not exists " + Laws.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
				+ Laws.NAME_CODE + " text, " + Laws.NAME_NAME + " text, " + Laws.NAME_COUNTRY_ID + " long," + Laws.NAME_VERSION + " text, "
				+ Laws.NAME_URL + " text, " + Laws.NAME_LAST_CHECK + " long, " + Laws.NAME_IS_UPDATING + " long, " + Laws.NAME_SHORT_NAME + " text);";

		private static final String CREATE_ENTRYS_TABLE = "create table if not exists " + Entries.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
				+ Entries.NAME_LAW_ID + " long, " + Entries.NAME_NAME + " text, " + Entries.NAME_SHORT_NAME + " text, " + Entries.NAME_FULL_NAME + " text,"
				+ Entries.NAME_PARENT_ID + " long, "
				+ Entries.NAME_TEXT + " text, " + Entries.NAME_SEQUENCE + " int, " + Entries.NAME_URL + " text);";

		public OpenHelper(Context context) {
			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// db.execSQL(CREATE_COUNTRIES_TABLE);
			db.execSQL(CREATE_LAWS_TABLE);
			db.execSQL(CREATE_ENTRYS_TABLE);
			db.execSQL("create index law_cid_idx on " + Laws.TABLE_NAME + " (" + Laws.NAME_COUNTRY_ID + "); ");
			//			db.execSQL("create unique index country_iso_idx on " + Countries.TABLE_NAME + " (" + Countries.NAME_ISO2_CODE + "); ");
			db.execSQL("create index entry_lawid_idx on " + Entries.TABLE_NAME + " (" + Entries.NAME_LAW_ID + "); ");
			db.execSQL("create index entry_parent_idx on " + Entries.TABLE_NAME + " (" + Entries.NAME_PARENT_ID + "); ");
			Logger.i("Created tables");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				Logger.w("Upgrading to DB Version 2...");
				db.execSQL("alter table " + Laws.TABLE_NAME + " add column " + Laws.NAME_IS_UPDATING + " int;");
				// nobreak

			default:
				Logger.w("Finished DB upgrading!");
				break;
			}
		}

	}

	/*
		public interface Countries {

			static final String TABLE_NAME = "countries";
			public static final String CONTENT_ITEM_NAME = "country";

			public static final String NAME_NAME = "name";
			public static final String NAME_ISO2_CODE = "ISO2code";

			public static final int INDEX_NAME = 1;
			public static final int INDEX_ISO2_CODE = 2;

			public static final String[] colNames = new String[] { NAME_ID, NAME_NAME, NAME_ISO2_CODE };
			public static final String[] PROJECTION_DEFAULT = colNames;

			public static final String DEFAUL_SORTORDER = NAME_NAME + " DESC";

			static final String SORTORDER_REVERSE = NAME_NAME + " ASC";

			public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + CONTENT_ITEM_NAME;
			public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
			static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
			static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
			public static final UriTableMapping URI_TABLE_MAPPING = new UriTableMapping(CONTENT_URI, TABLE_NAME, CONTENT_ITEM_NAME, CONTENT_TYPE, CONTENT_ITEM_TYPE);
		}
	*/
	public interface Laws {
		static final String TABLE_NAME = "laws";
		public static final String CONTENT_ITEM_NAME = "law";

		public static final String NAME_CODE = "code";
		public static final String NAME_NAME = "name";
		public static final String NAME_COUNTRY_ID = "countryID";
		public static final String NAME_VERSION = "version";
		public static final String NAME_URL = "url";
		public static final String NAME_LAST_CHECK = "lastCheck";
		public static final String NAME_IS_UPDATING = "isUpdating";
		public static final String NAME_SHORT_NAME = "shortName";

		public static final int INDEX_CODE = 1;
		public static final int INDEX_NAME = 2;
		public static final int INDEX_COUNTRY_ID = 3;
		public static final int INDEX_VERSION = 4;
		public static final int INDEX_URL = 5;
		public static final int INDEX_LAST_CHECK = 6;
		public static final int INDEX_IS_UPDATING = 7;
		public static final int INDEX_SHORT_NAME = 8;

		public static final String[] PROJECTION_DEFAULT = new String[] { NAME_ID, NAME_CODE, NAME_NAME, NAME_COUNTRY_ID, NAME_VERSION, NAME_URL, NAME_LAST_CHECK, NAME_IS_UPDATING,
				NAME_SHORT_NAME };

		public static final String SORTORDER_DEFAULT = NAME_NAME + " DESC";
		public static final String SORTORDER_REVERSE = NAME_NAME + " ASC";

		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
		public static final UriTableMapping URI_TABLE_MAPPING = new UriTableMapping(CONTENT_URI, TABLE_NAME, CONTENT_ITEM_NAME, CONTENT_TYPE, CONTENT_ITEM_TYPE);

		static final String SELECTION_CODE = NAME_CODE + "=?";

	}

	public interface Entries {
		static final String TABLE_NAME = "entries";
		public static final String CONTENT_ITEM_NAME = "entry";

		public static final String NAME_LAW_ID = "lawId";
		/**
		 * The name as it would apear in the context e.g. Artikel 7 <br/>
		 * With a short explaining text <br/>
		 * Entspricht dem Variable shortText Aus dem Parser.
		 */
		public static final String NAME_NAME = "name";
		/**
		 * A short name e.g. Atr 7 or ง 2 without any text
		 */
		public static final String NAME_SHORT_NAME = "shortname";
		/**
		 * The name completely qualifying the entry <br>
		 * e.g. OR, ยง 2, Art. 9, Abs. b <br>
		 * TODO ask Muriel
		 */
		public static final String NAME_FULL_NAME = "fullName";
		public static final String NAME_PARENT_ID = "parentId";
		public static final String NAME_TEXT = "text";
		public static final String NAME_URL = "url";
		/**
		 * The sort order of the entries
		 */
		public static final String NAME_SEQUENCE = "sequence";

		public static final int INDEX_LAW_ID = 1;
		public static final int INDEX_SHORT_NAME = 2;
		public static final int INDEX_NAME = 3;
		public static final int INDEX_FULL_NAME = 4;
		public static final int INDEX_PARENT_ID = 5;
		public static final int INDEX_TEXT = 6;
		public static final int INDEX_SEQUENCE = 7;
		public static final int INDEX_URL = 8;

		public static final String[] PROJECTION_DEFAULT = new String[] { NAME_ID, NAME_LAW_ID, NAME_NAME, NAME_SHORT_NAME, NAME_FULL_NAME, NAME_PARENT_ID, NAME_TEXT,
				NAME_SEQUENCE, NAME_URL };

		public static final String SORTORDER_DEFAULT = NAME_SEQUENCE + " ASC";
		public static final String SORTORDER_REVERSE = NAME_SEQUENCE + " DESC";

		public static final String CONTENT_URI_STRING = "content://" + AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY + "." + CONTENT_ITEM_NAME;
		public static final UriTableMapping URI_TABLE_MAPPING = new UriTableMapping(CONTENT_URI, TABLE_NAME, CONTENT_ITEM_NAME, CONTENT_TYPE, CONTENT_ITEM_TYPE);
		static final String SELECTION_LAW = NAME_LAW_ID + "=?";
		static final String SELECTION_LAW_PARENT = NAME_LAW_ID + "=? and " + NAME_PARENT_ID + "=?";
		public static final String SELECTION_TEXT_SEARCH = NAME_TEXT + " like ?";

	}

}
