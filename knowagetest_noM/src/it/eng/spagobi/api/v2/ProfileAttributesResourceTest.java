package it.eng.spagobi.api.v2;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import it.eng.spagobi.api.common.AbstractV2BasicAuthTestCase;
import it.eng.spagobi.profiling.bo.ProfileAttribute;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.http.ContentType;

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

		expect().contentType(ContentType.JSON).statusCode(200).when().log().all().get("/attributes");
		when().get("/attributes").then().body("attributeName", hasItems("name", "email", "address"));
	}

	@Test
	public void postTest() {
		ProfileAttribute test = buildAttribute();
		given().contentType(ContentType.JSON).body(test).when().post("/attributes").then().statusCode(200)
				.body("attributeName", equalTo(test.getAttributeName()));
	}

	public ProfileAttribute buildAttribute() {
		ProfileAttribute attr = new ProfileAttribute();
		attr.setAttributeName("junitTest1");
		attr.setAttributeDescription("junitTest");
		return attr;
	}
}
