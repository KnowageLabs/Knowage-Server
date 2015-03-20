/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestClassLoader {

	public TestClassLoader() {
		
	}
	
	public void updateclassLoader() {
		updateclassLoader( Thread.currentThread().getContextClassLoader() );
	}
	
	public void updateclassLoader(ClassLoader classLoader) {
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestClassLoader loader = new TestClassLoader();
	}
	
	
}
