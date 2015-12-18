package it.eng.spagobi.api.common;

import com.jayway.restassured.RestAssured;

public class AbstractV2BasicAuthTestCase {
	public void setup() {
		RestAssured.basePath = TestConstants.v2Path;
		RestAssured.authentication = TestConstants.basicAuthentication;
	}
}
