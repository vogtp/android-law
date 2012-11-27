package ch.bedesign.android.law.view.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.helper.GuiUtils;
import ch.bedesign.android.law.log.Logger;

public class LawPreference extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.main_law_preference);
		if (Logger.DEBUG) {
			addPreferencesFromResource(R.xml.debug_law_preferences);
			findPreference("prefKeyDeleteAll").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				public boolean onPreferenceClick(Preference preference) {
					Logger.w("DELETING ALL");
					Logger.w("Delete Laws");
					getContentResolver().delete(Laws.CONTENT_URI, null, null);
					Logger.w("Delete Entries");
					getContentResolver().delete(Entries.CONTENT_URI, null, null);

					DbInitaliser.initDb(LawPreference.this);
					return false;
				}
			});
		}

		findPreference("prefKeyLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(LawPreference.this, (String) newValue);
				}
				return true;
			}

		});
	}

}
