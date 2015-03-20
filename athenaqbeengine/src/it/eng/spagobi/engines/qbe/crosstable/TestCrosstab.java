/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable;

import junit.framework.TestCase;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class TestCrosstab extends TestCase{

	private CrossTab cs;
	
	protected void setUp() throws Exception {
		super.setUp();
		cs = new CrossTab();
		Node root = new Node("Root");
		root.buildSubTree(1, 2);
		cs.dataMatrix = buildMatrix(2, 16);

		System.out.println("");
	}
	
	public void doTests() {
		//cs.calculateCF("field[0]+field[1]+(7*field[1])", root, true, 1, "A+B");
		
	}
	
	/**
	 * TEST
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static String[][] buildMatrix(int rows, int columns){
		String[][] m = new String[rows][columns];
		for(int i=0; i<rows; i++){
			for(int j=0; j<columns; j++){
				m[i][j]=""+i;
			}
		}
		return m;
	}
}
