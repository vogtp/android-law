package ch.bedesign.android.law.view.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import ch.almana.android.billing.products.BuyMeABeerProductsInitialiser;
import ch.almana.android.billing.view.activity.BillingProductListActiviy;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.helper.GuiUtils;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.products.FreeLawProducts;
import ch.bedesign.android.law.products.PaidLawProducts;

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

					FreeLawProducts.initDb(LawPreference.this);
					return false;
				}
			});
			findPreference("prefKeyShowDB").setOnPreferenceClickListener(new OnPreferenceClickListener() {

				public boolean onPreferenceClick(Preference preference) {
					openDbBrowser();
					return false;
				}
			});
		}

		findPreference(getString(R.string.prefKeyLanguage)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(LawPreference.this, (String) newValue);
				}
				return true;
			}

		});
		findPreference(getString(R.string.prefKeyBuyMeABeer)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				new BuyMeABeerProductsInitialiser().loadProductsIfNotLoaded(getApplicationContext());
				startActivity(BillingProductListActiviy.getIntent(LawPreference.this, LawBillingProductListActiviy.class, getString(R.string.prefBuyMeABeer),
						PaidLawProducts.PRODUCTS_BUYMEABEER));
				return true;
			}
		});

	}

	private void openDbBrowser() {
		try {
			installAppIfNotInstalled("oliver.ehrenmueller.dbadmin");
			String path = (new DB.OpenHelper(getApplicationContext())).getReadableDatabase().getPath();
			File f = new File(path);
			File d = new File(Environment.getExternalStorageDirectory() + "/" + DB.DATABASE_NAME);
			try {
				copyFile(f, d);
				Intent i = new Intent(Intent.ACTION_EDIT);
				i.setData(Uri.parse("sqlite:" + d.getAbsolutePath()));
				startActivity(i);
			} catch (IOException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e1) {
		}
	}

	void copyFile(File src, File dst) throws IOException {
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst);
		FileChannel inChannel = in.getChannel();
		FileChannel outChannel = out.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	private void installAppIfNotInstalled(String packageName) throws Exception {
		try {
			ApplicationInfo info = getPackageManager().
					getApplicationInfo(packageName, 0);
			return;
		} catch (PackageManager.NameNotFoundException e) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			Uri fromParts = Uri.parse("market://search?q=pname:" + packageName);
			i.setData(fromParts);
			startActivity(i);
			throw new Exception("installing");
		}
	}
}
