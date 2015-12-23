package it.eng.spagobi.api.v2;

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import it.eng.spagobi.tools.catalogue.bo.MetaModel;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessModelResourceTest {

	List<Integer> bmIds;
	List<Integer> vIds;

	String json;

	MetaModel businessModel = new MetaModel();
	static int id;

	@Before
	public void setup() {
		RestAssured.basePath = "/knowage/restful-services/2.0";
		RestAssured.authentication = basic("biadmin", "biadmin");

		json = get("/businessmodels").asString();
		bmIds = JsonPath.from(json).get("id");

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy H:mm:ss:SSS");

		businessModel.setName("Insert from test " + formatter.format(date));
		businessModel.setDescription("Inserting new model for JUnit testing");
		businessModel.setModelLocked(false);
	}

	/**
	 * Tests for business models
	 **/
	@Test
	public void getAllBusinessModels() {
		expect().contentType(ContentType.JSON).statusCode(200).when().get("/businessmodels");

		get("/businessmodels").then().body("name", (not(equalTo(""))));

	}

	@Test
	public void getBusinessModelById() {
		for (int i = 0; i < bmIds.size(); i++) {
			expect().contentType(ContentType.JSON).statusCode(200).when().get("/businessmodels/" + bmIds.get(i));
		}
	}

	@Test
	public void postBusinessModel() {
		Response response = given().contentType(ContentType.JSON).body(businessModel).when().post("/businessmodels").then().statusCode(200).extract()
				.response();

		String responseJson = response.body().asString();
		id = JsonPath.from(responseJson).get("id");
		assertNotNull(id);
	}

	@Test
	public void putBusinessModel() {
		businessModel.setId(id);
		businessModel.setDescription(businessModel.getDescription() + " edited");

		Response response = given().contentType(ContentType.JSON).body(businessModel).when().put("/businessmodels/" + businessModel.getId()).then()
				.statusCode(200).extract().response();

	}

	@Test
	public void removeBusinessModel() {
		expect().statusCode(200).when().delete("/" + id);
	}

	/**
	 * Tests for business model versions
	 **/
	@Test
	public void getVersionsOfBusinnesModel() {
		for (int i = 0; i < bmIds.size(); i++) {
			expect().contentType(ContentType.JSON).statusCode(200).when().get("/businessmodels/" + bmIds.get(i) + "/versions");
		}
	}

	@Test
	public void getVersionsById() {
		for (int i = 0; i < bmIds.size(); i++) {

			String versions = get("/businessmodels/" + bmIds.get(i) + "/versions").asString();
			vIds = JsonPath.from(versions).get("id");

			for (int j = 0; j < vIds.size(); j++) {
				expect().contentType(ContentType.JSON).statusCode(200).when().get("/businessmodels/" + bmIds.get(i) + "/versions/" + vIds.get(j));
			}
		}
	}

}
