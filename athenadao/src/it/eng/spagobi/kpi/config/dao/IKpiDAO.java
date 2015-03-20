/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiDocuments;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public interface IKpiDAO extends ISpagoBIDao{
	
	
	public String loadKPIValueXml(Integer kpiValueId)throws EMFUserError ;
	/**
	 * Inserts a new KPI Value with its date, value period and thresholds
	 * 
	 * @param KpiValue to insert 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public Integer insertKpiValue(KpiValue value) throws EMFUserError;
	
	public KpiDocuments loadKpiDocByKpiIdAndDocId(Integer kpiId,Integer docId) throws EMFUserError ;
	
	/**
	 * Returns a List of all the the Threshols of the KpiInstance
	 * 
	 * @param Integer resId, Integer kpiInstId, String endDate
	 * @return List of all the the Threshols of the KpiInstance
	 * @throws EMFUserError If an Exception occurred
	 */
	public String getKpiTrendXmlResult(Integer resId, Integer kpiInstId, Date endDate) throws SourceBeanException;
	
	/**
	 * Returns a JSONOBject with all the the trend values of the KpiInstance
	 * 
	 * @param Integer kpiInstId, Date beginDate,  Date endDate
	 * @return List of all the the values of the KpiInstance in JSONObject form
	 * @throws EMFUserError If an Exception occurred
	 */
	public JSONObject getKpiTrendJSONResult(Integer kpiInstId, Date beginDate , Date endDate) throws SourceBeanException;
	
	public IDataSet getDsFromKpiId(Integer kpiId) throws EMFUserError; 
	
	/**
	 * Returns a List of all the the Threshols of the KpiInstance
	 * 
	 * @param Integer resId, Integer kpiInstId, String endDate
	 * @return List of all the the Threshols of the KpiInstance
	 * @throws EMFUserError If an Exception occurred
	 */
	public String getKpiTrendXmlResult(Integer resId, Integer kpiInstId, Date beginDate , Date endDate) throws SourceBeanException;

	/**
	 * Returns the Kpi of the referred id
	 * 
	 * @param id of the Kpi
	 * @return Kpi of the referred id
	 * @throws EMFUserError If an Exception occurred
	 */	
	public Kpi loadKpiById(Integer id) throws EMFUserError ;
	
	/**
	 * Returns the Kpi Definition of the referred id
	 * 
	 * @param id of the Kpi
	 * @return Kpi of the referred id
	 * @throws EMFUserError If an Exception occurred
	 */		
	public Kpi loadKpiDefinitionById(Integer id) throws EMFUserError ;
	
	/**
	 * KpiValue valid for the the KpiInstance selected, for the resource selected, ant the selected grant node associated 
	 * 
	 * @param KpiValue 
	 * @return KpiValue valid for the the KpiInstance selected, for the resource selected,ou selected in the date selected 
	 * @throws EMFUserError if an Exception occurs
	 */
	public KpiValue getKpiValue(Integer kpiInstanceId, Date d, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError;
	
	/**
	 * KpiValue valid for the the KpiInstance selected, for the resource selected,ou selected in the date selected 
	 * 
	 * @param KpiValue 
	 * @return KpiValue valid for the the KpiInstance selected, ou/hierarchy company selected  for the resource selected, in the date selected 
	 * @throws EMFUserError if an Exception occurs
	 */
	public KpiValue getKpiValueFromInterval(Integer kpiInstanceId, Date from, Date to, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError;
	
	/**
	 * KpiValue valid for the the KpiInstance selected, ou selected for the resource selected, in the date selected 
	 * 
	 * @param KpiValue 
	 * @return KpiValue valid for the the KpiInstance selected, for the resource selected, in the date selected 
	 * @throws EMFUserError if an Exception occurs
	 */
	public void deleteKpiValueFromInterval(Integer kpiInstanceId, Date from, Date to, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError;
	
	
	/**The last KpiValue for the the KpiInstance selected, for the resource selected, ou and hiererchy selected
	 * in the date selected or before
	 * @param kpiInstanceId
	 * @param d
	 * @param r
	 * @param grantNode
	 * @return
	 * @throws EMFUserError
	 */
	public KpiValue getDisplayKpiValue(Integer kpiInstanceId, Date d, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError;

	/**
	 * Returns the list of Kpi.
	 * 
	 * @return the list of all Kpi.
	 * @throws EMFUserError if an Exception occurs
	 */
	public List loadKpiList() throws EMFUserError;
	
	public List loadKpiList(String fieldOrder, String typeOrder) throws EMFUserError;
	
    public List loadPagedKpiList(Integer offset, Integer fetchSize)throws EMFUserError ;
	
	public Integer countKpis()throws EMFUserError ;
	
	public void modifyKpi(Kpi kpi) throws EMFUserError;

	public Integer insertKpi(Kpi toCreate)throws EMFUserError;

	public boolean deleteKpi(Integer kpiId) throws EMFUserError;
	
	public Integer setKpiRel(Integer kpiParentId, Integer kpiChildId, String parameter)throws EMFUserError;
	
	public List loadKpiRelListByParentId(Integer kpiParentId) throws EMFUserError;
	
	public boolean deleteKpiRel(Integer kpiRelId) throws EMFUserError;
	/**
	 * Returns the list of Kpi filtered by hsql query string.
	 * @param hsql query string 
	 * @return the list of filtered Kpi.
	 * @throws EMFUserError if an Exception occurs
	 */
	public List loadKpiListFiltered(String hsql,Integer offset, Integer fetchSize) throws EMFUserError;
	
	public Integer getKpiTrend(Integer resId, Integer kpiInstId, Date endDate) throws Exception;
	
}
