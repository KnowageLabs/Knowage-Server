/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.dao;

import it.eng.spagobi.tools.importexport.bo.AssociationFile;

import java.util.List;

public interface IAssociationFileDAO {

	/**
	 * Save association file.
	 * 
	 * @param assfile the assfile
	 * @param content the content
	 */
	public void saveAssociationFile(AssociationFile assfile, byte[] content);
	
	/**
	 * Gets the association files.
	 * 
	 * @return the association files
	 */
	public List getAssociationFiles();
	
	/**
	 * Delete association file.
	 * 
	 * @param assfile the assfile
	 */
	public void deleteAssociationFile(AssociationFile assfile);
	
	/**
	 * Gets the content.
	 * 
	 * @param assfile the assfile
	 * 
	 * @return the content
	 */
	public byte[] getContent(AssociationFile assfile);
	
	/**
	 * Load from id.
	 * 
	 * @param id the id
	 * 
	 * @return the association file
	 */
	public AssociationFile loadFromID(String id);
	
	/**
	 * Exists.
	 * 
	 * @param id the id
	 * 
	 * @return true, if successful
	 */
	public boolean exists(String id);
	
}
