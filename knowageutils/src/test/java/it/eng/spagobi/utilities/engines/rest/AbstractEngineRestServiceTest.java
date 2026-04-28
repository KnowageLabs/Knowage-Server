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
package it.eng.spagobi.utilities.engines.rest;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

public class AbstractEngineRestServiceTest {

	@Test
	public void testGetTemplateAsSourceBeanParsesXmlWithoutDeclaration() {
		SourceBean template = new TestEngineRestService("<olap><cube reference=\"FoodMartMySQL\"/></olap>")
				.getTemplateAsSourceBean();

		Assert.assertEquals("OLAP", template.getName());
		Assert.assertNotNull(template.getAttribute("CUBE"));
	}

	@Test
	public void testGetTemplateAsSourceBeanStripsUtf8Bom() {
		SourceBean template = new TestEngineRestService("\uFEFF<olap><cube reference=\"FoodMartMySQL\"/></olap>")
				.getTemplateAsSourceBean();

		Assert.assertEquals("OLAP", template.getName());
		Assert.assertNotNull(template.getAttribute("CUBE"));
	}

	@Test
	public void testGetTemplateAsSourceBeanWrapsInvalidTemplateErrors() {
		try {
			new TestEngineRestService("   ").getTemplateAsSourceBean();
			Assert.fail("Expected SpagoBIEngineStartupException");
		} catch (SpagoBIEngineStartupException e) {
			Assert.assertEquals("Impossible to parse template's content", e.getMessage());
			Assert.assertNotNull(e.getCause());
		}
	}

	private static final class TestEngineRestService extends AbstractEngineRestService {

		private final String templateAsString;

		private TestEngineRestService(String templateAsString) {
			this.templateAsString = templateAsString;
		}

		@Override
		public String getEngineName() {
			return "TestEngine";
		}

		@Override
		public HttpServletRequest getServletRequest() {
			return null;
		}

		@Override
		public String getTemplateAsString() {
			return templateAsString;
		}
	}
}
