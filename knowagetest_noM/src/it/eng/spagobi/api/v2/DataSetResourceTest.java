/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.fail;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.api.common.AbstractV2BasicAuthTestCase;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

import java.net.URLEncoder;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetResourceTest extends AbstractV2BasicAuthTestCase {

	private static final String STORE = "store";
	private static final String SALES = "sales";

	private static ICache cache;
	private static IDataSetDAO dataSetDAO;
	private String encoding;

	@BeforeClass
	public static void setUpClass() {
		try {
			UtilitiesForTest.setUpMasterConfiguration();
			UtilitiesDAOForTest.setUpDatabaseTestJNDI();
			TenantManager.setTenant(new Tenant("SPAGOBI"));
			cache = SpagoBICacheManager.getCache();
			dataSetDAO = DAOFactory.getDataSetDAO();
		} catch (Exception e) {
			fail(e.toString());
		}

		loadDataSetInCache(STORE);
		loadDataSetInCache(SALES);
	}

	@Override
	@Before
	public void setup() {
		RestAssured.basePath = "/knowage/restful-services/2.0";
		RestAssured.authentication = basic("biadmin", "biadmin");
		encoding = "UTF-8";
	}

	@Test
	public void getAssociativeSelectionsTest() {
		try {
			String selections = URLEncoder.encode("{\"store.store_type\":[\"Small Grocery\"]}", encoding);
			String associationGroup = URLEncoder
					.encode("{\"datasets\":[\"store\",\"sales\"],\"associations\":[{\"id\":\"A3\",\"description\":\"store.store_id=sales.store_id\",\"fields\":[{\"column\":\"store_id\",\"store\":\"store\"},{\"column\":\"store_id\",\"store\":\"sales\"}]}]}",
							encoding);
			given().urlEncodingEnabled(false).get("/datasets/loadAssociativeSelections?selections=" + selections + "&associationGroup=" + associationGroup)
					.then().contentType(ContentType.JSON).statusCode(200).body("store['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"))
					.body("sales['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void getDataStoreTest() {
		try {
			String selections = URLEncoder.encode("{\"store\":{\"store_type,region_id\":[\"('Supermarket','28')\"]}}", encoding);
			given().urlEncodingEnabled(false).get("/datasets/store/data?selections=" + selections).then().contentType(ContentType.JSON).statusCode(200)
					.body("results", equalTo(1)).body("rows.column_1", hasItems("1"));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void getDataStorePostTest() {
		try {
			String selections = "{\"store\":{\"store_type,region_id\":[\"('Supermarket','28')\"]}}";
			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/store/data").then().contentType(ContentType.JSON).statusCode(200)
					.body("label", equalTo("store")).body("store", containsString("metaData")).body("store", containsString("results"))
					.body("store", containsString("rows"));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@AfterClass
	public static void tearDownClass() {
		deleteDataSetFromCache(STORE);
		deleteDataSetFromCache(SALES);
	}

	private static void loadDataSetInCache(String dataSetLabel) {
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(dataSetLabel);
		if (!cache.contains(dataSet)) {
			dataSet.loadData();
			cache.put(dataSet, dataSet.getDataStore());
		}
	}

	private static void deleteDataSetFromCache(String dataSetLabel) {
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(dataSetLabel);
		cache.delete(dataSet);
	}
}
