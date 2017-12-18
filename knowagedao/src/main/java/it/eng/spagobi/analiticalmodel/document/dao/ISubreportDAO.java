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
