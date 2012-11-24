package ch.bedesign.android.law.helper;

public class SettingsLaw {

	private static SettingsLaw instance = new SettingsLaw();

	public static SettingsLaw getInstance() {
		return instance;
	}

	public boolean isInsertPageAtEnd() {
		// FIXME not yet working
		return true;
	}

}
