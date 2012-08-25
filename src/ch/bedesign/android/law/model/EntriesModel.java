package ch.bedesign.android.law.model;

import android.content.ContentValues;
import android.database.Cursor;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;

public class EntriesModel {

	private long id = -1;
	private long lawId;
	private long parentId;
	private String shortName;
	private String fullName;
	private String text;
	private long sequence;

	public EntriesModel(long lawId, long parentId, String shortName, String fullName, String text, long sequence) {
		super();
		this.lawId = lawId;
		this.parentId = parentId;
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

}
