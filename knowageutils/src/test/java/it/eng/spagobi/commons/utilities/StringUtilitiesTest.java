/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2017 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.commons.utilities;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class StringUtilitiesTest {

	@Test
	public void substituteNumericMonthParametersTest() throws Exception {
		String statement = "where month_of_year = $P{ad_mese_tutti} or $P{ad_mese_tutti} = 0";
		for (String type : new String[] { "NUM", "NUMBER" }) {
			Map<String, String> types = Collections.singletonMap("ad_mese_tutti", type);
			for (int month = 0; month <= 12; month++) {
				Map<String, String> values = Collections.singletonMap("ad_mese_tutti",
						StringUtilities.getSingleValue(Integer.toString(month), type));
				assertEquals("where month_of_year = " + month + " or " + month + " = 0",
						StringUtilities.substituteParametersInString(statement, values, types, false));
			}
		}
	}

	@Test
	public void substituteNumericMultivalueParametersTest() throws Exception {
		Map<String, String> values = Collections.singletonMap("months", StringUtilities.getMultiValue("0,1,12", "NUM"));
		Map<String, String> types = Collections.singletonMap("months", "NUM");
		assertEquals("where month_of_year in (0,1,12)",
				StringUtilities.substituteParametersInString("where month_of_year in ($P{months})", values, types, false));
	}

	@Test
	public void substituteQuotedStringParametersTest() throws Exception {
		Map<String, String> types = Collections.singletonMap("value", "STRING");
		String[][] cases = { { "'a'", "'a'" }, { "''", "''" }, { "'it's'", "'it''s'" },
				{ "'it's','a',''", "'it''s','a',''" } };
		for (String[] testCase : cases) {
			Map<String, String> values = Collections.singletonMap("value", testCase[0]);
			assertEquals("select " + testCase[1],
					StringUtilities.substituteParametersInString("select $P{value}", values, types, false));
		}
	}

	@Test
	public void substituteRawExpressionParametersTest() throws Exception {
		Map<String, String> values = Collections.singletonMap("expression", "COALESCE('x', 0)");
		Map<String, String> types = Collections.singletonMap("expression", "RAW");
		assertEquals("select COALESCE('x', 0)",
				StringUtilities.substituteParametersInString("select $P{expression}", values, types, false));
	}

	@Test
	public void getSubstringsBetweenTest() {
		String delimiter = "'";

		String values = "('1')";
		String[] result = StringUtilities.getSubstringsBetween(values, delimiter);
		assertEquals(1, result.length);
		assertEquals("1", result[0]);

		values = "('2')";
		result = StringUtilities.getSubstringsBetween(values, delimiter);
		assertEquals(1, result.length);
		assertEquals("2", result[0]);

		values = "('Drink')";
		result = StringUtilities.getSubstringsBetween(values, delimiter);
		assertEquals(1, result.length);
		assertEquals("Drink", result[0]);

		values = "('Drink','Food')";
		result = StringUtilities.getSubstringsBetween(values, delimiter);
		assertEquals(2, result.length);
		assertEquals("Drink", result[0]);
		assertEquals("Food", result[1]);

		values = "('D'rink','Fo'od')";
		result = StringUtilities.getSubstringsBetween(values, delimiter);
		assertEquals(3, result.length);
		assertEquals("D", result[0]);
		assertEquals(",", result[1]);
		assertEquals("od", result[2]);
	}

	@Test(expected = SpagoBIRuntimeException.class)
	public void getSubstringsBetweenExceptionTest() {
		String delimiter = "'";

		String values = "('D'rink')";
		String[] result = StringUtilities.getSubstringsBetween(values, delimiter);
	}

	@Test
	public void splitBetweenTest() {
		String prefix = "'";
		String delimiter = "','";
		String suffix = "'";
		String values = "('D'rink','Fo'od','Non-Consumable'')";
		String[] result = StringUtilities.splitBetween(values, prefix, delimiter, suffix);
		assertEquals(3, result.length);
		assertEquals("D'rink", result[0]);
		assertEquals("Fo'od", result[1]);
		assertEquals("Non-Consumable'", result[2]);

		prefix = "**";
		delimiter = "++++";
		suffix = "###";
		values = "(**D'rink++++Fo'od++++Non-Consumable'###)";
		result = StringUtilities.splitBetween(values, prefix, delimiter, suffix);
		assertEquals(3, result.length);
		assertEquals("D'rink", result[0]);
		assertEquals("Fo'od", result[1]);
		assertEquals("Non-Consumable'", result[2]);
	}

	@Test(expected = SpagoBIRuntimeException.class)
	public void splitBetweenExceptionTest() {
		String prefix = "**";
		String delimiter = "','";
		String suffix = "'";
		String values = "('D'rink','Fo'od')";
		String[] result = StringUtilities.splitBetween(values, prefix, delimiter, suffix);
	}
}
