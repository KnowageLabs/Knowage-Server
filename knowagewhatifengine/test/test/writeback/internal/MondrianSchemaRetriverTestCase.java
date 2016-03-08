/**
 * 
 */
package test.writeback.internal;

import java.io.File;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import test.AbstractWhatIfTestCase;
import test.DbConfigContainer;
import test.writeback.TestConstants;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public class MondrianSchemaRetriverTestCase extends AbstractWhatIfTestCase {
	public static final String VERSION_COLUMN_NAME = "wbversion";
	
	
	public String getCatalogue(){
		return DbConfigContainer.getMySqlTemplate();
	}
	
	public void testGetVersionColumn() throws Exception{
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		String columnName = ei.getWriteBackManager().getRetriver().getVersionColumnName();
		assertEquals(VERSION_COLUMN_NAME, columnName);
	}
	
	public String getTemplate(){
		return DbConfigContainer.getMySqlTemplate();
	}
}
