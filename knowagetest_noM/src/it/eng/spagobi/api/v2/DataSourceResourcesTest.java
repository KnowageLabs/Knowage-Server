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
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSourceResourcesTest {

	List<Integer> ids;
	DataSource dataSource = new DataSource();
	static int id;

	@Before
	public void setup() {

		RestAssured.basePath = "/knowage/restful-services/2.0";
		RestAssured.authentication = basic("biadmin", "biadmin");

		String json = get("/datasources").asString();
		ids = JsonPath.from(json).get("dsId");

		dataSource.setDescr(null);
		dataSource.setLabel("JUNIT");
		dataSource.setJndi(null);
		dataSource.setUrlConnection("jdbc:mysql://localhost/foodmart_key");
		dataSource.setUser("root");
		dataSource.setPwd("root");
		dataSource.setDriver("com.mysql.jdbc.Driver");
		dataSource.setDialectId(67);
		dataSource.setSchemaAttribute(null);
		dataSource.setMultiSchema(false);
		dataSource.setReadOnly(false);
		dataSource.setWriteDefault(false);

		Connection connection = null;

		try {
			Class.forName(dataSource.getDriver());
			connection = DriverManager.getConnection(dataSource.getUrlConnection(), dataSource.getUser(), dataSource.getPwd());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void getAllTest() {

		expect().statusCode(200).when().get("/datasources");

		get("/datasources").then().assertThat().contentType(ContentType.JSON);

	}

	@Test
	public void getByIdTest() {

		for (int i = 0; i < ids.size(); i++) {
			expect().contentType(ContentType.JSON).statusCode(200).when().get("/datasources/" + ids.get(i));
		}

	}

	@Test
	public void postTest() {
		Response response = given().contentType(ContentType.JSON).body(dataSource).log().all().when().post("/datasources").then().log().all().statusCode(201)
				.extract().response();
		id = Integer.parseInt(response.body().asString());
		get("/datasources/" + id).then().assertThat().body("dsId", equalTo(id));
	}

	@Test
	public void putTest() {
		dataSource.setDsId(id);
		dataSource.setDescr(null);
		dataSource.setLabel("JUNIT PUT");
		dataSource.setJndi(null);
		dataSource.setUrlConnection("jdbc:mysql://localhost/foodmart_key");
		dataSource.setUser("root");
		dataSource.setPwd("root");
		dataSource.setDriver("com.mysql.jdbc.Driver");
		dataSource.setDialectId(67);
		dataSource.setSchemaAttribute(null);
		dataSource.setMultiSchema(false);
		dataSource.setReadOnly(false);
		dataSource.setWriteDefault(false);

		get("/datasources/" + id).then().assertThat().body("dsId", equalTo(dataSource.getDsId()));
		get("/customChecks/" + id).then().assertThat().body("label", (not(equalTo(dataSource.getLabel()))));
		given().contentType("application/json").and().body(dataSource).log().all().when().put("/datasources/" + id).then().log().all().statusCode(201);

	}

	@Test
	public void removeTest() {
		Response response = expect().statusCode(200).when().delete("/datasources/" + id).thenReturn();
		assertThat((String.valueOf(id)), equalTo(response.body().asString()));
		String json = get("/datasources").asString();
		ids = JsonPath.from(json).get("dsId");
		assertFalse(ids.contains(Integer.parseInt(response.body().asString())));

	}

}
