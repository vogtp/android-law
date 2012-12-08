package ch.bedesign.android.law.products;

import java.util.HashMap;

import android.content.Context;
import ch.almana.android.billing.backend.BillingManager.Managed;
import ch.almana.android.billing.backend.PurchaseListener;
import ch.almana.android.billing.products.BuyMeABeerProductsInitialiser;
import ch.almana.android.billing.products.Product;
import ch.almana.android.billing.products.ProductList;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.model.CountryModel;
import ch.bedesign.android.law.model.LawModel;

public class PaidLawProducts extends BuyMeABeerProductsInitialiser implements PurchaseListener {


	public static final int PRODUCTS_LIST_LAWS = 1;

	private HashMap<String, LawModel> lawProductsList;

	private Context ctx;

	/**
	 * Initialise paid law products here
	 * 
	 * @param ctx
	 *            Context as from getContext()
	 */
	private void initLawProductList(Context ctx) {
		if (lawProductsList != null) {
			return;
		}
		lawProductsList = new HashMap<String, LawModel>();
		lawProductsList.put("law.VStrR", getLaw(ctx, LawCodes.VSTRR, "Verwaltungsstrafrecht", "VStrR", "http://www.admin.ch/ch/d/sr/313_0/"));
		lawProductsList.put("law.StPO", getLaw(ctx, LawCodes.STPO, "Strafprozessordnung", "StPO", "http://www.admin.ch/ch/d/sr/312_0/"));
		//	lawProductsList.put("law.", getLaw(ctx, LawCodes., "", "", ""));
	}

	// hide default constructor 
	private PaidLawProducts() {
		super();
	}

	public PaidLawProducts(Context ctx) {
		this();
		this.ctx = ctx.getApplicationContext();
	}

	@Override
	protected void addProductListsToManager(Context ctx) {
		productManager.addProducts(PRODUCTS_LIST_LAWS, getLaws(ctx));
	}

	private ProductList getLaws(Context ctx) {
		initLawProductList(ctx);
		ProductList productList = new ProductList();
		for (String lawProdId : lawProductsList.keySet()) {
			LawModel law = lawProductsList.get(lawProdId);
			productList.add(new Product(lawProdId, law.getShortName(), law.getName(), Managed.MANAGED));
		}
		return productList;

	}

	private LawModel getLaw(Context ctx, String code, String name, String shortName, String url) {
		return new LawModel(code, name, shortName, CountryModel.CH_de.getId(), ctx.getString(R.string.msg_law_not_yet_loaded), url, -1);
	}

	public void purchaseChanged(String pid, int count) {
		if (count < 1) {
			return;
		}
		initLawProductList(ctx);
		LawModel law = lawProductsList.get(pid);
		ctx.getContentResolver().insert(Laws.CONTENT_URI, law.getValues());

	}

	public void billingSupported(boolean supported) {
	}
}
