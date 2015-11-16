/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
