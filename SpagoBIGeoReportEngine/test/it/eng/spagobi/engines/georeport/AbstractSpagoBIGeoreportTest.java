/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.engines.georeport;

import junit.framework.TestCase;



public class AbstractSpagoBIGeoreportTest extends TestCase {

	
	protected boolean tearDown = false;
        
	public AbstractSpagoBIGeoreportTest() {
		super();
	}
	
	public void setUp() throws Exception {
		try {
			tearDown = false;
		} catch(Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}
	
	protected void tearDown() throws Exception {
		if(tearDown) {
			doTearDown();
		}
	}
	
	protected void doTearDown() {
	
	}
	
}
