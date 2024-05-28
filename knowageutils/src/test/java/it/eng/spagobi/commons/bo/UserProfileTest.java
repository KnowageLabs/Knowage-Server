/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.commons.bo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import it.eng.spago.error.EMFInternalError;

public class UserProfileTest {

	@Test
	public void testIfUserProfileIsSerializable() throws EMFInternalError {
		UserProfile src = new UserProfile();

		Map<String, Object> attributes = new HashMap<>();

		attributes.put("FIRST", "xxx");
		attributes.put("SECOND", Boolean.TRUE);
		attributes.put("THIRD", Long.valueOf(1L));

		List<String> functionalities = new ArrayList<>();

		functionalities.add("FIRST");
		functionalities.add("SECOND");
		functionalities.add("THIRD");

		List<String> roles = new ArrayList<>();

		roles.add("FIRST");
		roles.add("SECOND");
		roles.add("THIRD");

		// src.setApplication("KNOWAGE");
		src.getUserAttributes().putAll(attributes);
		src.setFunctionalities(functionalities);
		src.setIsSuperadmin(true);
		src.setOrganization("MyOrg");
		src.setOs("Windows");
		src.setRoles(roles);
		src.setSessionId("JSESSIONID1234567890");
		src.setSessionStart(System.currentTimeMillis());
		src.setSourceIpAddress("127.0.0.1");
		src.setSourceSocketEnabled(true);
		src.setUserAgent("Firefox");

		byte[] serialize = SerializationUtils.serialize(src);

		UserProfile dest = (UserProfile) SerializationUtils.deserialize(serialize);

		assertEquals(src, dest);
	}

}
