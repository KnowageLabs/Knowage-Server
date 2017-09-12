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
package it.eng.spagobi.tools.dataset.bo;

import static org.junit.Assert.assertEquals;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.UtilitiesForTest;

import org.junit.Before;
import org.junit.Test;

public class DataSetParametersListTest {

	public static final String SECOND_DEFAULT_VALUE = "2";
	public static final String FIRST_DEFAULT_VALUE = "kik";
	public static final String SECOND_PARAM = "secondParam";
	public static final String FIRST_PARAM = "firstParam";
	public static final String XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + "<PARAMETERSLIST>\n" + "  <ROWS>\n"
			+ "    <ROW DEFAULT_VALUE=\"kik\" NAME=\"firstParam\" TYPE=\"string\"/>\n"
			+ "    <ROW DEFAULT_VALUE=\"2\" NAME=\"secondParam\" TYPE=\"number\"/>\n" + "  </ROWS>\n" + "</PARAMETERSLIST>\n";

	@Before
	public void setUp() throws Exception {
		UtilitiesForTest.setUpMasterConfiguration();
	}

	@Test
	public void testLoadFromXML() throws SourceBeanException {
		DataSetParametersList dspl = new DataSetParametersList();
		dspl.loadFromXML(XML);
		checkParams(dspl);
	}

	@Test
	public void testToXML() {
		DataSetParametersList dspl = new DataSetParametersList();
		dspl.add(FIRST_PARAM, "string");
		dspl.add(SECOND_PARAM, "number");

		String xml = dspl.toXML();
		assertEquals(
				"<PARAMETERSLIST><ROWS><ROW NAME=\"firstParam\" TYPE=\"string\" DEFAULT_VALUE=\"\" /><ROW NAME=\"secondParam\" TYPE=\"number\" DEFAULT_VALUE=\"\" /></ROWS></PARAMETERSLIST>"
						+ "", xml);

		dspl = new DataSetParametersList();
		dspl.add(FIRST_PARAM, "string", FIRST_DEFAULT_VALUE);
		dspl.add(SECOND_PARAM, "number", SECOND_DEFAULT_VALUE);

		xml = dspl.toXML();
		assertEquals(
				"<PARAMETERSLIST><ROWS><ROW NAME=\"firstParam\" TYPE=\"string\" DEFAULT_VALUE=\"kik\" /><ROW NAME=\"secondParam\" TYPE=\"number\" DEFAULT_VALUE=\"2\" /></ROWS></PARAMETERSLIST>"
						+ "", xml);
	}

	@Test
	public void testFromXML() throws SourceBeanException {
		DataSetParametersList fromXML = DataSetParametersList.fromXML(XML);
		checkParams(fromXML);
	}

	private static void checkParams(DataSetParametersList fromXML) {
		assertEquals(2, fromXML.getItems().size());
		DataSetParameterItem d = (DataSetParameterItem) fromXML.getItems().get(0);
		assertEquals(FIRST_DEFAULT_VALUE, d.getDefaultValue());
		assertEquals("string", d.getType());
		assertEquals(FIRST_PARAM, d.getName());
		d = (DataSetParameterItem) fromXML.getItems().get(1);
		assertEquals(SECOND_DEFAULT_VALUE, d.getDefaultValue());
		assertEquals("number", d.getType());
		assertEquals(SECOND_PARAM, d.getName());
	}

}
