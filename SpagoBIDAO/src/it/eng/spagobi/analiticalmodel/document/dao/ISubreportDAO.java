/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

/**
 * @author Gioia
 *
 */
public interface ISubreportDAO extends ISpagoBIDao{

	/**
	 * Load subreports by master rpt id.
	 * 
	 * @param master_rpt_id the master_rpt_id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadSubreportsByMasterRptId(Integer master_rpt_id) throws EMFUserError;
	
	/**
	 * Load subreports by sub rpt id.
	 * 
	 * @param sub_rpt_id the sub_rpt_id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadSubreportsBySubRptId(Integer sub_rpt_id) throws EMFUserError;
	
	/**
	 * Insert subreport.
	 * 
	 * @param aSubreport the a subreport
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void insertSubreport(Subreport aSubreport) throws EMFUserError;
	
	/**
	 * Erase subreport by master rpt id.
	 * 
	 * @param id the id
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void eraseSubreportByMasterRptId(Integer id) throws EMFUserError;
	
	/**
	 * Erase subreport by sub rpt id.
	 * 
	 * @param id the id
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void eraseSubreportBySubRptId(Integer id) throws EMFUserError;
}
