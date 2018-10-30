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
package it.eng.spagobi.utilities;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParametersDecoderTest {

	@Test
	public void testIsMultiValue() {
		ParametersDecoder decoder = new ParametersDecoder();

		String value = "{;{American;Colony;Ebony;Johnson;Urban}STRING}";
		boolean isMultiValue = decoder.isMultiValues(value);
		assertTrue(isMultiValue);

		String json = "{name: {a : 4, b : 4}}";
		isMultiValue = decoder.isMultiValues(json);
		assertTrue(!isMultiValue);
	}

}
