package ch.bedesign.android.law.view.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import ch.bedesign.android.law.R;

public class LawPreference extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.main_law_preference);

	}

}
