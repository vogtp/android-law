package ch.bedesign.android.law.model;

import android.content.ContentValues;
import android.database.Cursor;
import ch.almana.android.util.StringUtils;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;

public class EntriesModel {

	private long id = -1;
	private long lawId;
	private long parentId;
	private String url;
	private String name;
	private String shortName;
	private String fullName;
	private String text;
	private long sequence;

	public EntriesModel(long lawId, long parentId) {
		super();
		this.lawId = lawId;
		this.parentId = parentId;
	}

	public EntriesModel(long lawId, long parentId, String url, String name, String shortName, String fullName, String text, long sequence) {
		super();
		this.lawId = lawId;
		this.parentId = parentId;
		this.url = url;
		this.name = name;
		this.shortName = shortName;
		this.fullName = fullName;
		this.text = text;
		this.sequence = sequence;
	}

	public EntriesModel(Cursor c) {
		super();
		this.id = c.getLong(DB.INDEX_ID);
		this.lawId = c.getLong(Entries.INDEX_LAW_ID);
		this.parentId = c.getLong(Entries.INDEX_PARENT_ID);
		this.url = c.getString(Entries.INDEX_URL);
		this.name = c.getString(Entries.INDEX_NAME);
		this.shortName = c.getString(Entries.INDEX_SHORT_NAME);
		this.fullName = c.getString(Entries.INDEX_FULL_NAME);
		this.text = c.getString(Entries.INDEX_TEXT);
		this.sequence = c.getLong(Entries.INDEX_SEQUENCE);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues(3);
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(Entries.NAME_LAW_ID, lawId);
		values.put(Entries.NAME_PARENT_ID, parentId);
		values.put(Entries.NAME_URL, url);
		values.put(Entries.NAME_NAME, name);
		values.put(Entries.NAME_SHORT_NAME, shortName);
		values.put(Entries.NAME_FULL_NAME, fullName);
		values.put(Entries.NAME_TEXT, text);
		values.put(Entries.NAME_SEQUENCE, sequence);
		return values;
	}
	public long getLawId() {
		return lawId;
	}

	public void setLawId(long lawId) {
		this.lawId = lawId;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		if (!StringUtils.isEmpty(fullName)) {
			return fullName;
		}
		if (!StringUtils.isEmpty(name)) {
			return name;
		}
		return super.toString();
	}

}
