/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import it.eng.spago.error.EMFUserError;

import java.util.List;

public interface IExportManager {

	/**
	 * Prepare the environment for export.
	 * 
	 * @param pathExpFold Path of the export folder
	 * @param nameExpFile the name to give to the exported file
	 * @param expSubObj Flag which tells if it's necessary to export subobjects
	 * @param expSnaps the exp snaps
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void prepareExport(String pathExpFold, String nameExpFile, 
			boolean expSubObj, boolean expSnaps) throws EMFUserError;
	
	/**
	 * Exports objects
	 * 
	 * @param objPaths List of path of the objects to export
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void exportObjects(List objPaths) throws EMFUserError;
	
	/**
	 * Exports resources (OLAP schemas, ETL jobs, datamarts)
	 * 
	 * @throws EMFUserError the EMF user error
	 */
//	public void exportResources() throws EMFUserError;
	
	/**
	 * Creates the archive export file
	 * 
	 * @throws EMFUserError
	 */
	public void createExportArchive() throws EMFUserError;
	
	/**
	 * Clean the export environment (close sessions and delete temporary files).
	 */
	public void cleanExportEnvironment();
}
