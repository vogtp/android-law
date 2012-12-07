package ch.bedesign.android.law.application;

import android.app.Application;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.products.FreeLawProducts;

public class LawApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		FreeLawProducts.initDb(this);
		//initalise settings
		SettingsLaw.getInstance(this);
	}

}
