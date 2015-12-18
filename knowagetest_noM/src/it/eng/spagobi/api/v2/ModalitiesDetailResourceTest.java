package it.eng.spagobi.api.v2;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import it.eng.spagobi.api.common.AbstractV2BasicAuthTestCase;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModalitiesDetailResourceTest extends AbstractV2BasicAuthTestCase{

	List<Integer> ids;
	Check check = new Check();
	static int id;

	@Before
	public void setup() {
		super.setup();

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
