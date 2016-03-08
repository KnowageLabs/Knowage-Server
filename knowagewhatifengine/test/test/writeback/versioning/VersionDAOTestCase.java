/**
 * 
 */
package test.writeback.versioning;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.version.SbiVersion;
import it.eng.spagobi.engines.whatif.version.VersionDAO;
import test.AbstractWhatIfTestCase;
import test.DbConfigContainer;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public class VersionDAOTestCase  extends AbstractWhatIfTestCase {

	public void testGetAllVersions() throws Exception{
		String catalog = getCatalogue();
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance( catalog);
		Connection connection = null;
		try {
			connection = ei.getDataSource().getConnection();
			VersionDAO dao = new VersionDAO(ei);
			List<SbiVersion> versions = dao.getAllVersions(connection);
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			connection.close();
		}

	}
	
	public String getCatalogue(){
		
        File userDir = new File("").getAbsoluteFile();
        File f  = new File(userDir,  "\\test\\test\\writeback\\resources\\FoodMartMySQL.xml");
		return f.getAbsolutePath();
	}
	
	public String getTemplate(){
		return DbConfigContainer.getMySqlTemplate();
	}
	
}
