package ch.bedesign.android.law.view.activity;

import ch.bedesign.android.law.R;
import ch.bedesign.android.law.R.xml;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LawPreference extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
		addPreferencesFromResource(R.xml.main_law_preference);
	}

}
