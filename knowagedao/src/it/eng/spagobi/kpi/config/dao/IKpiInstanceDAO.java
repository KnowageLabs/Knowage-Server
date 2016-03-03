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
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.config.bo.KpiAlarmInstance;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.metadata.SbiKpiComments;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;

import java.util.Date;
import java.util.List;

public interface IKpiInstanceDAO extends ISpagoBIDao{
	
	/**
	 * Returns the KpiInstance of the referred id
	 * 
	 * @param id of the KpiInstance
	 * @return KpiInstance of the referred id
	 * @throws EMFUserError If an Exception occurred
	 */
	public KpiInstance loadKpiInstanceById(Integer id) throws EMFUserError ;
	/**
	 * Returns the SbiKpiInstance of the referred id
	 * 
	 * @param id of the KpiInstance
	 * @return KpiInstance of the referred id
	 * @throws EMFUserError If an Exception occurred
	 */
	public SbiKpiInstance loadSbiKpiInstanceById(Integer id) throws EMFUserError ;	
	/**
	 * Returns the KpiInstance with id 'id' that was valid in date d 
	 * 
	 * @param id of the KpiInstance
	 * @param Date of when the KpiInstance has to be valid
	 * @return KpiInstance of the referred id valid in date d
	 * @throws EMFUserError If an Exception occurred
	 */
	public KpiInstance loadKpiInstanceByIdFromHistory(Integer id, Date d) throws EMFUserError ;

	/**
	 * Returns a List of all the the Threshols of the KpiInstance
	 * 
	 * @param KpiInstance k
	 * @return List of all the the Threshols of the KpiInstance
	 * @throws EMFUserError If an Exception occurred
	 */
	public List getThresholds(KpiInstance k)throws EMFUserError;
	
	public KpiInstance toKpiInstance(SbiKpiInstance kpiInst) throws EMFUserError;

	public void setKpiInstanceFromKPI(KpiInstance kpiInstance, Integer kpiId)
	throws EMFUserError;
	
	public Boolean isKpiInstUnderAlramControl(Integer kpiInstID)
	throws EMFUserError;

	public String getChartType(Integer kpiInstanceID) throws EMFUserError;
	
	public List<KpiAlarmInstance> loadKpiAlarmInstances()throws EMFUserError;
	
	public List<SbiKpiComments> loadCommentsByKpiInstanceId(Integer kpiInstId) throws Exception;
	
	public Integer saveKpiComment(Integer idKpiInstance, String comment, String owner) throws EMFUserError;
	
	public void deleteKpiComment(Integer commentId) throws EMFUserError;
	
	public void editKpiComment(Integer commentId, String comment, String owner) throws EMFUserError;

}
