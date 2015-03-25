/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.typesmanager;

import it.eng.spagobi.tools.importexport.ExportManager;
import it.eng.spagobi.tools.importexport.ExporterMetadata;

import org.apache.log4j.Logger;

/** class for specific types export managers
 * 
 * @author gavardi
 *
 */

public abstract class AbstractTypesExportManager  implements ITypesExportManager  {

	String type;
	ExporterMetadata exporter;
	ExportManager exportManager;

	
	

	public AbstractTypesExportManager(String type, ExporterMetadata exporter, ExportManager manager) {
		super();
		this.type = type;
		this.exporter = exporter;
		this.exportManager = manager;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
	
}
