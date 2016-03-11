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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/costInformation")
public class costIinformations {


	/*
	@GET
	@Path("/{pathParameter}")
	public Response responseMsg( @PathParam("pathParameter") String pathParameter,
			@DefaultValue("Nothing to say") @QueryParam("queryParameter") String queryParameter) {

		String response = "Hello from: " + pathParameter + " : " + queryParameter;

		return Response.status(200).entity(response).build();
	}

	*/

	@POST
	@Path("/calculateCost")
	@Produces("application/json")
	@Consumes("application/json")
	public String responseMsg(String body )
	{
		List<Double> prices=new ArrayList<Double>();
		JSONObject response=new JSONObject();

		try
		{
			System.out.println(body);

			JSONObject jsonObj=new JSONObject(body);
			int numCores=Integer.parseInt(jsonObj.getString("selectedNumCores"));
			System.out.println(numCores);

			String modality=jsonObj.getString("modality");
			System.out.println(modality);

			List<String> products=new ArrayList<String>();
			JSONArray selected=jsonObj.getJSONArray("selected");
			for(int i=0;i<selected.length();i++)
			{
				products.add(selected.getString(i));
			}
			/*
				for(int j=0;j<products.size();j++)
				{
				System.out.println(products.get(j));
				}
			*/

			prices=calculatePrice(numCores,products,modality);

			response.put("goldPrice", prices.get(1));   // 1 is the index of goldPrice
			response.put("silverPrice", prices.get(0));	// 0 is the index of silverPrice
			return response.toString();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		/*String formattedPrice=new DecimalFormat("#.##").format(price).replace(',', '.');
		System.out.println(formattedPrice);
		return formattedPrice;*/

		return response.toString();


		//return price+"";
	}



	private List<Double> calculatePrice(int numCores, List<String> products, String modality)
	{
		HSSFWorkbook workbook=null;
		HSSFSheet sheetFunctionalities=null;
		HSSFSheet sheetSubscription=null;
		HSSFSheet sheetExternalOEM=null;
		List<Double> prices=new ArrayList<Double>();

		try
		{
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL input = classLoader.getResource("listino_knowage.v4.xls");
			String xlsFilePath=input.getFile().substring(1);

			System.out.println(xlsFilePath);
			File f=new File(xlsFilePath);
			System.out.println(f.exists());

			FileInputStream fileInputStream = new FileInputStream(f);
			workbook = new HSSFWorkbook(fileInputStream);
			sheetFunctionalities = workbook.getSheet("Mappatura funzionalità");

			Map<String,Double> subFuncWeightMap=initializeSubFuncWeightMap(sheetFunctionalities);
			/*
			//For debugging
			Set<String> keys=costs.keySet();
			for(Object s: keys.toArray())
			{
				String key=(String)s;
				System.out.println(key+"--"+costs.get(s));
			}
			*/

			Map<String,Set<String>> productSubfuncMap=initializeProductSubfuncMap(sheetFunctionalities);
			/*
			//For debugging
			Set<String> keys2=productSubfuncMap.keySet();
			for(Object s2:keys2.toArray())
			{
				String key2=(String) s2;
				System.out.println("KEY="+key2);
				for(Object s3:productSubfuncMap.get(key2))
				{
					String subf=(String) s3;
					System.out.println(subf);
				}
				System.out.println("---------------------------------------------");
			}
			*/



			Set<String> functionalitiesSet=new HashSet<String>();
			Set<String> tempSet=new HashSet<String>();
			for(String product:products)
			{
				tempSet=productSubfuncMap.get(product);
				functionalitiesSet.addAll(tempSet);
			}
			/*
			//For debugging
			for(Object s4: functionalitiesSet.toArray())
			{
				String key4=(String)s4;
				System.out.println(key4);
			}
			*/

			double cost=0;
			sheetSubscription = workbook.getSheet("Prezzo sottoscrizione");
			double functionalityBasicCost=sheetSubscription.getRow(4).getCell(3).getNumericCellValue();
			sheetExternalOEM=workbook.getSheet("Prezzo OEM esterno");
			double minSaleForOEM=sheetExternalOEM.getRow(4).getCell(4).getNumericCellValue();
			double increasePercentageForGoldSubscription=sheetSubscription.getRow(6).getCell(7).getNumericCellValue();
			double ISVsale=sheetSubscription.getRow(7).getCell(7).getNumericCellValue();

			System.out.println(increasePercentageForGoldSubscription);

			for(String functionalityObj : functionalitiesSet)
			{
				String functionality = functionalityObj;
				cost=cost+subFuncWeightMap.get(functionality)*functionalityBasicCost;
			}

	        double costWithNumCores=0;
	        switch (numCores)
	        {
	            case 4:  costWithNumCores = cost*50/100;
	                     break;
	            case 8:  costWithNumCores = cost;
	                     break;
	            case 12: costWithNumCores = cost*150/100;
                		 break;
	            case 16: costWithNumCores = cost*200/100;
	                     break;
	            case 20: costWithNumCores = cost*250/100;
                		 break;
	            case 24: costWithNumCores = cost*300/100;
                		 break;
	        }

	        double costWithSubscriptionGold=0;
	        double costWithSubscriptionSilver=0;


	        if(modality.equals("SUBSCRIPTION"))
	        {
		        	costWithSubscriptionGold=costWithNumCores*(1+increasePercentageForGoldSubscription);
		        	costWithSubscriptionSilver=costWithNumCores;
		        	prices.add(costWithSubscriptionSilver);
		        	prices.add(costWithSubscriptionGold);
			}
	        if(modality.equals("ISV"))
	        {
	        		costWithSubscriptionGold=(costWithNumCores*(1+increasePercentageForGoldSubscription))*(1-ISVsale);  //chiedere ad Angelo
		        	costWithSubscriptionSilver=(costWithNumCores)*(1-ISVsale);
		        	prices.add(costWithSubscriptionSilver);
		        	prices.add(costWithSubscriptionGold);
			}



		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prices;
	}




	private Map<String,Double> initializeSubFuncWeightMap(HSSFSheet sheet)
	{
		Map<String,Double> subFuncWeightMap=new HashMap<String,Double>();
		HSSFRow row=null;
		HSSFCell weightCell=null;
		HSSFCell subFuncCell=null;

	    double weight = 0;
	    String subFunc= null;


	    for (int rowIndex = 5; rowIndex<=sheet.getLastRowNum() ; rowIndex++)  //5 is the first line where the functionalities values started on the table
	    {
			  row = sheet.getRow(rowIndex);
			  if (row != null)
			  {
			        weightCell = row.getCell(1);
			        subFuncCell= row.getCell(3);
			        if (weightCell != null && subFuncCell!=null)
			        {
			          weight = weightCell.getNumericCellValue();
			          subFunc= subFuncCell.getStringCellValue();
			          subFuncWeightMap.put(subFunc,weight);
			        }
			   }

		}

		return subFuncWeightMap;
	}

	private Map<String,Set<String>> initializeProductSubfuncMap(HSSFSheet sheet)
	{
		Map<String,Set<String>> productSubfuncMap=new HashMap<String, Set<String>>();

		Map<String, Integer> productColMap=new HashMap<String, Integer>();
		productColMap.put("BD", 9);
		productColMap.put("SI", 11);
		productColMap.put("ER", 13);
		productColMap.put("LI", 15);
		productColMap.put("PM", 17);
		productColMap.put("PA", 19);
		productColMap.put("OD", 21);
		productColMap.put("EI", 23);

		for(Object o:productColMap.keySet().toArray())
		{
			String product=(String)o;
			HSSFRow row=null;
			HSSFCell subFuncCell=null;
			HSSFCell xCell=null;
			int subFunctionColIdx=3;													//index of 'Sottofunzione' column on Excel sheet
			Set<String> subFunc=new HashSet<String>();

		    for (int rowIndex = 5; rowIndex<=sheet.getLastRowNum() ; rowIndex++)		//5 is the first line where the functionalities values started on the table
		    {
				  row = sheet.getRow(rowIndex);
				  if (row != null)
				  {
				        subFuncCell = row.getCell(subFunctionColIdx);
				        xCell= row.getCell(productColMap.get(product));

				        if (subFuncCell != null && xCell != null)
				        {
				        	if(xCell.getStringCellValue().equals("X"))
				        	{
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
