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

import org.junit.Test;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class StringUtilitiesTest {

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
		String values = "('D'rink','Fo'od')";
		String[] result = StringUtilities.splitBetween(values, prefix, delimiter, suffix);
		assertEquals(2, result.length);
		assertEquals("D'rink", result[0]);
		assertEquals("Fo'od", result[1]);

		prefix = "**";
		delimiter = "++++";
		suffix = "###";
		values = "(**D'rink++++Fo'od###)";
		result = StringUtilities.splitBetween(values, prefix, delimiter, suffix);
		assertEquals(2, result.length);
		assertEquals("D'rink", result[0]);
		assertEquals("Fo'od", result[1]);
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
