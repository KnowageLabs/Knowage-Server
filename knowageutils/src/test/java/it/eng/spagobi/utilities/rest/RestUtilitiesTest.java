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
