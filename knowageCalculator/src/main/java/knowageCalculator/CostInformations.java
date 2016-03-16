package knowageCalculator;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/costInformation")
public class CostInformations {

	static private Logger logger = Logger.getLogger(CostInformations.class);

	@POST
	@Path("/calculateCostISVorSubscription")
	@Produces("application/json")
	@Consumes("application/json")
	public String responseMsg(String body) {
		List<Double> prices = new ArrayList<Double>();
		JSONObject response = new JSONObject();

		try {
			logger.debug("/calculatecost rest service, received body=" + body);

			JSONObject jsonObj = new JSONObject(body);
			int numCores = Integer.parseInt(jsonObj.getString("selectedNumCores"));

			String modality = jsonObj.getString("modality");

			List<String> products = new ArrayList<String>();
			JSONArray selected = jsonObj.getJSONArray("selected");
			for (int i = 0; i < selected.length(); i++) {
				products.add(selected.getString(i));
			}
			/*
			 * for(int j=0;j<products.size();j++) { System.out.println(products.get(j)); }
			 */

			prices = calculatePrice(numCores, products, modality);

			response.put("goldPrice", prices.get(1)); // 1 is the index of goldPrice
			response.put("silverPrice", prices.get(0)); // 0 is the index of silverPrice
			return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * String formattedPrice=new DecimalFormat("#.##").format(price).replace(',', '.'); System.out.println(formattedPrice); return formattedPrice;
		 */

		return response.toString();

		// return price+"";
	}

	@POST
	@Path("/calculateCostOEMext")
	@Produces("application/json")
	@Consumes("application/json")
	public String responseMsgOEMext(String body) {
		JSONArray response = new JSONArray();

		try {
			logger.debug("/calculatecost rest service, received body=" + body);

			JSONObject jsonObj = new JSONObject(body);
			int numCores = Integer.parseInt(jsonObj.getString("selectedNumCores"));

			String modality = jsonObj.getString("modality");

			List<String> products = new ArrayList<String>();
			JSONArray selected = jsonObj.getJSONArray("selected");
			for (int i = 0; i < selected.length(); i++) {
				products.add(selected.getString(i));
			}
			/*
			 * for(int j=0;j<products.size();j++) { System.out.println(products.get(j)); }
			 */

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL input = classLoader.getResource("listino_knowage.v4_calcolaticeLAST.xls");
			String xlsFilePath = input.getFile().substring(1);
			logger.debug("reading xls file from:" + xlsFilePath);
			File f = new File(xlsFilePath);
			logger.debug("File exists:" + f.exists());
			FileInputStream fileInputStream = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
			HSSFSheet configurationSheet = workbook.getSheet("ConfigurationSheet");
			// sheetExternalOEM=workbook.getSheet("Prezzo OEM esterno");

			double minSaleForOEM = configurationSheet.getRow(0).getCell(1).getNumericCellValue(); // taken from excel sheet

			double costWithSubscriptionGold = 0, costWithSubscriptionSilver = 0;
			int[] categories = { 1, 20, 50, 100, 200, 300 }; // TODO: read categories from xls
			JSONObject jsonObject = null;

			if (modality.equals("OEM_EXT")) // Questo caso metterlo nella funzione super, che in caso di OEM_INT chiamerà calcolacosto e moltiplicherà per
											// cat!!!!
			{
				for (int cat : categories) {
					costWithSubscriptionGold = calculatePrice(numCores, products, "SUBSCRIPTION").get(1) * (1 - minSaleForOEM) * cat;
					costWithSubscriptionSilver = calculatePrice(numCores, products, "SUBSCRIPTION").get(0) * (1 - minSaleForOEM) * cat;
					jsonObject = new JSONObject();
					if (cat == 300) {
						jsonObject.put("Category", "Unlimited");
					} else {
						jsonObject.put("Category", cat + "");
					}
					jsonObject.put("goldPrice", costWithSubscriptionGold);
					jsonObject.put("silverPrice", costWithSubscriptionSilver);
					response.put(jsonObject);
				}

			} else // invalid modality
			{
				response = new JSONArray();
			}

			return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response.toString();

	}

	@POST
	@Path("/calculateCostOEMint")
	@Produces("application/json")
	@Consumes("application/json")
	public String responseMsgOEMint(String body) {
		String response = null;

		try {
			logger.debug("/calculatecost rest service, received body=" + body);

			JSONObject jsonObj = new JSONObject(body);
			int numCores = Integer.parseInt(jsonObj.getString("selectedNumCores"));

			String modality = jsonObj.getString("modality");

			List<String> products = new ArrayList<String>();
			JSONArray selected = jsonObj.getJSONArray("selected");
			for (int i = 0; i < selected.length(); i++) {
				products.add(selected.getString(i));
			}

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL input = classLoader.getResource("listino_knowage.v4_calcolaticeLAST.xls");
			String xlsFilePath = input.getFile().substring(1);
			logger.debug("reading xls file from:" + xlsFilePath);
			File f = new File(xlsFilePath);
			logger.debug("File exists:" + f.exists());
			FileInputStream fileInputStream = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
			HSSFSheet configurationSheet = workbook.getSheet("ConfigurationSheet");
			// sheetExternalOEM=workbook.getSheet("Prezzo OEM esterno");

			double minSaleForOEM = configurationSheet.getRow(0).getCell(1).getNumericCellValue(); // taken from excel sheet

			int[] categories = { 1, 20, 50, 100, 200, 300 }; // TODO: read categories from xls

			if (modality.equals("OEM_INT")) {
				double licensePercentageEII = configurationSheet.getRow(1).getCell(1).getNumericCellValue();
				double priceHypothesys = configurationSheet.getRow(2).getCell(1).getNumericCellValue();
				double minPriceOneUserOEM_INT = configurationSheet.getRow(6).getCell(1).getNumericCellValue();
				double basicPriceOneUser = Double.max(licensePercentageEII * priceHypothesys, minPriceOneUserOEM_INT);
				// System.out.println("basicPriceOneUser:"+basicPriceOneUser);
				double increasePercentageForGoldSubscription = configurationSheet.getRow(4).getCell(1).getNumericCellValue();
				Map<String, Set<String>> productSubfuncMap = initializeProductSubfuncMap(workbook.getSheet("Mappatura funzionalità"));
				Map<String, Double> subfuncWeightMap = initializeSubFuncWeightMap(workbook.getSheet("Mappatura funzionalità"));
				double referenceProductGroupWeight = 0;
				Set<String> referenceSubfunctionalities = new HashSet<String>();
				String[][] productConfigurations = { { "SI", "ER", "EI" }, { "BD" }, { "PM" }, { "PA" }, { "LI" }, { "OD" } };

				// -----------------------Initialize Reference Product Group parameters---------------------

				String[] referenceProductGroup = { "SI", "ER", "EI" };
				for (String p : referenceProductGroup) {
					Set<String> subfunc = productSubfuncMap.get(p);
					referenceSubfunctionalities.addAll(subfunc);
				}
				for (String subf : referenceSubfunctionalities) {
					double w = subfuncWeightMap.get(subf);
					referenceProductGroupWeight = referenceProductGroupWeight + w;
				}
				// System.out.println("REFERENCE_PRODUCT_GROUP_WEIGHT="+referenceProductGroupWeight); //Expected 55,7

				// ----------------------------------------------------------------------------------------

				JSONObject responseObj = new JSONObject();
				JSONArray silverTable = new JSONArray();
				JSONObject silverRow = new JSONObject();
				JSONArray goldTable = new JSONArray();
				JSONObject goldRow = new JSONObject();
				boolean basicProducts = true;

				for (String[] productGroup : productConfigurations) {

					Set<String> subfunctionalities = new HashSet<String>();
					silverRow = new JSONObject();
					goldRow = new JSONObject();

					String productGroupString = "";
					for (String p : productGroup) {
						Set<String> subfunc = productSubfuncMap.get(p);
						subfunctionalities.addAll(subfunc);
						productGroupString = productGroupString + "," + p;
					}
					productGroupString = productGroupString.substring(1);
					silverRow.put("products", productGroupString);
					goldRow.put("products", productGroupString);

					for (int cat : categories) {

						if (!basicProducts) {
							subfunctionalities.removeAll(referenceSubfunctionalities);
						}

						double deltaWeightsTot = 0;
						for (String subfunc : subfunctionalities) {
							deltaWeightsTot = deltaWeightsTot + subfuncWeightMap.get(subfunc);
						}

						double price = (basicPriceOneUser * deltaWeightsTot / referenceProductGroupWeight) * cat;
						if (cat == 300) {
							silverRow.put("Unlimited_max_number_of_clients_price", price);
							goldRow.put("Unlimited_max_number_of_clients_price", price * increasePercentageForGoldSubscription);

						} else {
							silverRow.put("max_" + cat + "_clients_price", price);
							goldRow.put("max_" + cat + "_clients_price", price * increasePercentageForGoldSubscription);
						}
					}
					silverTable.put(silverRow);
					goldTable.put(goldRow);

					if (basicProducts) {
						basicProducts = false;
					}
				}

				responseObj.put("silverTable", silverTable);
				responseObj.put("goldTable", goldTable);
				response = responseObj.toString();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	private List<Double> calculatePrice(int numCores, List<String> products, String modality) {
		HSSFWorkbook workbook = null;
		HSSFSheet sheetFunctionalities = null;
		HSSFSheet configurationSheet = null;
		List<Double> prices = new ArrayList<Double>();

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL input = classLoader.getResource("listino_knowage.v4_calcolaticeLAST.xls");
			String xlsFilePath = input.getFile().substring(1);

			logger.debug("reading xls file from:" + xlsFilePath);
			File f = new File(xlsFilePath);
			logger.debug("File exists:" + f.exists());

			FileInputStream fileInputStream = new FileInputStream(f);
			workbook = new HSSFWorkbook(fileInputStream);
			sheetFunctionalities = workbook.getSheet("Mappatura funzionalità");

			Map<String, Double> subFuncWeightMap = initializeSubFuncWeightMap(sheetFunctionalities);
			/*
			 * //For debugging Set<String> keys=costs.keySet(); for(String key: keys) { System.out.println(key+"--"+costs.get(s)); }
			 */

			Map<String, Set<String>> productSubfuncMap = initializeProductSubfuncMap(sheetFunctionalities);
			/*
			 * //For debugging Set<String> keys2=productSubfuncMap.keySet(); for(String key2:keys2) { System.out.println("KEY="+key2); for(Object
			 * s3:productSubfuncMap.get(key2)) { String subf=(String) s3; System.out.println(subf); }
			 * System.out.println("---------------------------------------------"); }
			 */

			Set<String> functionalitiesSet = new HashSet<String>();
			Set<String> tempSet = new HashSet<String>();
			for (String product : products) {
				tempSet = productSubfuncMap.get(product);
				functionalitiesSet.addAll(tempSet);
			}
			/*
			 * //For debugging for(String key4: functionalitiesSet) { System.out.println(key4); }
			 */

			double cost = 0;
			configurationSheet = workbook.getSheet("ConfigurationSheet");
			double functionalityBasicCost = configurationSheet.getRow(3).getCell(1).getNumericCellValue();
			double increasePercentageForGoldSubscription = configurationSheet.getRow(4).getCell(1).getNumericCellValue();
			double ISVsale = configurationSheet.getRow(5).getCell(1).getNumericCellValue();

			for (String functionality : functionalitiesSet) {
				cost = cost + subFuncWeightMap.get(functionality) * functionalityBasicCost;
			}

			double costWithNumCores = 0;
			switch (numCores) {
			case 4:
				costWithNumCores = cost * 50 / 100; // TODO linkare i valori percentuali ai valori del foglio xls (ancora non ci sono nell'xls)
				break;
			case 8:
				costWithNumCores = cost;
				break;
			case 12:
				costWithNumCores = cost * 150 / 100;
				break;
			case 16:
				costWithNumCores = cost * 200 / 100;
				break;
			case 20:
				costWithNumCores = cost * 250 / 100;
				break;
			case 24:
				costWithNumCores = cost * 300 / 100;
				break;
			}

			double costWithSubscriptionGold = 0;
			double costWithSubscriptionSilver = 0;

			if (modality.equals("SUBSCRIPTION")) {
				costWithSubscriptionGold = costWithNumCores * (1 + increasePercentageForGoldSubscription);
				costWithSubscriptionSilver = costWithNumCores;
				prices.add(costWithSubscriptionSilver);
				prices.add(costWithSubscriptionGold);
			} else if (modality.equals("ISV")) {
				costWithSubscriptionGold = (costWithNumCores * (1 + increasePercentageForGoldSubscription)) * (1 - ISVsale); // chiedere ad Angelo
				costWithSubscriptionSilver = (costWithNumCores) * (1 - ISVsale);
				prices.add(costWithSubscriptionSilver);
				prices.add(costWithSubscriptionGold);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return prices;
	}

	private Map<String, Double> initializeSubFuncWeightMap(HSSFSheet sheet) {
		Map<String, Double> subFuncWeightMap = new HashMap<String, Double>();
		HSSFRow row = null;
		HSSFCell weightCell = null;
		HSSFCell subFuncCell = null;

		double weight = 0;
		String subFunc = null;

		for (int rowIndex = 5; rowIndex <= sheet.getLastRowNum(); rowIndex++) // 5 is the first line where the functionalities values started on the table
		{
			row = sheet.getRow(rowIndex);
			if (row != null) {
				weightCell = row.getCell(1);
				subFuncCell = row.getCell(3);
				if (weightCell != null && subFuncCell != null) {
					weight = weightCell.getNumericCellValue();
					subFunc = subFuncCell.getStringCellValue();
					subFuncWeightMap.put(subFunc, weight);
				}
			}

		}

		return subFuncWeightMap;
	}

	private Map<String, Set<String>> initializeProductSubfuncMap(HSSFSheet sheet) {
		Map<String, Set<String>> productSubfuncMap = new HashMap<String, Set<String>>();

		Map<String, Integer> productColMap = new HashMap<String, Integer>();
		productColMap.put("BD", 9);
		productColMap.put("SI", 11);
		productColMap.put("ER", 13);
		productColMap.put("LI", 15);
		productColMap.put("PM", 17);
		productColMap.put("PA", 19);
		productColMap.put("OD", 21);
		productColMap.put("EI", 23);

		for (String product : productColMap.keySet()) {
			HSSFRow row = null;
			HSSFCell subFuncCell = null;
			HSSFCell xCell = null;
			int subFunctionColIdx = 3; // index of 'Sottofunzione' column on Excel sheet
			Set<String> subFunc = new HashSet<String>();

			for (int rowIndex = 5; rowIndex <= sheet.getLastRowNum(); rowIndex++) // 5 is the first line where the functionalities values started on the table
			{
				row = sheet.getRow(rowIndex);
				if (row != null) {
					subFuncCell = row.getCell(subFunctionColIdx);
					xCell = row.getCell(productColMap.get(product));

					if (subFuncCell != null && xCell != null) {
						if (xCell.getStringCellValue().equals("X")) {
							subFunc.add(subFuncCell.getStringCellValue());
						}
					}
				}

			}

			productSubfuncMap.put(product, subFunc);

		}

		return productSubfuncMap;
	}

}
