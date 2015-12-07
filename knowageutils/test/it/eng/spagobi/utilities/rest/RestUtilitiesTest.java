package it.eng.spagobi.utilities.rest;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.NameValuePair;

public class RestUtilitiesTest extends TestCase {

	
	public void testGetAddressPairs() {
		String address="http://www.test.com/ko?a=b&c=%26";
		List<NameValuePair> ps = RestUtilities.getAddressPairs(address);
		assertExists("a","b",ps);
		assertExists("c","&",ps);
	}

	private void assertExists(String key, String value, List<NameValuePair> ps) {
		for (NameValuePair nv : ps) {
			if (nv.getName().equals(key) && nv.getValue().equals(value)) {
				return;
			}
		}
		fail(key+"="+value);
		
	}

}
