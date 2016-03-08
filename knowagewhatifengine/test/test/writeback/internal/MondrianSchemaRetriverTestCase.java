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
