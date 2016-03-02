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
package it.eng.spagobi.tools.importexportOLD.typesmanager;

import it.eng.spagobi.tools.importexportOLD.ExportManager;
import it.eng.spagobi.tools.importexportOLD.ExporterMetadata;

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
