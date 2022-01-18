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
package it.eng.knowage.knowageapi.utils;

import it.eng.knowage.knowageapi.error.NullReferenceException;
import it.eng.knowage.knowageapi.error.UnreachableCodeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Assert {

	/**
	 * Protect constructor since it is a static only class
	 */
	protected Assert() {
	}

	public static void assertTrue(boolean condition, String message) {
		if (!condition)
			throw new AssertionException(message);
	}

	public static void assertNotNull(Object o, String message) {
		if (o == null)
			throw new NullReferenceException(message);
	}

	public static void assertUnreachable(String message) {
		throw new UnreachableCodeException(message);
	}

	public static void assertNotEmpty(String input, String message) {
		if (input.isEmpty())
			throw new AssertionException(message);
	}

	public static void assertNotBlank(String input, String message) {
		assertNotNull(input, message);
		assertNotEmpty(input, message);
	}

	public static void assertType(Object obj, Class<?> clazz, String message) {
		assertNotNull(obj, "The obj param cannot be null");

		if (!obj.getClass().equals(clazz)) {
			throw new AssertionException(message);
		}
	}
}
