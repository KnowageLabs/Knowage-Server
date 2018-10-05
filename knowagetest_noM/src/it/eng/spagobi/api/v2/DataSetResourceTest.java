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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.api.common.AbstractV2BasicAuthTestCase;
import it.eng.spagobi.api.common.TestConstants;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetResourceTest extends AbstractV2BasicAuthTestCase {

	private static final String CSV_FOLDER_PATH = "./resources-test/dataset";
	private static final String CSV_FILE_NAME = "SbiFileDataSet.csv";
	private String encoding;

	private it.eng.spagobi.api.DataSetResource resourceV1;
	private it.eng.spagobi.api.v2.DataSetResource resourceV2;

	@BeforeClass
	public static void setUpClass() {
		try {
			UtilitiesForTest.setUpMasterConfiguration();
			UtilitiesDAOForTest.setUpDatabaseTestJNDI();
			TenantManager.setTenant(new Tenant("DEFAULT_TENANT"));

			UserProfileManager.setProfile(new UserProfile("biadmin", "DEFAULT_TENANT"));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Before
	public void initialize() {
		resourceV1 = new it.eng.spagobi.api.DataSetResource();
		resourceV2 = new it.eng.spagobi.api.v2.DataSetResource();
	}

	private static void setHazelcastDefaultConfig() {
		com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();

		cfg.getNetworkConfig().setPort(5701);
		cfg.getNetworkConfig().setPortAutoIncrement(false);
		cfg.getNetworkConfig().setPortCount(100);
		MulticastConfig multicastConfig = new MulticastConfig();
		multicastConfig.setEnabled(false);
		cfg.getNetworkConfig().getJoin().setMulticastConfig(multicastConfig);

		TcpIpConfig tcpIpConfig = new TcpIpConfig();
		tcpIpConfig.setEnabled(true);
		List<String> members = new ArrayList<String>();
		members.add("127.0.0.1");
		tcpIpConfig.setMembers(members);
		cfg.getNetworkConfig().getJoin().setTcpIpConfig(tcpIpConfig);

		cfg.setProperty("hazelcast.socket.bind.any", "false");
		cfg.setProperty("hazelcast.logging.type", "log4j");

		DistributedLockFactory.setDefaultConfig(cfg);
	}

	@Override
	@Before
	public void setup() {
		super.setup();

		encoding = "UTF-8";
		setHazelcastDefaultConfig();
	}

	@Test
	public void getAssociativeSelectionsTest() {
		String dataset1Label = "SbiQueryDataSet";
		String dataset2Label = "SbiFileDataSet";
		try {
			String selections = "{\"SbiQueryDataSet.store_type\":[\"Small Grocery\"]}";
			String encodedSelections = URLEncoder.encode(selections, encoding);

			String associationGroup = "{\"datasets\":[\"SbiQueryDataSet\",\"SbiFileDataSet\"],\"associations\":[{\"id\":\"A3\",\"description\":\"SbiQueryDataSet.store_id=SbiFileDataSet.store_id\",\"fields\":[{\"column\":\"store_id\",\"store\":\"SbiQueryDataSet\"},{\"column\":\"store_id\",\"store\":\"SbiFileDataSet\",\"type\":\"dataset\"},{\"column\":\"param\",\"store\":\"Doc\",\"type\":\"document\"}]}]}";
			String encodedAssociationGroup = URLEncoder.encode(associationGroup, encoding);

			String realtimeDatasets = "[\"SbiQueryDataSet\",\"SbiFileDataSet\"]";
			String encodedRealtimeDatasets = URLEncoder.encode(realtimeDatasets, encoding);

			String datasets = "{\"SbiQueryDataSet\":{\"param1\":\"parameter1\"},\"SbiFileDataSet\":{\"param2\":\"parameter2\"}}";
			String encodedDatasets = URLEncoder.encode(datasets, encoding);

			copyCsvFile();

			createDatasets(dataset1Label, false);
			createDatasets(dataset2Label, false);

			// selections + realtime
			String asString = given()
					.urlEncodingEnabled(false)
					.get("/datasets/loadAssociativeSelections?selections=" + encodedSelections + "&associationGroup=" + encodedAssociationGroup + "&realTime="
							+ encodedRealtimeDatasets + "&datasets=" + encodedDatasets).then().extract().body().asString();
			given().urlEncodingEnabled(false)
					.get("/datasets/loadAssociativeSelections?selections=" + encodedSelections + "&associationGroup=" + encodedAssociationGroup + "&realTime="
							+ encodedRealtimeDatasets).then().contentType(ContentType.JSON).statusCode(200)
					.body("SbiQueryDataSet['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"))
					.body("SbiQueryDataSet['store_type']", hasItems("('Small Grocery')"))
					.body("SbiFileDataSet['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"))
					.body("Doc['param']", hasItems("('2')", "('5')", "('14')", "('22')"));

			// selections
			given().urlEncodingEnabled(false)
					.get("/datasets/loadAssociativeSelections?selections=" + encodedSelections + "&associationGroup=" + encodedAssociationGroup).then()
					.contentType(ContentType.JSON).statusCode(200).body("SbiQueryDataSet['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"))
					.body("SbiQueryDataSet['store_type']", hasItems("('Small Grocery')"))
					.body("SbiFileDataSet['store_id']", hasItems("('2')", "('5')", "('14')", "('22')"))
					.body("Doc['param']", hasItems("('2')", "('5')", "('14')", "('22')"));

			deleteCsvFile();
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(dataset1Label);
			deleteDataset(dataset2Label);
		}
	}

	@Test
	public void getDataStorePostTest() {
		String datasetLabel = "SbiQueryDataSet";
		String selections = "{\"SbiQueryDataSet\":{\"store_type,region_id\":[\"('Deluxe Supermarket','26')\",\"('Deluxe Supermarket','25')\"]}}";
		try {
			createDatasets(datasetLabel, false);

			ValidatableResponse response;

			// selections + realtime
			response = given().contentType(ContentType.JSON).body(selections).when().post("/datasets/SbiQueryDataSet/data?realtime=true").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(2)).body("rows", hasSize(2)).body("rows[0].column_1", equalTo("8"))
					.body("rows[1].column_1", equalTo("12"));

			// selections
			response = given().contentType(ContentType.JSON).body(selections).when().post("/datasets/SbiQueryDataSet/data").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(2)).body("rows", hasSize(2)).body("rows[0].column_1", equalTo("8"))
					.body("rows[1].column_1", equalTo("12"));

			// pagination
			response = given().contentType(ContentType.JSON).body("").when().post("/datasets/SbiQueryDataSet/data?offset=-1&size=7").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(25));

			response = given().contentType(ContentType.JSON).body("").when().post("/datasets/SbiQueryDataSet/data?offset=0&size=-1").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(25));

			response = given().contentType(ContentType.JSON).body("").when().post("/datasets/SbiQueryDataSet/data?offset=0&size=7").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(7)).body("rows[0].column_1", equalTo("0"))
					.body("rows[1].column_1", equalTo("1")).body("rows[2].column_1", equalTo("2")).body("rows[3].column_1", equalTo("3"))
					.body("rows[4].column_1", equalTo("4")).body("rows[5].column_1", equalTo("5")).body("rows[6].column_1", equalTo("6"));

			response = given().contentType(ContentType.JSON).body("").when().post("/datasets/SbiQueryDataSet/data?offset=21&size=7").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(4))
					.body("rows[0].column_1", equalTo("21")).body("rows[1].column_1", equalTo("22")).body("rows[2].column_1", equalTo("23"))
					.body("rows[3].column_1", equalTo("24"));

			// pagination + realtime
			response = given().contentType(ContentType.JSON).body("").when().post("/datasets/SbiQueryDataSet/data?offset=0&size=2&realtime=true").then();
			response.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(2)).body("rows[0].column_1", equalTo("0"))
					.body("rows[1].column_1", equalTo("1"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Test
	public void getDataStorePost2Test() {
		String queryDatasetLabel = "SbiQueryDataSet";
		testDataStorePost2(queryDatasetLabel, false);
		testDataStorePost2(queryDatasetLabel, true);
	}

	private void testDataStorePost2(String datasetLabel, boolean isPersisted) {
		String storeNameAggregations = "{\"measures\":[{\"id\":\"store_sqft\",\"columnName\":\"store_sqft\",\"funct\":\"SUM\",\"alias\":\"sqft\",\"orderType\":\"\"},{\"id\":\"double_sqft\",\"columnName\":\"\\\"store_sqft\\\"+\\\"store_sqft\\\"+0\",\"funct\":\"SUM\",\"alias\":\"double_sqft\",\"orderType\":\"\"}],\"categories\":[{\"id\":\"store_name\",\"columnName\":\"store_name\",\"funct\":\"NONE\",\"alias\":\"store_name\",\"orderType\":\"ASC\"}],\"dataset\":\"Store\"}";
		String storeTypeAggregations = storeNameAggregations.replace("store_name", "store_type");
		String summaryRow = "{\"measures\":[{\"id\":\"sr_sqft\",\"columnName\":\"sqft\",\"funct\":\"SUM\",\"alias\":\"sr_sqft\",\"orderType\":\"\"},{\"id\":\"sr_double_sqft\",\"columnName\":\"double_sqft\",\"funct\":\"SUM\",\"alias\":\"sr_double_sqft\",\"orderType\":\"\"}],\"categories\":[],\"dataset\":\"Store\"}";
		try {
			createDatasets(datasetLabel, isPersisted);

			// store name aggregations + summary row
			given().contentType(ContentType.JSON).when()
					.post("/datasets/SbiQueryDataSet/data?aggregations=" + storeNameAggregations + "&summaryRow=" + summaryRow).then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(26))
					.body("rows[25].column_2", containsString("571596")).body("rows[25].column_3", containsString("1143192"));

			// store name aggregations + summary row + realtime
			given().contentType(ContentType.JSON).when()
					.post("/datasets/SbiQueryDataSet/data?aggregations=" + storeNameAggregations + "&summaryRow=" + summaryRow + "&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25)).body("rows", hasSize(26))
					.body("rows[25].column_2", containsString("571596")).body("rows[25].column_3", containsString("1143192"));

			// store name aggregations + summary row + pagination + realtime
			given().contentType(ContentType.JSON)
					.when()
					.post("/datasets/SbiQueryDataSet/data?aggregations=" + storeNameAggregations + "&summaryRow=" + summaryRow
							+ "&offset=1&size=3&realtime=true").then().contentType(ContentType.JSON).statusCode(200).body("results", equalTo(25))
					.body("rows", hasSize(4)).body("rows[3].column_2", containsString("571596")).body("rows[3].column_3", containsString("1143192"));

			// store type aggregations with SUM + realtime
			given().contentType(ContentType.JSON).when().post("/datasets/SbiQueryDataSet/data?aggregations=" + storeTypeAggregations + "&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(6)).body("rows", hasSize(6))
					.body("rows[5].column_1", equalTo("Supermarket")).body("rows[5].column_2", containsString("193480"))
					.body("rows[5].column_3", containsString("386960"));

			// store type aggregations with AVG + realtime
			String storeTypeAggregationsWithAvg = storeTypeAggregations.replace("SUM", "AVG");
			given().contentType(ContentType.JSON).when().post("/datasets/SbiQueryDataSet/data?aggregations=" + storeTypeAggregationsWithAvg + "&realtime=true")
					.then().contentType(ContentType.JSON).statusCode(200).body("results", equalTo(6)).body("rows", hasSize(6))
					.body("rows[5].column_1", equalTo("Supermarket")).body("rows[5].column_2", containsString("27640"))
					.body("rows[5].column_3", containsString("55280"));

			// store type aggregations with MAX + realtime
			String storeTypeAggregationsWithMax = storeTypeAggregations.replace("SUM", "MAX");
			given().contentType(ContentType.JSON).when().post("/datasets/SbiQueryDataSet/data?aggregations=" + storeTypeAggregationsWithMax + "&realtime=true")
					.then().contentType(ContentType.JSON).statusCode(200).body("results", equalTo(6)).body("rows", hasSize(6))
					.body("rows[5].column_1", equalTo("Supermarket")).body("rows[5].column_2", containsString("39696"))
					.body("rows[5].column_3", containsString("79392"));

			// store type aggregations with MIN + realtime
			String storeTypeAggregationsWithMin = storeTypeAggregations.replace("SUM", "MIN");
			given().contentType(ContentType.JSON).when().post("/datasets/SbiQueryDataSet/data?aggregations=" + storeTypeAggregationsWithMin + "&realtime=true")
					.then().contentType(ContentType.JSON).statusCode(200).body("results", equalTo(6)).body("rows", hasSize(6))
					.body("rows[5].column_1", equalTo("Supermarket")).body("rows[5].column_2", containsString("20319"))
					.body("rows[5].column_3", containsString("40638"));

			// store type aggregations with COUNT + realtime
			String storeTypeAggregationsWithCount = storeTypeAggregations.replace("SUM", "COUNT");
			given().contentType(ContentType.JSON).when()
					.post("/datasets/SbiQueryDataSet/data?aggregations=" + storeTypeAggregationsWithCount + "&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(6)).body("rows", hasSize(6))
					.body("rows[5].column_1", equalTo("Supermarket")).body("rows[5].column_2", containsString("7"))
					.body("rows[5].column_3", containsString("7"));

		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Test
	public void getDataStorePostNoRestTest() {
		String queryDatasetLabel = "SbiQueryDataSet";
		testDataStorePostNoRest(queryDatasetLabel, false);
		testDataStorePostNoRest(queryDatasetLabel, true);
	}

	private void testDataStorePostNoRest(String datasetLabel, boolean isPersisted) {
		String storeNameAggregations = "{\"measures\":[{\"id\":\"store_sqft\",\"columnName\":\"store_sqft\",\"funct\":\"SUM\",\"alias\":\"sqft\",\"orderType\":\"\"},{\"id\":\"double_sqft\",\"columnName\":\"\\\"store_sqft\\\"+\\\"store_sqft\\\"+0\",\"funct\":\"SUM\",\"alias\":\"double_sqft\",\"orderType\":\"\"}],\"categories\":[{\"id\":\"store_name\",\"columnName\":\"store_name\",\"funct\":\"NONE\",\"alias\":\"store_name\",\"orderType\":\"ASC\"}],\"dataset\":\"Store\"}";
		String storeTypeAggregations = storeNameAggregations.replace("store_name", "store_type");
		String summaryRow = "{\"measures\":[{\"id\":\"sr_sqft\",\"columnName\":\"sqft\",\"funct\":\"SUM\",\"alias\":\"sr_sqft\",\"orderType\":\"\"},{\"id\":\"sr_double_sqft\",\"columnName\":\"double_sqft\",\"funct\":\"SUM\",\"alias\":\"sr_double_sqft\",\"orderType\":\"\"}],\"categories\":[],\"dataset\":\"Store\"}";
		try {
			createDatasetsNoRest(datasetLabel, isPersisted);

			JsonPath jsonPath;
			List<Object> rows;
			HashMap<String, String> row;

			// store name aggregations + summary row
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeNameAggregations, summaryRow, -1, -1, false));
			assertEquals(25, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(26, rows.size());
			row = (HashMap<String, String>) rows.get(25);
			assertTrue(row.get("column_2").contains("571596"));
			assertTrue(row.get("column_3").contains("1143192"));

			// store name aggregations + summary row + realtime
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeNameAggregations, summaryRow, -1, -1, true));
			assertEquals(25, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(26, rows.size());
			row = (HashMap<String, String>) rows.get(25);
			assertTrue(row.get("column_2").contains("571596"));
			assertTrue(row.get("column_3").contains("1143192"));

			// store name aggregations + summary row + pagination + realtime
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeNameAggregations, summaryRow, 1, 3, true));
			assertEquals(25, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(4, rows.size());
			row = (HashMap<String, String>) rows.get(3);
			assertTrue(row.get("column_2").contains("571596"));
			assertTrue(row.get("column_3").contains("1143192"));

			// store type aggregations with SUM + realtime
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeTypeAggregations, null, -1, -1, true));
			assertEquals(6, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(6, rows.size());
			row = (HashMap<String, String>) rows.get(5);
			assertTrue(row.get("column_2").contains("193480"));
			assertTrue(row.get("column_3").contains("386960"));

			// store type aggregations with AVG + realtime
			String storeTypeAggregationsWithAvg = storeTypeAggregations.replace("SUM", "AVG");
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeTypeAggregationsWithAvg, null, -1, -1, true));
			assertEquals(6, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(6, rows.size());
			row = (HashMap<String, String>) rows.get(5);
			assertTrue(row.get("column_2").contains("27640"));
			assertTrue(row.get("column_3").contains("55280"));

			// store type aggregations with MAX + realtime
			String storeTypeAggregationsWithMax = storeTypeAggregations.replace("SUM", "MAX");
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeTypeAggregationsWithMax, null, -1, -1, true));
			assertEquals(6, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(6, rows.size());
			row = (HashMap<String, String>) rows.get(5);
			assertTrue(row.get("column_2").contains("39696"));
			assertTrue(row.get("column_3").contains("79392"));

			// store type aggregations with MIN + realtime
			String storeTypeAggregationsWithMin = storeTypeAggregations.replace("SUM", "MIN");
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeTypeAggregationsWithMin, null, -1, -1, true));
			assertEquals(6, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(6, rows.size());
			row = (HashMap<String, String>) rows.get(5);
			assertTrue(row.get("column_2").contains("20319"));
			assertTrue(row.get("column_3").contains("40638"));

			// store type aggregations with COUNT + realtime
			String storeTypeAggregationsWithCount = storeTypeAggregations.replace("SUM", "COUNT");
			jsonPath = JsonPath.from(resourceV2.getDataStorePost(datasetLabel, null, null, null, -1, storeTypeAggregationsWithCount, null, -1, -1, true));
			assertEquals(6, jsonPath.getInt("results"));
			rows = jsonPath.getList("rows");
			assertEquals(6, rows.size());
			row = (HashMap<String, String>) rows.get(5);
			assertTrue(row.get("column_2").contains("7"));
			assertTrue(row.get("column_3").contains("7"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDatasetNoRest(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiFlatDataSetTest() {
		String datasetLabel = "SbiFlatDataSet";
		testSbiFlatDataSet(datasetLabel, false);
	}

	private void testSbiFlatDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"product_name\":[\"Washington Cream Soda\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(1)).body("rows[0].column_4", equalTo("Washington Cream Soda"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(1)).body("rows[0].column_4", equalTo("Washington Cream Soda"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiQueryDataSetTest() {
		String datasetLabel = "SbiQueryDataSet";
		testSbiQueryDataSet(datasetLabel, false);
		testSbiQueryDataSet(datasetLabel, true);
	}

	private void testSbiQueryDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"store_type,region_id\":[\"('Supermarket','28')\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(1)).body("rows[0].column_5", equalTo("Store 1"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(1)).body("rows[0].column_5", equalTo("Store 1"));

		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiFileDataSetTest() {
		String datasetLabel = "SbiFileDataSet";
		testSbiFileDataSet(datasetLabel, false);
		testSbiFileDataSet(datasetLabel, true);
	}

	private void testSbiFileDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"product_id,store_id\":[\"(1,1)\"]}}";
		try {
			copyCsvFile();

			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(2)).body("rows[0].column_3", equalTo("9685")).body("rows[1].column_3", equalTo("1894"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(2)).body("rows[0].column_3", equalTo("9685"))
					.body("rows[1].column_3", equalTo("1894"));

			deleteCsvFile();
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiJClassDataSetTest() {
		String datasetLabel = "SbiJClassDataSet";
		testSbiJClassDataSet(datasetLabel, false);
		testSbiJClassDataSet(datasetLabel, true);
	}

	private void testSbiJClassDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"VALUE\":[\"(200)\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(1)).body("rows[0].column_1", equalTo("200"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(1)).body("rows[0].column_1", equalTo("200"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiScriptDataSetTest() {
		String datasetLabel = "SbiScriptDataSet";
		testSbiScriptDataSet(datasetLabel, false);
		testSbiScriptDataSet(datasetLabel, true);
	}

	private void testSbiScriptDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"VALUE\":[\"(200)\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(1)).body("rows[0].column_1", equalTo("200"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(1)).body("rows[0].column_1", equalTo("200"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiRestDataSetTest() {
		String datasetLabel = "SbiRESTDataSet";
		testSbiRestDataSet(datasetLabel, false);
		testSbiRestDataSet(datasetLabel, true);
	}

	private void testSbiRestDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"prosumerId\":[\"('pros3')\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(74)).body("rows[0].column_2", containsString("3.97"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(74)).body("rows[0].column_2", containsString("3.97"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	@Ignore
	@Test
	public void SbiCkanDataSetTest() {
		String datasetLabel = "SbiCkanDataSet";
		testSbiCkanDataSet(datasetLabel, false);
		testSbiCkanDataSet(datasetLabel, true);
	}

	private void testSbiCkanDataSet(String datasetLabel, boolean isPersisted) {
		String selections = "{\"" + datasetLabel + "\":{\"country\":[\"('Mexico')\"]}}";
		try {
			createDatasets(datasetLabel, isPersisted);

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON)
					.statusCode(200).body("results", equalTo(13)).body("rows[0].column_3", equalTo("Mexico City"));

			given().contentType(ContentType.JSON).body(selections).when().post("/datasets/" + datasetLabel + "/data?offset=0&size=1000&realtime=true").then()
					.contentType(ContentType.JSON).statusCode(200).body("results", equalTo(13)).body("rows[0].column_3", equalTo("Mexico City"));
		} catch (Exception e) {
			fail(e.toString());
		} finally {
			deleteDataset(datasetLabel);
		}
	}

	private String getDatasetDescription(String datasetLabel) throws IOException, UnsupportedEncodingException {
		byte[] encoded = Files.readAllBytes(Paths.get("./resources-test/dataset/" + datasetLabel + ".json"));
		String datasetDescription = new String(encoded, encoding);
		return datasetDescription;
	}

	private void deleteDataset(String datasetLabel) {
		given().contentType(ContentType.JSON).when().delete("/datasets/" + datasetLabel).then();
		given().basePath(TestConstants.v1Path).contentType(ContentType.JSON).when().delete("/datasets/" + datasetLabel + "/cleanCache").then();
	}

	private void deleteDatasetNoRest(String datasetLabel) {
		resourceV1.cleanCache(datasetLabel);
		resourceV1.deleteDataset(datasetLabel);
	}

	@SuppressWarnings("unchecked")
	private void createDatasets(String datasetLabel, boolean isPersisted) throws UnsupportedEncodingException, IOException, JSONException {
		// delete dataset if it exists
		Response response = given().contentType(ContentType.JSON).when().get("/datasets/" + datasetLabel).then().contentType(ContentType.JSON).statusCode(200)
				.extract().response();
		String responseJson = response.body().asString();
		String label = JsonPath.from(responseJson).get("label");
		if (label != null && label.equals(datasetLabel)) {
			deleteDataset(datasetLabel);
		}

		// check that the dataset doesn't exist
		given().contentType(ContentType.JSON).when().get("/datasets/" + datasetLabel).then().contentType(ContentType.JSON).statusCode(200)
				.body("errors", hasSize(greaterThan(0)));

		// create the dataset
		String description = getDatasetDescription(datasetLabel);
		JSONObject jsonDescription = new JSONObject(description);
		jsonDescription.put("persisted", isPersisted);
		jsonDescription.put("persistTableName", datasetLabel);
		given().contentType(ContentType.JSON).body(jsonDescription.toString()).when().post("/datasets").then().statusCode(201);

		// check that the dataset exists
		given().contentType(ContentType.JSON).when().get("/datasets/" + datasetLabel).then().contentType(ContentType.JSON).statusCode(200)
				.body("label", anyOf(equalTo(datasetLabel), hasItems(datasetLabel)));

		// force data loading (for cached dataset)
		given().contentType(ContentType.JSON).when().get("/datasets/" + datasetLabel + "/data").then().contentType(ContentType.JSON).statusCode(200);
	}

	private void createDatasetsNoRest(String datasetLabel, boolean isPersisted) throws UnsupportedEncodingException, IOException, JSONException {
		String responseJson;

		// delete dataset if it exists
		try {
			responseJson = resourceV1.getDataSet(datasetLabel); // thows exception if dataset doesn't exist
			deleteDatasetNoRest(datasetLabel);
		} catch (Exception e) {
		}

		// check that the dataset doesn't exist
		try {
			responseJson = resourceV1.getDataSet(datasetLabel);
			fail("Dataset [" + datasetLabel + "] still exists");
		} catch (Exception e) {
		}

		// create the dataset
		String description = getDatasetDescription(datasetLabel);
		JSONObject jsonDescription = new JSONObject(description);
		jsonDescription.put("persisted", isPersisted);
		jsonDescription.put("persistTableName", datasetLabel);
		javax.ws.rs.core.Response response = resourceV2.addDataSet(jsonDescription.toString());
		assertEquals(response.getStatus(), 201);

		// check that the dataset exists
		responseJson = resourceV1.getDataSet(datasetLabel);
		assertTrue(responseJson.contains(datasetLabel));

		// force data loading (for cached dataset)
		responseJson = resourceV1.getDataStore(datasetLabel, null, null, null, -1, null, null, -1, -1, false);
		assertTrue(responseJson != null && !responseJson.isEmpty());
	}

	private String getResourceDir() {
		String responseJson = given().when().get("/configs/label/SPAGOBI.RESOURCE_PATH_JNDI_NAME").then().contentType(ContentType.JSON).statusCode(200)
				.extract().body().asString();
		Config config = (Config) JsonConverter.jsonToObject(responseJson, Config.class);
		String jndiName = config.getValueCheck();

		try {
			String resourceDir = given().when().get("/utilities/jndi?label=java%3a%2f%2fcomp%2fenv%2fresource_path").then().extract().body().asString();
			resourceDir = given().when().get("/utilities/jndi?label=" + URLEncoder.encode(jndiName, encoding)).then().extract().body().asString();
			return resourceDir;
		} catch (UnsupportedEncodingException e) {
			throw new SpagoBIRuntimeException("Unable to get the resouce folder");
		}
	}

	private String getTenantName() {
		String responseJson = given().when().get("/utilities/tenant").then().contentType(ContentType.JSON).statusCode(200).extract().body().asString();
		String tenantName = JsonPath.from(responseJson).get("name");
		return tenantName;
	}

	private void copyCsvFile() throws IOException {
		String folderPath = getDatasetFileFolderPath();
		File destFolder = new File(folderPath);
		destFolder.mkdirs();
		File sourceFile = new File(CSV_FOLDER_PATH, CSV_FILE_NAME);
		File destFile = new File(folderPath, CSV_FILE_NAME);
		Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	private String getDatasetFileFolderPath() {
		String resourceDir = getResourceDir();
		String tenant = getTenantName();
		String pathname = resourceDir + "/" + tenant + "/dataset/files";
		return pathname;
	}

	private void deleteCsvFile() throws IOException {
		String folderPath = getDatasetFileFolderPath();
		File csvFile = new File(folderPath + "/" + CSV_FILE_NAME);
		Files.delete(csvFile.toPath());
	}
}
