/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.test;

import junit.framework.TestCase;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractSpagoBITestCase extends TestCase {
	
	protected boolean performTearDown;
	
	public AbstractSpagoBITestCase() {
		super();
	}

	public void setUp() throws Exception {
		try {
			performTearDown = true;
		} catch(Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}
	
	public void tearDown() throws Exception {
		if(performTearDown) {
			doTearDown();
		}
	}

	protected void doTearDown() {
		
	}
}
