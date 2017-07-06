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
package it.eng.knowage.engines.dossier.common;

import java.io.File;

import org.apache.log4j.Logger;

import it.eng.knowage.engines.dossier.DossierEngineConfig;

public class PPTTemplateLoader {
	
	static private Logger logger = Logger.getLogger(PPTTemplateLoader.class);
	
	//NOTE: Hardoced TENANT value!!!
	private static final String PATH_TO_TEMP = File.separator + "SPAGOBI" + File.separator + "dossier";
	
	public File loadPPTTemplate(String templateName){
		 
		String resourcePath = DossierEngineConfig.getInstance().getEngineResourcePath();
		
		String pathToFile = resourcePath + PATH_TO_TEMP +  File.separator + templateName;

		File pptTemplate = new File(pathToFile);
		
		return pptTemplate;
		 
	 }

}
