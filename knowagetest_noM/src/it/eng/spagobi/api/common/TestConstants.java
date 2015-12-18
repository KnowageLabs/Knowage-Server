package it.eng.spagobi.api.common;

import static com.jayway.restassured.RestAssured.basic;

import com.jayway.restassured.authentication.AuthenticationScheme;

public class TestConstants {
	public static final String v2Path = "/knowage/restful-services/2.0";
	public static final String v1Path = "/knowage/restful-services";
	public static final AuthenticationScheme basicAuthentication = basic("biadmin", "biadmin");
}
