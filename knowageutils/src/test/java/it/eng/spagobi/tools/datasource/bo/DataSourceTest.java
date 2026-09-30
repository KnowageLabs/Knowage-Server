/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2026 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.tools.datasource.bo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.eng.spagobi.commons.bo.UserProfile;

public class DataSourceTest {

	@Test
	public void shouldUseBaseJndiWhenMultischemaAttributeIsMissing() {
		DataSource dataSource = createMultischemaDataSource();

		assertEquals("foodmart_oracle", dataSource.getJNDIRunTime(new UserProfile()));
	}

	@Test
	public void shouldUseBaseJndiWhenMultischemaAttributeIsBlank() {
		DataSource dataSource = createMultischemaDataSource();
		UserProfile profile = new UserProfile();
		profile.getUserAttributes().put("schema", "   ");

		assertEquals("foodmart_oracle", dataSource.getJNDIRunTime(profile));
	}

	@Test
	public void shouldAppendMultischemaAttributeToJndi() {
		DataSource dataSource = createMultischemaDataSource();
		UserProfile profile = new UserProfile();
		profile.getUserAttributes().put("schema", "_tenant");

		assertEquals("foodmart_oracle_tenant", dataSource.getJNDIRunTime(profile));
	}

	private DataSource createMultischemaDataSource() {
		DataSource dataSource = new DataSource();
		dataSource.setJndi("foodmart_oracle");
		dataSource.setMultiSchema(true);
		dataSource.setSchemaAttribute("schema");
		return dataSource;
	}
}
