package it.eng.spagobi.api.v2;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import it.eng.spagobi.api.common.AbstractV2BasicAuthTestCase;
import it.eng.spagobi.profiling.bo.ProfileAttribute;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProfileAttributesResourceTest extends AbstractV2BasicAuthTestCase {
	ProfileAttribute attribute = null;
	List<Integer> ids;
	static int id;

	@Override
	@Before
	public void setup() {
		super.setup();
	}

	@Test
	public void getAll() {

		expect().contentType(ContentType.JSON).statusCode(200).when().get("/attributes");
		when().get("/attributes").then().body("attributeName", hasItems("name", "email", "address"));
	}

	@Test
	public void postTest() {
		ProfileAttribute test = buildAttribute();
		Response response = given().contentType(ContentType.JSON).body(test).when().post("/attributes").then().statusCode(200)
				.body("attributeName", equalTo(test.getAttributeName())).extract().response();
		try {
			JSONObject attr = new JSONObject(response.asString());
			id = attr.getInt("attributeId");

		} catch (JSONException e) {

		}
	}

	@Test
	public void putTest() {
		ProfileAttribute attr = buildAttribute();
		attr.setAttributeId(id);
		attr.setAttributeName("junitTestEdited");
		attr.setAttributeDescription("junitTestEdited");

		given().contentType("application/json").and().body(attr).when().put("/attributes/" + id).then().statusCode(200);

	}

	@Test
	public void removeTest() {

		expect().statusCode(200).when().delete("/attributes/" + id).thenReturn();
	}

	public ProfileAttribute buildAttribute() {
		ProfileAttribute attr = new ProfileAttribute();
		attr.setAttributeName("junitTest");
		attr.setAttributeDescription("junitTest");
		return attr;
	}
}
