/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;


public interface IObjTemplateDAO extends ISpagoBIDao{

	/**
	 * Gets the bI object active template.
	 * 
	 * @param biobjId the biobj id
	 * 
	 * @return the bI object active template
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public ObjTemplate getBIObjectActiveTemplate(Integer biobjId) throws EMFInternalError; 
	
	
	/**
	 * Gets the bI object active template starting by document label
	 * 
	 * @param biobjLabel the BiObject label
	 * 
	 * @return the bI object active template
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public ObjTemplate getBIObjectActiveTemplateByLabel(String label) throws EMFInternalError; 

	
	/**
	 * Gets the bI object template list.
	 * 
	 * @param biobjId the biobj id
	 * 
	 * @return the bI object template list
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public List getBIObjectTemplateList(Integer biobjId) throws EMFInternalError; 
	
	/**
	 * Load bi object template.
	 * 
	 * @param tempId the temp id
	 * 
	 * @return the obj template
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public ObjTemplate loadBIObjectTemplate(Integer tempId) throws EMFInternalError;
	
	/**
	 * Gets the next prog for template.
	 * 
	 * @param biobjId the biobj id
	 * 
	 * @return the next prog for template
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public Integer getNextProgForTemplate(Integer biobjId) throws EMFInternalError;
	
	/**
	 * Delete bi object template.
	 * 
	 * @param tempId the temp id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void deleteBIObjectTemplate(Integer tempId) throws EMFInternalError;
	
	/**
	 * Insert a new bi object template.
	 * 
	 * @param objTemplate the new template
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void insertBIObjectTemplate(ObjTemplate objTemplate) throws EMFUserError, EMFInternalError;
	
}
