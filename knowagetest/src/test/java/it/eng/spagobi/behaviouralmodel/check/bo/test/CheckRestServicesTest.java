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
package it.eng.spagobi.behaviouralmodel.check.bo.test;

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import it.eng.spagobi.behaviouralmodel.check.bo.Check;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CheckRestServicesTest {

	List<Integer> ids;
	Check check = new Check();
	static int id;

	@Before
	public void setup() {
		RestAssured.basePath = "/knowage/restful-services/2.0";
		RestAssured.authentication = basic("biadmin", "biadmin");

		String json = get("/customChecks").asString();
		ids = JsonPath.from(json).get("checkId");

		check.setDescription("testJUnit");
		check.setFirstValue(null);
		check.setLabel("JUnit");
		check.setName("Test");
		check.setSecondValue(null);
		check.setValueTypeCd("REGEXP");
		check.setValueTypeId(454);

	}

	@Test
	public void getAllTest() {

		expect().contentType(ContentType.JSON).statusCode(200).when().get("/customChecks");

	}

	@Test
	public void getByIdTest() {
		for (int i = 0; i < ids.size(); i++) {
			expect().contentType(ContentType.JSON).statusCode(200).when().get("/customChecks/" + ids.get(i));
		}
	}

	@Test
	public void postTest() {
		Response response = given().contentType(ContentType.JSON).body(check).when().post("/customChecks").then().statusCode(201).extract().response();
		id = Integer.parseInt(response.body().asString());
		get("/customChecks/" + id).then().assertThat().body("checkId", equalTo(id));
	}

	@Test
	public void putTest() {
		check.setCheckId(id);
		check.setDescription("testJUnit");
		check.setFirstValue(null);
		check.setLabel("JUnitEdited");
		check.setName("Test");
		check.setSecondValue(null);
		check.setValueTypeCd("REGEXP");
		check.setValueTypeId(454);

		get("/customChecks/" + id).then().assertThat().body("checkId", equalTo(check.getCheckId()));
		get("/customChecks/" + id).then().assertThat().body("label", (not(equalTo(check.getLabel()))));
		given().contentType("application/json").and().body(check).when().put("/customChecks/" + id).then().statusCode(201);

	}

	@Test
	public void removeTest() {
		Response response = expect().statusCode(200).when().delete("/customChecks/" + id).thenReturn();
		assertThat((String.valueOf(id)), equalTo(response.body().asString()));
		String json = get("/customChecks").asString();
		ids = JsonPath.from(json).get("checkId");
		assertFalse(ids.contains(Integer.parseInt(response.body().asString())));

	}

}
