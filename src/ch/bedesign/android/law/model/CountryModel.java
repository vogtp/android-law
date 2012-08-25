package ch.bedesign.android.law.model;



public class CountryModel {

	public final static CountryModel CH_de = new CountryModel(1, "Swiss German", "de_CH");

	private final long id;
	private final String name;
	private final String iso2code;

	private CountryModel(long id, String name, String iso2code) {
		super();
		this.id = id;
		this.name = name;
		this.iso2code = iso2code;
	}

	//	public CountryModel(String name, String iso2code) {
	//		super();
	//		this.name = name;
	//		this.iso2code = iso2code;
	//	}

	//	public CountryModel(Cursor c) {
	//		super();
	//		this.id = c.getLong(DB.INDEX_ID);
	//		this.name = c.getString(Countries.INDEX_NAME);
	//		this.iso2code = c.getString(Countries.INDEX_ISO2_CODE);
	//	}
	//
	//	public ContentValues getValues() {
	//		ContentValues values = new ContentValues(3);
	//		if (id > -1) {
	//			values.put(DB.NAME_ID, id);
	//		}
	//		values.put(Countries.NAME_NAME, name);
	//		values.put(Countries.NAME_ISO2_CODE, iso2code);
	//		return values;
	//	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIso2code() {
		return iso2code;
	}

	//	public void setName(String name) {
	//		this.name = name;
	//	}
	//
	//	public void setIso2code(String iso2code) {
	//		this.iso2code = iso2code;
	//	}
}
