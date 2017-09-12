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
package it.eng.spagobi.dataset.cache.test;

import java.io.File;

import it.eng.spagobi.tools.dataset.utils.datamart.IQbeDataSetDatamartRetriever;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 *	DatamartRetriever fittizio usato soltanto in caso di Test in quanto non è possibile utilizzare altri IQbeDataSetDatamartRetriever
 */
public class FakeDatamartRetriever implements IQbeDataSetDatamartRetriever {

	
	
	private String resourcePath;
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.dataset.utils.datamart.IQbeDataSetDatamartRetriever#retrieveDatamartFile(java.lang.String)
	 */
	@Override
	public File retrieveDatamartFile(String modelName) {
		File qbeDataMartDir;
		File metamodelJarFile;

		
		qbeDataMartDir = null;
		
		String baseDirStr = this.getResourcePath();
		File baseDir = new File(baseDirStr);																			
		String completePath = baseDir + File.separator + File.separator + "qbe"
				+ File.separator + "datamarts";
		qbeDataMartDir = new File(completePath);
		
		File targetMetamodelFolder = new File(qbeDataMartDir, modelName);		
		metamodelJarFile = new File(targetMetamodelFolder, "datamart.jar");
		return metamodelJarFile;		

	}

	/**
	 * @return the resourcePath
	 */
	public String getResourcePath() {
		return resourcePath;
	}

	/**
	 * @param resourcePath the resourcePath to set
	 */
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	
	

}
