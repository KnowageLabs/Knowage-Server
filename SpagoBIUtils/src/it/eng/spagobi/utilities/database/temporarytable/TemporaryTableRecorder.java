/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.database.temporarytable;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class TemporaryTableRecorder {

	private List<TemporaryTable> temporaryTables = null;
	
	public TemporaryTableRecorder () {
		temporaryTables = new ArrayList<TemporaryTable>();
	}
	
	public void addTemporaryTable(TemporaryTable tt) {
		temporaryTables.add(tt);
	}
	
	public Iterator<TemporaryTable> iterator() {
		return temporaryTables.iterator();
	}
}
