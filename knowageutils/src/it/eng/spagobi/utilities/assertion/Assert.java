/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.assertion;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Assert {
	
	/**
	 * Protect constructor since it is a static only class
	 */
	protected Assert() { }
	
	public static void assertTrue(boolean condition, String message) {
		if (!condition) throw new AssertionException(message);
	}
	
	public static void assertNotNull(Object o, String message) {
		if (o == null) throw new NullReferenceException(message);
	}
	
	public static void assertUnreachable(String message) {
		throw new UnreachableCodeException(message);
	}
}
