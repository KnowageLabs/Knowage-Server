/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.engines.georeport;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestCostants {	
	public static File workspaceFolder = new File("D:/Documenti/Sviluppo/workspaces/helios/spagobi/server");
	public static File inputFolder = new File(workspaceFolder, "SpagoBIGeoReportEngine");
	public static File outputFolder = new File( new File(workspaceFolder, "unit-test"), "SpagoBIGeoReportEngine");
}
