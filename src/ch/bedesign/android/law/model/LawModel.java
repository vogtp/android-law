package ch.bedesign.android.law.model;

import android.content.ContentValues;
import android.database.Cursor;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Laws;

public class LawModel {

	private static final long HOUR_IN_MILLIES = 1000 * 60 * 60;
	private long id = -1;
	private String code;
	private String name;
	private long countryId;
	private String version;
	private String url;
	private long lastCheck;
	private long isUpdating = -1;

	public LawModel(String code, String name, long countryId, String version, String url, long lastCheck) {
		super();
		this.code = code;
		this.name = name;
		this.countryId = countryId;
		this.version = version;
		this.url = url;
		this.lastCheck = lastCheck;
	}

	public LawModel(Cursor c) {
		super();
		this.id = c.getLong(DB.INDEX_ID);
		this.code = c.getString(Laws.INDEX_CODE);
		this.name = c.getString(Laws.INDEX_NAME);
		this.countryId = c.getLong(Laws.INDEX_COUNTRY_ID);
		this.version = c.getString(Laws.INDEX_VERSION);
		this.url = c.getString(Laws.INDEX_URL);
		this.lastCheck = c.getLong(Laws.INDEX_LAST_CHECK);
		this.setIsUpdating(c.getLong(Laws.INDEX_IS_UPDATING));
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues(7);
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(Laws.NAME_CODE, code);
		values.put(Laws.NAME_NAME, name);
		values.put(Laws.NAME_COUNTRY_ID, countryId);
		values.put(Laws.NAME_VERSION, version);
		values.put(Laws.NAME_URL, url);
		values.put(Laws.NAME_LAST_CHECK, lastCheck);
		values.put(Laws.NAME_IS_UPDATING, isUpdating);
		return values;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCountryId() {
		return countryId;
	}

	public void setCountryId(long countryId) {
		this.countryId = countryId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(long lastCheck) {
		this.lastCheck = lastCheck;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isUpdating() {
		if (System.currentTimeMillis() - isUpdating > HOUR_IN_MILLIES) {
			return false;
		}
		return true;
	}

	//	public long getIsUpdating() {
	//		return isUpdating;
	//	}

	public void setIsUpdating(long isUpdating) {
		this.isUpdating = isUpdating;
	}
}
