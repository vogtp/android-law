package ch.bedesign.android.law.view.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.helper.GuiUtils;

public class LawPreference extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.main_law_preference);

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
