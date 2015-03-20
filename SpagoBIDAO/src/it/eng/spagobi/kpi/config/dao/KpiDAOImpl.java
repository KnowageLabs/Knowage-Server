/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiDocuments;
import it.eng.spagobi.kpi.config.bo.KpiRel;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.config.metadata.SbiKpi;
import it.eng.spagobi.kpi.config.metadata.SbiKpiDocument;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstanceHistory;
import it.eng.spagobi.kpi.config.metadata.SbiKpiRel;
import it.eng.spagobi.kpi.config.metadata.SbiKpiValue;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.dao.IResourceDAO;
import it.eng.spagobi.kpi.model.metadata.SbiResources;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnit;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitHierarchies;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;
import it.eng.spagobi.kpi.threshold.dao.IThresholdDAO;
import it.eng.spagobi.kpi.threshold.dao.IThresholdValueDAO;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.udp.dao.IUdpValueDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONArray;
import org.json.JSONObject;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	static private Logger logger = Logger.getLogger(KpiDAOImpl.class);

	public String loadKPIValueXml(Integer kpiValueId)
	throws EMFUserError {
		logger.debug("IN");
		String xmlToReturn = "";
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiValue hibSbiKpiValue = (SbiKpiValue) aSession.load(SbiKpiValue.class, kpiValueId);
			xmlToReturn = hibSbiKpiValue.getXmlData();

		} catch (HibernateException he) {
			logger.error("Error while loading the KpiValue with id "
					+ ((kpiValueId == null) ? "" : kpiValueId.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10112);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return xmlToReturn;
	}

	private KpiDocuments toKpiDoc(SbiKpiDocument kpiDoc) throws EMFUserError {
		logger.debug("IN");
		KpiDocuments toReturn = new KpiDocuments();

		toReturn.setBiObjId(kpiDoc.getSbiObjects().getBiobjId());
		toReturn.setBiObjLabel(kpiDoc.getSbiObjects().getLabel());
		toReturn.setKpiDocId(kpiDoc.getIdKpiDoc());
		toReturn.setKpiId(kpiDoc.getSbiKpi().getKpiId());
		return toReturn;
	}

	public KpiDocuments loadKpiDocByKpiIdAndDocId(Integer kpiId,Integer docId) throws EMFUserError {
		logger.debug("IN");
		KpiDocuments toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpi hibKpi = (SbiKpi) aSession.load(SbiKpi.class, kpiId);
			SbiObjects hibObject = (SbiObjects) aSession.load(SbiObjects.class, docId);
			Criterion kpiCriterrion = Expression.eq("sbiKpi",hibKpi);
			Criterion sbiObjCriterrion = Expression.eq("sbiObjects",hibObject);
			Criteria crit = aSession.createCriteria(SbiKpiDocument.class);
			crit.add(kpiCriterrion);
			crit.add(sbiObjCriterrion);
			SbiKpiDocument kpiDoc = (SbiKpiDocument) crit.uniqueResult();
			toReturn = toKpiDoc(kpiDoc);

		} catch (HibernateException he) {
			logger.error("Error while loading the KpiDoc ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10112);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}


	public Kpi loadKpiDefinitionById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Kpi toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpi hibSbiKpi = (SbiKpi) aSession.load(SbiKpi.class, id);
			toReturn = toKpiDefinition(hibSbiKpi);

		} catch (HibernateException he) {
			logger.error("Error while loading the Kpi with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10112);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private Kpi toKpiDefinition(SbiKpi kpi) throws EMFUserError {
		logger.debug("IN");
		Kpi toReturn = new Kpi();

		String code = kpi.getCode();
		String description = kpi.getDescription();

		Integer kpiId = kpi.getKpiId();
		String kpiName = kpi.getName();

		//SbiDataSet dsC = kpi.getSbiDataSet();
		IDataSet dsC = DAOFactory.getDataSetDAO().loadDataSetById(kpi.getSbiDataSet());
		Integer dsId = null;
		String dsLabel = null;
		if (dsC != null) {
			dsId = dsC.getId();
			dsLabel = dsC.getLabel();
		}

		Double standardWeight = kpi.getWeight();

		String metric = kpi.getMetric();

		String interpretation = kpi.getInterpretation(); 
		String inputAttribute = kpi.getInputAttributes();
		String modelReference = kpi.getModelReference();
		String targetAudience = kpi.getTargetAudience();

		Integer kpiTypeId = null;
		String kpiTypeCd = null;
		Integer metricScaleId = null;
		String metricScaleCd = null;
		Integer measureTypeId = null;
		String measureTypeCd = null;

		if (kpi.getSbiDomainsByKpiType() != null) {
			kpiTypeId = kpi.getSbiDomainsByKpiType().getValueId();
			kpiTypeCd = kpi.getSbiDomainsByKpiType().getValueCd();
		}

		if (kpi.getSbiDomainsByMeasureType() != null) {
			measureTypeId = kpi.getSbiDomainsByMeasureType().getValueId();
			measureTypeCd = kpi.getSbiDomainsByMeasureType().getValueCd();
		}

		if (kpi.getSbiDomainsByMetricScaleType() != null) {
			metricScaleId = kpi.getSbiDomainsByMetricScaleType().getValueId();
			metricScaleCd = kpi.getSbiDomainsByMetricScaleType().getValueCd();
		}

		Set kpiDocs = kpi.getSbiKpiDocumentses();
		List kpiDocsList = new ArrayList();
		if(kpiDocs!=null && !kpiDocs.isEmpty()){
			Iterator i = kpiDocs.iterator();
			while (i.hasNext()) {
				SbiKpiDocument doc = (SbiKpiDocument) i.next();
				if(doc!=null){				
					KpiDocuments temp = new KpiDocuments();
					temp.setBiObjId(doc.getSbiObjects().getBiobjId());
					temp.setBiObjLabel(doc.getSbiObjects().getLabel());
					temp.setKpiDocId(doc.getIdKpiDoc());
					temp.setKpiId(doc.getSbiKpi().getKpiId());
					kpiDocsList.add(temp);
				}
			}
		}

		toReturn.setKpiName(kpiName);
		logger.debug("Kpi name setted");
		toReturn.setSbiKpiDocuments(kpiDocsList);
		logger.debug("Kpi DocumentIds setted");
		toReturn.setCode(code);
		logger.debug("Kpi code setted");
		toReturn.setMetric(metric);
		logger.debug("Kpi metric setted");
		toReturn.setDescription(description);
		logger.debug("Kpi description setted");
		toReturn.setStandardWeight(standardWeight);
		logger.debug("Kpi weight setted");
		toReturn.setIsAdditive(kpi.getIsAdditive());

		toReturn.setKpiId(kpiId);
		logger.debug("Kpi Id setted");
		toReturn.setDsLabel(dsLabel);
		toReturn.setKpiDsId(dsId);
		logger.debug("Kpi dataset setted");

		if (kpi.getSbiThreshold() != null) {
			Threshold threshold = DAOFactory.getThresholdDAO().loadThresholdById(
					kpi.getSbiThreshold().getThresholdId());
			toReturn.setThreshold(threshold);
			logger.debug("Kpi threshold setted");

		}

		// add also associated UDP
		List udpValues = DAOFactory.getUdpDAOValue().findByReferenceId(kpiId, "Kpi");
		toReturn.setUdpValues(udpValues);


		toReturn.setInterpretation(interpretation);
		logger.debug("Kpi Interpretation setted");
		toReturn.setInputAttribute(inputAttribute);
		logger.debug("Kpi InputAttribute setted");
		toReturn.setModelReference(modelReference);	
		logger.debug("Kpi ModelReference se	tted");
		toReturn.setTargetAudience(targetAudience);
		logger.debug("Kpi TargetAudience setted");

		toReturn.setKpiTypeId(kpiTypeId);
		toReturn.setKpiTypeCd(kpiTypeCd);
		logger.debug("Kpi KpiTypeId setted");
		toReturn.setMetricScaleId(metricScaleId);
		toReturn.setMetricScaleCd(metricScaleCd);
		logger.debug("Kpi MetricScaleId setted");
		toReturn.setMeasureTypeId(measureTypeId);
		toReturn.setMeasureTypeCd(measureTypeCd);
		logger.debug("Kpi MeasureTypeId setted");

		logger.debug("OUT");
		return toReturn;

	}

	public Kpi loadKpiById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Kpi toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpi hibSbiKpiInstance = (SbiKpi) aSession.load(SbiKpi.class, id);
			toReturn = toKpi(hibSbiKpiInstance);

		} catch (HibernateException he) {
			logger.error("Error while loading the Kpi with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10112);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public String getKpiTrendXmlResult(Integer resId, Integer kpiInstId,
			Date endDate) throws SourceBeanException {

		logger.debug("IN");
		String toReturn = "";
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select max(s.idKpiInstanceValue) , s.beginDt";
			hql += " from SbiKpiValue s where s.sbiKpiInstance.idKpiInstance = ? ";
			hql += " and s.beginDt <= ? " ;
			hql += " and s.endDt > ? ";
			if (resId != null) {
				hql += " and s.sbiResources.resourceId = ? ";
			} else {
				logger.debug("Null resource setted");
			}
			hql += "group by s.beginDt order by s.beginDt desc";         

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiInstId);
			hqlQuery.setDate(1, endDate);
			hqlQuery.setDate(2, endDate);
			if (resId != null) {
				hqlQuery.setInteger(2, resId);
				logger.debug("Resource setted");
			} else {
				logger.debug("Null resource setted");
			}	
			hqlQuery.setMaxResults(10);

			SourceBean sb = new SourceBean("ROWS");
			List l = hqlQuery.list();
			if (!l.isEmpty()) {
				logger.debug("The result list is not empty");
				for (int k = l.size() - 1; k >= 0; k--) {
					Object[] tempL =  (Object[])l.get(k);
					Integer kpiValueId = (Integer) tempL[0];
					SbiKpiValue temp = (SbiKpiValue) aSession.load(SbiKpiValue.class, kpiValueId);
					SourceBean sb2 = new SourceBean("ROW");
					if (temp!=null && temp.getValue() != null) {
						sb2.setAttribute("x", temp.getBeginDt());
						sb2.setAttribute("KPI_VALUE", temp.getValue());
						sb.setAttribute(sb2);
					}
				}
			} else {
				logger.debug("The result list is empty");
				SourceBean sb2 = new SourceBean("ROW");
				sb.setAttribute(sb2);
			}

			toReturn = sb.toString();

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		return toReturn;
	}

	public String getKpiTrendXmlResult(Integer resId, Integer kpiInstId, Date beginDate , Date endDate) throws SourceBeanException{

		logger.debug("IN");
		String toReturn = "";
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select max(s.idKpiInstanceValue) as VALUE, s.beginDt as DATE ";
			hql += " from SbiKpiValue s where s.sbiKpiInstance.idKpiInstance = ? ";
			hql += " and s.beginDt <= ? and s.beginDt >= ? ";
			if (resId != null) {
				hql += " and s.sbiResources.resourceId = ? ";
			} else {
				logger.debug("Null resource setted");
			}	
			hql += "group by s.beginDt order by s.beginDt desc";         

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiInstId);
			hqlQuery.setDate(1, endDate);
			hqlQuery.setDate(2, beginDate);
			if (resId != null) {
				hqlQuery.setInteger(3, resId);
				logger.debug("Resource setted");
			} else {
				logger.debug("Null resource setted");
			}				
			hqlQuery.setMaxResults(10);

			SourceBean sb = new SourceBean("ROWS");
			List l = hqlQuery.list();
			if (!l.isEmpty()) {
				logger.debug("The result list is not empty");
				for (int k = l.size() - 1; k >= 0; k--) {
					Object[] tempL =  (Object[])l.get(k);
					Integer kpiValueId = (Integer) tempL[0];
					SbiKpiValue temp = (SbiKpiValue) aSession.load(SbiKpiValue.class, kpiValueId);
					SourceBean sb2 = new SourceBean("ROW");
					if (temp !=null && temp.getValue() != null) {
						sb2.setAttribute("x", temp.getBeginDt());
						sb2.setAttribute("KPI_VALUE", temp.getValue());
						sb.setAttribute(sb2);
					}
				}
			} else {
				logger.debug("The result list is empty");
				SourceBean sb2 = new SourceBean("ROW");
				sb.setAttribute(sb2);
			}

			toReturn = sb.toString();

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		return toReturn;
	}

	public JSONObject getKpiTrendJSONResult(Integer kpiInstId, Date beginDate , Date endDate) throws SourceBeanException{
		logger.debug("IN");
		JSONObject toReturn = new JSONObject();
		int numRows = 0;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select max(s.idKpiInstanceValue), s.beginDt";
			hql += " from SbiKpiValue s where s.sbiKpiInstance.idKpiInstance = ? ";
			hql += " and s.beginDt <= ? and s.beginDt >= ? ";			
			hql += "group by s.beginDt order by s.beginDt desc";         

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiInstId);
			//hqlQuery.setDate(1, endDate);
			//hqlQuery.setDate(2, beginDate);	
			hqlQuery.setTimestamp(1, endDate);
			hqlQuery.setTimestamp(2, beginDate);

			//hqlQuery.setMaxResults(10);

			List l = hqlQuery.list();
			JSONArray jsonData = new JSONArray();
			
			if (!l.isEmpty()) {
				logger.debug("The result list is not empty");
				for (int k = l.size() - 1; k >= 0; k--) {
					Object[] tempL =  (Object[])l.get(k);
					JSONObject jsonObj = new JSONObject();
					
					Integer kpiValueId = (Integer) tempL[0];
					SbiKpiValue temp = (SbiKpiValue) aSession.load(SbiKpiValue.class, kpiValueId);
					if (temp !=null && temp.getValue() != null) {
						try{
							numRows++;
							Date dt = temp.getBeginDt();							
							SimpleDateFormat sdf;
							sdf = new SimpleDateFormat("d");
							String day = sdf.format(dt);
							sdf = new SimpleDateFormat("MM");
							String month = sdf.format(dt);
							sdf = new SimpleDateFormat("yyyy");
							String year = sdf.format(dt);
							sdf = new SimpleDateFormat("H");
							String hour = sdf.format(dt);
							sdf = new SimpleDateFormat("m");
							String min = sdf.format(dt);
							sdf = new SimpleDateFormat("s");
							String sec = sdf.format(dt);

							String format  =  GeneralUtilities.getServerTimeStampFormat();
							String strDtReturn = day + "/" + month + "/" + year + " " + hour + ":" + min + ":" + sec;	
							sdf = new SimpleDateFormat(format);						
							Float valueReturn = Float.parseFloat(temp.getValue());
							
							logger.debug("Date for KPI : " + dt );
							logger.debug("Value of KPI: " + valueReturn );
							
							jsonObj.put("id", k+1);
							jsonObj.put("KPI_DATE",  strDtReturn); //KPI_DATE
							jsonObj.put("KPI_VALUE",  valueReturn); //KPI_VALUE
							jsonData.put(jsonObj);
						}catch (Exception e) {
							logger.error("Error while getting trend data",e);
							return null;		
						}
					}
				}
			} 

			toReturn = new JSONObject();
			toReturn.put("trends", jsonData);

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
		}catch (Exception e) {
			logger.error("Error while getting trend data",e);
			return null;		
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		return toReturn;
	}

	
	public Integer getKpiTrend(Integer resId, Integer kpiInstId, Date endDate) throws Exception{

		logger.debug("IN");
		Integer toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select max(s.idKpiInstanceValue), s.value, s.beginDt";
			hql += " from SbiKpiValue s where s.sbiKpiInstance.idKpiInstance = ? ";
			hql += " and s.beginDt <= ? ";
			//hql += " and s.endDt > ? ";
			if (resId != null) {
				hql += " and s.sbiResources.resourceId = ? ";
			} else {
				logger.debug("Null resource setted");
			}	
			hql += "group by s.beginDt, s.value order by s.beginDt desc";         

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiInstId);
			hqlQuery.setTimestamp(1, endDate);
			//hqlQuery.setTimestamp(2, endDate);
			if (resId != null) {
				hqlQuery.setInteger(3, resId);
				logger.debug("Resource setted");
			} else {
				logger.debug("Null resource setted");
			}				
			hqlQuery.setMaxResults(2);

			List l = hqlQuery.list();
			
			Double lastValue = null;
			Double previousValue = null;
			if (!l.isEmpty()) {
				logger.debug("The result list is not empty");
				//for (int k = l.size() - 1; k >= 0; k--) {
				for (int k = 0; k < l.size(); k++) {
					Object[] tempL =  (Object[])l.get(k);
					Integer kpiValueId = (Integer) tempL[0];
					//SbiKpiValue temp = (SbiKpiValue) aSession.load(SbiKpiValue.class, kpiValueId);
					String tempVal =  (String)tempL[1];
					if(tempVal != null){
						if(lastValue == null){
							lastValue = Double.parseDouble(tempVal);
						}else{
							previousValue = Double.parseDouble(tempVal);
						}
					}
					
				}
				if(previousValue == null){
					return null;
				}else{
					logger.debug(lastValue +"  "+previousValue);
					if(lastValue != null && lastValue != null 
							&& previousValue != null && previousValue != null){
						if(lastValue > previousValue ){
							toReturn = 1;
						}else if(lastValue < previousValue ){
							toReturn = -1;
						}else {
							toReturn = 0;
						}
					}
				}
			} else {
				logger.debug("The result list is empty");
			}


		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		return toReturn;
	}
	public List getKpiValue(SbiKpiInstance kpi, Date d) throws EMFUserError {

		logger.debug("IN");

		Integer kpiInstID = kpi.getIdKpiInstance();
		Session aSession = null;
		Transaction tx = null;
		SbiKpiInstance hibKpiInstance = null;
		List values = new ArrayList();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			hibKpiInstance = (SbiKpiInstance) aSession.load(
					SbiKpiInstance.class, kpiInstID);
			Set kpiValues = hibKpiInstance.getSbiKpiValues();
			SbiDomains dom = hibKpiInstance.getChartType();
			String chartType = null;
			if (dom != null)
				chartType = dom.getValueCd();

			Iterator iVa = kpiValues.iterator();
			while (iVa.hasNext()) {
				SbiKpiValue value = (SbiKpiValue) iVa.next();
				Date kpiValueBegDt = value.getBeginDt();
				logger.debug("Kpi value begin date: "
						+ (kpiValueBegDt != null ? kpiValueBegDt.toString()
								: "Begin date null"));
				Date kpiValueEndDt = value.getEndDt();
				logger.debug("Kpi value end date: "
						+ (kpiValueEndDt != null ? kpiValueEndDt.toString()
								: "End date null"));

				logger.debug("Date in which the value has to be valid: "
						+ (d != null ? d.toString() : "Date null"));
				if (d.after(kpiValueBegDt) && d.before(kpiValueEndDt)) {
					logger.debug("Date between beginDate and EndDate");
					KpiValue val = toKpiValue(value, d);
					if (chartType != null) {
						val.setChartType(chartType);
						logger.debug("Setted chart Type: " + chartType);
					}
					values.add(val);
					logger.debug("Setted the correct value: " + val.getValue());
				}
			}

		} catch (HibernateException he) {
			logger
			.error(
					"Error while getting the List of KpiValues related to the SbiKpiInstance with id "
					+ ((kpiInstID == null) ? "" : kpiInstID
							.toString()) + "at the Date " + d,
							he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10102);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return values;
	}



	public Integer insertKpiValue(KpiValue value) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer kpiValueId = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiValue hibKpiValue = new SbiKpiValue();
			Date beginDt = value.getBeginDate();
			logger
			.debug("Kpi value begin date: "
					+ (beginDt != null ? beginDt.toString()
							: "Begin date null"));
			Date endDt = value.getEndDate();
			logger.debug("Kpi value end date: "
					+ (endDt != null ? endDt.toString() : "End date null"));
			String valueDescr = value.getValueDescr();
			logger.debug("Kpi value: "
					+ (valueDescr != null ? valueDescr
							: "value Description null"));
			String kpiValue = value.getValue();
			logger.debug("Kpi value: "
					+ (kpiValue != null ? kpiValue : "Value null"));
			Integer kpiInstanceId = value.getKpiInstanceId();
			logger.debug("Kpi Instance ID: "
					+ (kpiInstanceId != null ? kpiInstanceId.toString()
							: "Kpi Instance ID null"));
			SbiKpiInstance sbiKpiInstance = (SbiKpiInstance) aSession.load(
					SbiKpiInstance.class, kpiInstanceId);
			Resource r = value.getR();
			if (r != null) {
				IResourceDAO resDaoImpl=DAOFactory.getResourceDAO();
				SbiResources sbiResources = resDaoImpl.toSbiResource(r);
				logger.debug("Resource: "
						+ (r.getName() != null ? r.getName()
								: "Resource name null"));
				hibKpiValue.setSbiResources(sbiResources);
			}
			OrganizationalUnitGrantNode grantNode = value.getGrantNodeOU();
			if (grantNode != null) {
				OrganizationalUnit ou = grantNode.getOuNode().getOu();
				SbiOrgUnit hibOU = new SbiOrgUnit();
				hibOU.setLabel(ou.getLabel());
				hibOU.setName(ou.getName());
				hibOU.setDescription(ou.getDescription());
				hibOU.setId(ou.getId());
				
				logger.debug("Organizational unit: "
						+ (ou.getName() != null ? ou.getName()
								: "OU name null"));
				hibKpiValue.setSbiOrgUnit(hibOU);
				//same for hierarchy
				OrganizationalUnitHierarchy hier = grantNode.getOuNode().getHierarchy();
				SbiOrgUnitHierarchies hibHier = new SbiOrgUnitHierarchies();
				hibHier.setDescription(hier.getDescription());
				hibHier.setId(hier.getId());
				hibHier.setLabel(hier.getLabel());
				hibHier.setName(hier.getName());
				hibHier.setTarget(hier.getTarget());
				hibHier.setCompany(hier.getCompany());
				
				hibKpiValue.setSbiOrgUnitHierarchies(hibHier);
				//inserts company too as standalone column field
				hibKpiValue.setCompany(hier.getCompany());
				
			}
			hibKpiValue.setDescription(valueDescr);
			logger.debug("Kpi value description setted");
			hibKpiValue.setBeginDt(beginDt);
			logger.debug("Kpi value begin date setted");
			hibKpiValue.setEndDt(endDt);
			logger.debug("Kpi value end date setted");
			hibKpiValue.setValue(kpiValue);
			logger.debug("Kpi value setted");
			hibKpiValue.setSbiKpiInstance(sbiKpiInstance);
			logger.debug("Kpi Instance setted");
			hibKpiValue.setXmlData(value.getValueXml());
			updateSbiCommonInfo4Insert(hibKpiValue);
			kpiValueId = (Integer)aSession.save(hibKpiValue);
			tx.commit();
			return kpiValueId;
		} catch (Throwable he) {
			logger.error(
					"Error while inserting the KpiValue related to the KpiInstance with id "
					+ ((value.getKpiInstanceId() == null) ? "" : value
							.getKpiInstanceId().toString()), he);
			he.printStackTrace();

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10103);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}




	public KpiValue getKpiValue(Integer kpiInstanceId, Date d, Resource r, OrganizationalUnitGrantNode grantNode)
	throws EMFUserError {

		logger.debug("IN");
		KpiValue toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiKpiValue.class);
			finder.add(Expression.eq("sbiKpiInstance.idKpiInstance",
					kpiInstanceId));
			finder.add(Expression.le("beginDt", d));
			finder.add(Expression.ge("endDt", d));
			finder.addOrder(Order.desc("beginDt"));
			finder.addOrder(Order.desc("idKpiInstanceValue"));
			logger.debug("Order Date Criteria setted");
			finder.setMaxResults(1);
			logger.debug("Max result to 1 setted");

			if (r != null) {
				finder.add(Expression.eq("sbiResources.resourceId", r.getId()));
			}
			if (grantNode != null) {
				finder.add(Expression.eq("sbiOrgUnit.id", grantNode.getOuNode().getOu().getId()));
				finder.add(Expression.eq("sbiOrgUnitHierarchies.id", grantNode.getOuNode().getHierarchy().getId()));
				if(grantNode.getOuNode().getHierarchy().getCompany() != null){
					finder.add(Expression.eq("company", grantNode.getOuNode().getHierarchy().getCompany()));
				}
			}
			List l = finder.list();
			if (!l.isEmpty()) {
				KpiValue tem = null;
				Iterator it = l.iterator();
				while (it.hasNext()) {
					SbiKpiValue temp = (SbiKpiValue) it.next();
					toReturn = toKpiValue(temp, d);
				}
			}

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10108);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private KpiValue toKpiValue(SbiKpiValue value, Date d) throws EMFUserError {

		logger.debug("IN");
		KpiValue toReturn = new KpiValue();

		Date beginDate = value.getBeginDt();
		logger
		.debug("SbiKpiValue begin date: "
				+ (beginDate != null ? beginDate.toString()
						: "Begin date null"));
		Date endDate = value.getEndDt();
		logger.debug("SbiKpiValue end date: "
				+ (endDate != null ? endDate.toString() : "End date null"));
		String val = value.getValue();
		logger
		.debug("SbiKpiValue value: "
				+ (val != null ? val : "Value null"));
		String valueDescr = value.getDescription();
		logger.debug("SbiKpiValue description: "
				+ (valueDescr != null ? valueDescr : "Value description null"));
		Integer kpiInstanceID = null;
		Double weight = null;
		Double target = null;
		String scaleCode = null;
		String scaleName = null;

		SbiResources res = value.getSbiResources();
		Resource r = null;
		IResourceDAO resDao=DAOFactory.getResourceDAO();
		if (res != null) {
			r = resDao.toResource(res);
			logger.debug("SbiKpiValue resource: "
					+ (r.getColumn_name() != null ? r.getColumn_name()
							: "resource name null"));
		}

		kpiInstanceID = value.getSbiKpiInstance().getIdKpiInstance();
		logger.debug("SbiKpiValue kpiInstanceID: "
				+ (kpiInstanceID != null ? kpiInstanceID.toString()
						: "kpiInstanceID null"));
		SbiKpiInstance kpiInst = value.getSbiKpiInstance();

		List thresholdValues = new ArrayList();
		Date kpiInstBegDt = kpiInst.getBeginDt();
		logger.debug("kpiInstBegDt begin date: "
				+ (kpiInstBegDt != null ? kpiInstBegDt.toString()
						: "Begin date null"));
		// in case the current threshold is correct
		if (((d.before(endDate) || d.equals(endDate))
				&& (d.after(beginDate) || d.equals(beginDate))
				&& (d.after(kpiInstBegDt) || d.equals(kpiInstBegDt)))|| kpiInst.getSbiKpiInstanceHistories().isEmpty()) {

			weight = kpiInst.getWeight();
			logger.debug("SbiKpiValue weight: "
					+ (weight != null ? weight.toString() : "weight null"));
			target = kpiInst.getTarget();
			logger.debug("SbiKpiValue target: "
					+ (target != null ? target.toString() : "target null"));

			if (kpiInst.getSbiMeasureUnit() != null) {
				scaleCode = kpiInst.getSbiMeasureUnit().getScaleCd();
				logger.debug("SbiKpiValue scaleCode: "
						+ (scaleCode != null ? scaleCode : "scaleCode null"));
				scaleName = kpiInst.getSbiMeasureUnit().getScaleNm();
				logger.debug("SbiKpiValue scaleName: "
						+ (scaleName != null ? scaleName : "scaleName null"));
			}
			logger.debug("Requested date d: " + d.toString()
					+ " in between beginDate and EndDate");
			SbiThreshold t = kpiInst.getSbiThreshold();
			if(t!=null){

				Set ts = t.getSbiThresholdValues();
				Iterator i = ts.iterator();
				while (i.hasNext()) {
					SbiThresholdValue tls = (SbiThresholdValue) i.next();

					IThresholdValueDAO thDao=(IThresholdValueDAO)DAOFactory.getThresholdValueDAO();
					ThresholdValue tr = thDao.toThresholdValue(tls);
					thresholdValues.add(tr);
				}
			}			

		} else {// in case older thresholds have to be retrieved

			Set kpiInstHist = kpiInst.getSbiKpiInstanceHistories();
			Iterator i = kpiInstHist.iterator();
			while (i.hasNext()) {
				SbiKpiInstanceHistory ih = (SbiKpiInstanceHistory) i.next();
				Date ihBegDt = ih.getBeginDt();
				Date ihEndDt = ih.getEndDt();
				if ((d.after(ihBegDt) || d.equals(ihBegDt))
						&& (d.before(ihEndDt) || d.equals(ihEndDt))) {

					weight = ih.getWeight();
					logger.debug("SbiKpiValue weight: "
							+ (weight != null ? weight.toString()
									: "weight null"));
					target = ih.getTarget();
					logger.debug("SbiKpiValue target: "
							+ (target != null ? target.toString()
									: "target null"));

					if (ih.getSbiMeasureUnit() != null) {
						scaleCode = ih.getSbiMeasureUnit().getScaleCd();
						logger.debug("SbiKpiValue scaleCode: "
								+ (scaleCode != null ? scaleCode
										: "scaleCode null"));
						scaleName = ih.getSbiMeasureUnit().getScaleNm();
						logger.debug("SbiKpiValue scaleName: "
								+ (scaleName != null ? scaleName
										: "scaleName null"));
					}
					SbiThreshold t = ih.getSbiThreshold();
					if(t!=null){
						Set ts = t.getSbiThresholdValues();
						Iterator it = ts.iterator();
						while (it.hasNext()) {
							SbiThresholdValue tls = (SbiThresholdValue) it.next();

							IThresholdValueDAO thDao=(IThresholdValueDAO)DAOFactory.getThresholdValueDAO();
							ThresholdValue tr = thDao.toThresholdValue(tls);
							thresholdValues.add(tr);
						}
					}		
				}
			}
		}
		toReturn.setValueDescr(valueDescr);
		logger.debug("Kpi value descritpion setted");
		toReturn.setTarget(target);
		logger.debug("Kpi value target setted");
		toReturn.setBeginDate(beginDate);
		logger.debug("Kpi value begin date setted");
		toReturn.setEndDate(endDate);
		logger.debug("Kpi value end date setted");
		toReturn.setValue(val);
		logger.debug("Kpi value setted");
		toReturn.setKpiInstanceId(kpiInstanceID);
		logger.debug("Kpi value Instance ID setted");
		toReturn.setWeight(weight);
		logger.debug("Kpi value weight setted");
		toReturn.setR(r);
		logger.debug("Kpi value resource setted");
		toReturn.setScaleCode(scaleCode);
		logger.debug("Kpi value scale Code setted");
		toReturn.setScaleName(scaleName);
		logger.debug("Kpi value scale Name setted");
		toReturn.setThresholdValues(thresholdValues);
		logger.debug("Kpi value Thresholds setted");
		toReturn.setKpiValueId(value.getIdKpiInstanceValue());
		logger.debug("Kpi value ID setted");
		toReturn.setValueXml(value.getXmlData());
		logger.debug("Kpi value XML setted");
		OrganizationalUnitGrantNode grantNode = new OrganizationalUnitGrantNode();
		OrganizationalUnitNode node = new OrganizationalUnitNode();
		if(value.getSbiOrgUnit() != null){
			OrganizationalUnit ou = DAOFactory.getOrganizationalUnitDAO().getOrganizationalUnit(value.getSbiOrgUnit().getId());
			node.setOu(ou);
		}
		if(value.getSbiOrgUnitHierarchies() != null){
			OrganizationalUnitHierarchy hierarchy = DAOFactory.getOrganizationalUnitDAO().getHierarchy(value.getSbiOrgUnitHierarchies().getId());
			node.setHierarchy(hierarchy);
		}
		if(value.getSbiOrgUnit() != null && value.getSbiOrgUnitHierarchies() != null){
			grantNode.setOuNode(node);
			toReturn.setGrantNodeOU(grantNode);
		}

		logger.debug("Kpi value organizational unit grant node setted");

		logger.debug("OUT");
		return toReturn;
	}


	public Kpi toKpi(SbiKpi kpi) throws EMFUserError {

		logger.debug("IN");
		Kpi toReturn = new Kpi();

		String code = kpi.getCode();
		String description = kpi.getDescription();
		String metric = kpi.getMetric();
		String interpretation = kpi.getInterpretation();

		String inputAttributes=kpi.getInputAttributes();
		String modelReference=kpi.getModelReference();
		String targetAudience=kpi.getTargetAudience();

		Boolean isParent = false;
		if (kpi.getFlgIsFather() != null
				&& kpi.getFlgIsFather().equals(new Character('T'))) {
			isParent = true;
		}

		Integer kpiId = kpi.getKpiId();
		String kpiName = kpi.getName();
		//SbiDataSet dsC = kpi.getSbiDataSet();
		IDataSet dsC = null;
		if (kpi.getSbiDataSet() != null) {
			dsC = DAOFactory.getDataSetDAO().loadDataSetById(kpi.getSbiDataSet());
		}
		Integer dsId = null;
		String dsLabel = null;
		if (dsC != null) {
			dsId = dsC.getId();
			dsLabel = dsC.getLabel();
		}

		IThresholdDAO thresholdDAO=DAOFactory.getThresholdDAO();
		SbiThreshold thresh = kpi.getSbiThreshold();
		if(thresh!=null){
			Threshold threshold=thresholdDAO.toThreshold(thresh);
			toReturn.setThreshold(threshold);
			logger.debug("Kpi threshold setted");
		}

		Double standardWeight = kpi.getWeight();

		// Gets the father
		SbiKpi dad = kpi.getSbiKpi();
		Boolean isRoot = false;
		Integer father = null;
		if (dad != null) {
			father = dad.getKpiId();
		} else {
			isRoot = true;
		}

		String scaleCode = "";
		String scaleName = "";
		if (kpi.getSbiMeasureUnit() != null) {
			scaleCode = kpi.getSbiMeasureUnit().getScaleCd();
			scaleName = kpi.getSbiMeasureUnit().getScaleNm();
		}


		Set kpiDocs = kpi.getSbiKpiDocumentses();
		List kpiDocsList = new ArrayList();
		if(kpiDocs!=null && !kpiDocs.isEmpty()){
			Iterator i = kpiDocs.iterator();
			while (i.hasNext()) {
				SbiKpiDocument doc = (SbiKpiDocument) i.next();
				if(doc!=null){
					KpiDocuments temp = new KpiDocuments();
					temp.setBiObjId(doc.getSbiObjects().getBiobjId());
					temp.setBiObjLabel(doc.getSbiObjects().getLabel());
					temp.setKpiDocId(doc.getIdKpiDoc());
					temp.setKpiId(doc.getSbiKpi().getKpiId());
					kpiDocsList.add(temp);
				}
			}
		}

		// add also associated UDP
		List udpValues = DAOFactory.getUdpDAOValue().findByReferenceId(kpiId, "Kpi");
		toReturn.setUdpValues(udpValues);


		toReturn.setDescription(description);
		logger.debug("Kpi description setted");
		toReturn.setSbiKpiDocuments(kpiDocsList);
		logger.debug("Kpi Documentlabels setted");
		toReturn.setIsParent(isParent);
		logger.debug("Kpi isParent setted");
		toReturn.setIsRoot(isRoot);
		logger.debug("Kpi isRoot setted");
		toReturn.setKpiDsId(dsId);
		toReturn.setDsLabel(dsLabel);
		logger.debug("Kpi dataset setted");
		toReturn.setKpiId(kpiId);
		logger.debug("Kpi Id setted");
		toReturn.setKpiName(kpiName);
		logger.debug("Kpi name setted");
		toReturn.setStandardWeight(standardWeight);
		logger.debug("Kpi weight setted");
		toReturn.setIsAdditive(kpi.getIsAdditive());
		toReturn.setCode(code);
		logger.debug("Kpi code setted");
		toReturn.setMetric(metric);
		logger.debug("Kpi metric setted");
		toReturn.setScaleCode(scaleCode);
		logger.debug("Kpi scaleCode setted");
		toReturn.setScaleName(scaleName);
		logger.debug("Kpi scaleName setted");
		toReturn.setInterpretation(interpretation);
		logger.debug("Interpretation setted");
		toReturn.setModelReference(modelReference);
		logger.debug("modelReference setted");
		toReturn.setTargetAudience(targetAudience);
		logger.debug("targetAudience setted");
		toReturn.setInputAttribute(inputAttributes);
		logger.debug("inputAttributes setted");	

		if(kpi.getSbiDomainsByKpiType()!=null){
			toReturn.setKpiTypeId(kpi.getSbiDomainsByKpiType().getValueId());
			toReturn.setKpiTypeCd(kpi.getSbiDomainsByKpiType().getValueCd());
		}
		if(kpi.getSbiDomainsByMeasureType()!=null){
			toReturn.setMeasureTypeId(kpi.getSbiDomainsByMeasureType().getValueId());
			toReturn.setMeasureTypeCd(kpi.getSbiDomainsByMeasureType().getValueCd());
		}
		if(kpi.getSbiDomainsByMetricScaleType()!=null){
			toReturn.setMetricScaleId(kpi.getSbiDomainsByMetricScaleType().getValueId());
			toReturn.setMetricScaleCd(kpi.getSbiDomainsByMetricScaleType().getValueCd());
		}

		logger.debug("OUT");
		return toReturn;
	}


	private String getKpiProperty(String property) {
		String toReturn = null;
		if (property != null && property.toUpperCase().equals("CODE"))
			toReturn = "code";
		if (property != null && property.toUpperCase().equals("NAME"))
			toReturn = "name";
		if (property != null && property.toUpperCase().equals("DESCRIPTION"))
			toReturn = "description";
		if (property != null && property.toUpperCase().equals("THRESHOLD"))
			toReturn = "sbiThreshold";
		if (property != null && property.toUpperCase().equals("THRESHOLDCODE"))
			toReturn = "sbiThreshold";
		return toReturn;
	}


	public List loadKpiList(String fieldOrder, String typeOrder)
	throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
			if (fieldOrder != null && typeOrder != null) {
				Criteria crit = aSession.createCriteria(SbiKpi.class);
				if (typeOrder.toUpperCase().trim().equals("ASC"))
					crit.addOrder(Order.asc(getKpiProperty(fieldOrder)));
				if (typeOrder.toUpperCase().trim().equals("DESC"))
					crit.addOrder(Order.desc(getKpiProperty(fieldOrder)));
				toTransform = crit.list();
			} else {
				toTransform = aSession.createQuery("from SbiKpi").list();
			}

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiKpi hibKpi = (SbiKpi) iterator.next();
				Kpi kpi = new Kpi();
				kpi.setCode(hibKpi.getCode());
				kpi.setDescription(hibKpi.getDescription());
				kpi.setKpiName(hibKpi.getName());
				kpi.setKpiId(hibKpi.getKpiId());
				if(hibKpi.getSbiThreshold() != null){
					Threshold threshold = new Threshold();
					threshold.setId(hibKpi.getSbiThreshold().getThresholdId());
					threshold.setName(hibKpi.getSbiThreshold().getName());
					kpi.setThreshold(threshold);
				}
				toReturn.add(kpi);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Kpi", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public List loadKpiList() throws EMFUserError {
		return loadKpiList(null, null);
	}

	public void modifyKpi(Kpi kpi) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String name = kpi.getKpiName();
			String description = kpi.getDescription();
			String code = kpi.getCode();
			String metric = kpi.getMetric();
			Double weight = kpi.getStandardWeight();
			SbiDataSet ds = null;
			String interpretation = kpi.getInterpretation(); 
			String inputAttribute = kpi.getInputAttribute();
			String modelReference = kpi.getModelReference();
			String targetAudience = kpi.getTargetAudience();

			if (kpi.getKpiDsId() != null) {
				Integer ds_id = kpi.getKpiDsId() ;
				//ds = (SbiDataSet) aSession.load(SbiDataSet.class,ds_id);
				Query countQuery = aSession.createQuery("from SbiDataSet ds where ds.active = ? and ds.id.dsId = ?");
				countQuery.setBoolean(0, true);
				countQuery.setInteger(1, ds_id);
				ds = (SbiDataSet)countQuery.uniqueResult();
			}

			SbiThreshold sbiThreshold = null;
			if (kpi.getThreshold() != null) {
				Integer thresholdId = kpi.getThreshold().getId();
				sbiThreshold = (SbiThreshold) aSession.load(SbiThreshold.class, thresholdId);
			}

			SbiKpi sbiKpi = (SbiKpi) aSession.load(SbiKpi.class, kpi.getKpiId());

			SbiDomains kpiType = null;
			if (kpi.getKpiTypeId() != null) {
				Integer kpiTypeId = kpi.getKpiTypeId();
				kpiType = (SbiDomains) aSession.load(SbiDomains.class, kpiTypeId);
			}

			SbiDomains metricScaleType = null;
			if (kpi.getMetricScaleId() != null) {
				Integer metricScaleId = kpi.getMetricScaleId();
				metricScaleType = (SbiDomains) aSession.load(SbiDomains.class, metricScaleId);
			}

			SbiDomains measureType = null;
			if (kpi.getMeasureTypeId() != null) {
				Integer measureTypeId = kpi.getMeasureTypeId();
				measureType = (SbiDomains) aSession.load(SbiDomains.class, measureTypeId);
			}



			//Loading all old sbiObjects
			Criterion kpiCriter = Expression.eq("sbiKpi",sbiKpi);
			Criteria crite = aSession.createCriteria(SbiKpiDocument.class);
			crite.add(kpiCriter);
			List existingDocs = crite.list();

			List kpiDocsList = kpi.getSbiKpiDocuments();
			Set sbiKpiDocuments = new HashSet(0);
			Iterator i = kpiDocsList.iterator();
			while (i.hasNext()) {

				KpiDocuments doc = (KpiDocuments) i.next();

				String label = doc.getBiObjLabel();
				Criterion labelCriterrion = Expression.eq("label",label);
				Criteria criteria = aSession.createCriteria(SbiObjects.class);
				criteria.add(labelCriterrion);
				SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();



				Integer kpiId = kpi.getKpiId();
				Criterion kpiCriterrion = Expression.eq("sbiKpi",sbiKpi);
				Criterion sbiObjCriterrion = Expression.eq("sbiObjects",hibObject);
				Criteria crit = aSession.createCriteria(SbiKpiDocument.class);
				crit.add(kpiCriterrion);
				crit.add(sbiObjCriterrion);
				SbiKpiDocument kpiDoc = (SbiKpiDocument) crit.uniqueResult();

				if(existingDocs!=null && !existingDocs.isEmpty() && kpiDoc!=null){
					if(existingDocs.contains(kpiDoc)){
						existingDocs.remove(kpiDoc);
					}
				}
				if(kpiDoc==null){
					SbiKpiDocument temp = new SbiKpiDocument();
					temp.setSbiKpi(sbiKpi);
					temp.setSbiObjects(hibObject);
					updateSbiCommonInfo4Update(temp);
					aSession.saveOrUpdate(temp);
				}
			}

			if(existingDocs!=null && !existingDocs.isEmpty() ){
				Iterator it2 = existingDocs.iterator();
				while(it2.hasNext()){
					SbiKpiDocument kpiDoc = (SbiKpiDocument) it2.next();
					aSession.delete(kpiDoc);
				}
			}

			sbiKpi.setInterpretation(interpretation);
			sbiKpi.setInputAttributes(inputAttribute);
			sbiKpi.setModelReference(modelReference);
			sbiKpi.setTargetAudience(targetAudience);
			sbiKpi.setSbiDomainsByKpiType(kpiType);
			sbiKpi.setSbiDomainsByMeasureType(measureType);
			sbiKpi.setSbiDomainsByMetricScaleType(metricScaleType);
			sbiKpi.setName(name);
			sbiKpi.setDescription(description);
			sbiKpi.setCode(code);
			sbiKpi.setMetric(metric);
			sbiKpi.setWeight(weight);
			sbiKpi.setIsAdditive(kpi.getIsAdditive());
			//sbiKpi.setSbiKpiDocumentses(sbiKpiDocuments);
			sbiKpi.setSbiDataSet(ds.getId().getDsId());
			sbiKpi.setSbiThreshold(sbiThreshold);
			updateSbiCommonInfo4Update(sbiKpi);
			aSession.saveOrUpdate(sbiKpi);
			IUdpValueDAO dao=DAOFactory.getUdpDAOValue();
			dao.setUserProfile(getUserProfile());
			dao.insertOrUpdateRelatedUdpValues(kpi, sbiKpi, aSession, "Kpi");

			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			logger.error("error in modifying kpi");
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
	}







	public Integer insertKpi(Kpi kpi) throws EMFUserError {
		logger.debug("IN");
		Integer idToReturn;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String name = kpi.getKpiName();
			String description = kpi.getDescription();
			String code = kpi.getCode();
			String metric = kpi.getMetric();
			Double weight = kpi.getStandardWeight();

			SbiDataSet ds = null;
			if (kpi.getKpiDsId()  != null) {
				Integer ds_id = kpi.getKpiDsId();
				//ds = (SbiDataSet) aSession.load(SbiDataSet.class,ds_id);
				Query countQuery = aSession.createQuery("from SbiDataSet ds where ds.active = ? and ds.id.dsId = ?");
				countQuery.setBoolean(0, true);
				countQuery.setInteger(1, kpi.getKpiDsId());
				ds = (SbiDataSet)countQuery.uniqueResult();
			}

			SbiThreshold sbiThreshold = null;
			if (kpi.getThreshold() != null) {
				Integer thresholdId = kpi.getThreshold().getId();
				sbiThreshold = (SbiThreshold) aSession.load(SbiThreshold.class, thresholdId);
			}


			SbiKpi sbiKpi = new SbiKpi();

			String interpretation = kpi.getInterpretation(); 
			String inputAttribute = kpi.getInputAttribute();
			String modelReference = kpi.getModelReference();
			String targetAudience = kpi.getTargetAudience();

			SbiDomains kpiType = null;
			if (kpi.getKpiTypeId() != null) {
				Integer kpiTypeId = kpi.getKpiTypeId();
				kpiType = (SbiDomains) aSession.load(SbiDomains.class, kpiTypeId);
			}

			SbiDomains metricScaleType = null;
			if (kpi.getMetricScaleId() != null) {
				Integer metricScaleId = kpi.getMetricScaleId();
				metricScaleType = (SbiDomains) aSession.load(SbiDomains.class, metricScaleId);
			}

			SbiDomains measureType = null;
			if (kpi.getMeasureTypeId() != null) {
				Integer measureTypeId = kpi.getMeasureTypeId();
				measureType = (SbiDomains) aSession.load(SbiDomains.class, measureTypeId);
			}

			sbiKpi.setInterpretation(interpretation);
			sbiKpi.setInputAttributes(inputAttribute);
			sbiKpi.setModelReference(modelReference);
			sbiKpi.setTargetAudience(targetAudience);
			sbiKpi.setSbiDomainsByKpiType(kpiType);
			sbiKpi.setSbiDomainsByMeasureType(measureType);
			sbiKpi.setSbiDomainsByMetricScaleType(metricScaleType);
			sbiKpi.setName(name);
			sbiKpi.setDescription(description);
			sbiKpi.setCode(code);
			sbiKpi.setMetric(metric);
			sbiKpi.setWeight(weight);
			sbiKpi.setIsAdditive(kpi.getIsAdditive());
			sbiKpi.setSbiDataSet(ds.getId().getDsId());
			sbiKpi.setSbiThreshold(sbiThreshold);
			updateSbiCommonInfo4Insert(sbiKpi);
			idToReturn = (Integer) aSession.save(sbiKpi);

			List kpiDocsList = kpi.getSbiKpiDocuments();
			Set sbiKpiDocuments = new HashSet(0);
			Iterator i = kpiDocsList.iterator();
			while (i.hasNext()) {
				KpiDocuments doc = (KpiDocuments) i.next();
				String label = doc.getBiObjLabel();
				Criterion labelCriterrion = Expression.eq("label",label);
				Criteria criteria = aSession.createCriteria(SbiObjects.class);
				criteria.add(labelCriterrion);
				SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();

				SbiKpiDocument temp = new SbiKpiDocument();
				temp.setSbiKpi(sbiKpi);
				temp.setSbiObjects(hibObject);
				updateSbiCommonInfo4Insert(temp);
				aSession.save(temp);
			}

			//insertOrUpdateRelatedUdpValues(kpi, sbiKpi, aSession);
			IUdpValueDAO dao = DAOFactory.getUdpDAOValue();
			dao.setUserProfile(getUserProfile());
			dao.insertOrUpdateRelatedUdpValues(kpi, sbiKpi, aSession, "Kpi");

			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return idToReturn;
	}

	public boolean deleteKpi(Integer kpiId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiKpi akpi = (SbiKpi) aSession.load(SbiKpi.class, kpiId);
			String hql = "from SbiKpiDocument d where d.sbiKpi.kpiId = :id ";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger("id", kpiId);
			List<SbiKpiDocument> docs = (List<SbiKpiDocument>)hqlQuery.list();
			for(int i=0; i< docs.size();i++){
				aSession.delete(docs.get(i));
			}
			aSession.flush();
			aSession.delete(akpi);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Kpi", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10015);

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Kpi ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);
		} finally {
			aSession.close();
		}
		return true;
	}


	public IDataSet getDsFromKpiId(Integer kpiId) throws EMFUserError {
		logger.debug("IN");
		IDataSet toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpi k = (SbiKpi) aSession.load(SbiKpi.class, kpiId);
			/*SbiDataSet ds = k.getSbiDataSet();
			if (ds!=null){
				toReturn = DAOFactory.getDataSetDAO().loadActiveIDataSetByID(ds.getId().getDsId());
			}*/
			if (k.getSbiDataSet()!=null){
				toReturn = DAOFactory.getDataSetDAO().loadDataSetById(k.getSbiDataSet());
			}
		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10115);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}


	public Integer countKpis() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiKpi ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Kpis", he);	
			if (tx != null)
				tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}


	public List loadPagedKpiList(Integer offset, Integer fetchSize)
	throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;

			String hql = "select count(*) from SbiKpi ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}

			hibernateQuery = aSession.createQuery("from SbiKpi order by name");
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toTransform = hibernateQuery.list();			

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiKpi hibKpi = (SbiKpi) iterator.next();
				Kpi kpi = toKpi(hibKpi);
				toReturn.add(kpi);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Threshold", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public Integer setKpiRel(Integer kpiParentId, Integer kpiChildId,
			String parameter) throws EMFUserError {
		logger.debug("IN");
		Integer idToReturn;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiKpiRel kpiRelation = new SbiKpiRel();

			SbiKpi parentKpi = (SbiKpi)aSession.load(SbiKpi.class, kpiParentId);
			SbiKpi childKpi = (SbiKpi)aSession.load(SbiKpi.class, kpiChildId);

			kpiRelation.setParameter(parameter);
			kpiRelation.setSbiKpiByKpiChildId(childKpi);
			kpiRelation.setSbiKpiByKpiFatherId(parentKpi);

			idToReturn = (Integer)aSession.save(kpiRelation);

			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return idToReturn;
	}

	public List loadKpiRelListByParentId(Integer kpiParentId)
	throws EMFUserError {
		// TODO Auto-generated method stub
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			String query = "from SbiKpiRel r where r.sbiKpiByKpiFatherId.kpiId= :parentId";
			Query q = aSession.createQuery(query);
			q.setInteger("parentId", kpiParentId);
			List toTransform =  q.list();

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiKpiRel sbiKpiRel = (SbiKpiRel) iterator.next();
				KpiRel rel = toKpiRel(sbiKpiRel);
				toReturn.add(rel);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Kpi relations", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	public KpiRel toKpiRel(SbiKpiRel sbiKpiRel) throws EMFUserError {
		logger.debug("IN");
		KpiRel kpiRel = new KpiRel();
		if(sbiKpiRel != null){
			kpiRel.setKpiChildId(sbiKpiRel.getSbiKpiByKpiChildId().getKpiId());
			kpiRel.setKpiFatherId(sbiKpiRel.getSbiKpiByKpiFatherId().getKpiId());
			kpiRel.setParameter(sbiKpiRel.getParameter());
			kpiRel.setKpiRelId(sbiKpiRel.getKpiRelId());
			kpiRel.setKpiChild(toKpi(sbiKpiRel.getSbiKpiByKpiChildId()));
		}

		logger.debug("OUT");
		return kpiRel;

	}
	public boolean deleteKpiRel(Integer kpiRelId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiKpiRel akpirel = (SbiKpiRel) aSession.load(SbiKpiRel.class, kpiRelId);

			aSession.delete(akpirel);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Kpi relation", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10015);

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Kpi relation", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);
		} finally {
			aSession.close();
		}
		return true;
	}

	public KpiValue getKpiValueFromInterval(Integer kpiInstanceId, Date from, Date to, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError {
		logger.debug("IN");
		KpiValue toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiKpiValue.class);
			finder.add(Expression.eq("sbiKpiInstance.idKpiInstance",
					kpiInstanceId));
			finder.add(Expression.eq("beginDt", from));
			finder.add(Expression.eq("endDt", to));
			finder.addOrder(Order.desc("beginDt"));
			finder.addOrder(Order.desc("idKpiInstanceValue"));
			logger.debug("Order Date Criteria setted");
			finder.setMaxResults(1);
			logger.debug("Max result to 1 setted");

			if (r != null) {
				finder.add(Expression.eq("sbiResources.resourceId", r.getId()));
			}
			if (grantNode != null) {
				finder.add(Expression.eq("sbiOrgUnit.id", grantNode.getOuNode().getOu().getId()));
				finder.add(Expression.eq("sbiOrgUnitHierarchies.id", grantNode.getOuNode().getHierarchy().getId()));
				if(grantNode.getOuNode().getHierarchy().getCompany() != null){
					finder.add(Expression.eq("company", grantNode.getOuNode().getHierarchy().getCompany()));
				}
			}
			List l = finder.list();
			if (!l.isEmpty()) {
				KpiValue tem = null;
				Iterator it = l.iterator();
				while (it.hasNext()) {
					SbiKpiValue temp = (SbiKpiValue) it.next();
					toReturn = toKpiValue(temp, from, to);
				}
			}

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10108);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		logger.debug("OUT");
		return toReturn;
	}
	
	private KpiValue toKpiValue(SbiKpiValue value, Date from, Date to) throws EMFUserError {

		logger.debug("IN");
		KpiValue toReturn = new KpiValue();

		Date beginDate = value.getBeginDt();
		logger.debug("SbiKpiValue begin date: "
				+ (beginDate != null ? beginDate.toString() : "Begin date null"));
		Date endDate = value.getEndDt();
		logger.debug("SbiKpiValue end date: "
				+ (endDate != null ? endDate.toString() : "End date null"));
		String val = value.getValue();
		logger.debug("SbiKpiValue value: "
				+ (val != null ? val : "Value null"));
		String valueDescr = value.getDescription();
		logger.debug("SbiKpiValue description: "
				+ (valueDescr != null ? valueDescr : "Value description null"));
		
		Integer kpiInstanceID = null;
		Double weight = null;
		Double target = null;
		String scaleCode = null;
		String scaleName = null;

		SbiResources res = value.getSbiResources();
		Resource r = null;
		IResourceDAO resDao=DAOFactory.getResourceDAO();
		if (res != null) {
			r = resDao.toResource(res);
			logger.debug("SbiKpiValue resource: "
					+ (r.getColumn_name() != null ? r.getColumn_name() : "resource name null"));
		}
		SbiOrgUnit sbiOrgUnit = value.getSbiOrgUnit();
		OrganizationalUnit orgUnit = null;
		if(sbiOrgUnit != null){
			orgUnit = DAOFactory.getOrganizationalUnitDAO().getOrganizationalUnit(sbiOrgUnit.getId());	
			logger.debug("SbiKpiValue ou: "
					+ (orgUnit.getName() != null ? orgUnit.getName()
							: "ou name null"));
		}
		kpiInstanceID = value.getSbiKpiInstance().getIdKpiInstance();
		logger.debug("SbiKpiValue kpiInstanceID: "
				+ (kpiInstanceID != null ? kpiInstanceID.toString() : "kpiInstanceID null"));
		SbiKpiInstance kpiInst = value.getSbiKpiInstance();

		List thresholdValues = new ArrayList();
		Date kpiInstBegDt = kpiInst.getBeginDt();
		logger.debug("kpiInstBegDt begin date: "
				+ (kpiInstBegDt != null ? kpiInstBegDt.toString() : "Begin date null"));
		
		// TODO for the moment get actual values of weight/target etc check if it is correct
		weight = kpiInst.getWeight();
		logger.debug("SbiKpiValue weight: "
				+ (weight != null ? weight.toString() : "weight null"));
		target = kpiInst.getTarget();
		logger.debug("SbiKpiValue target: "
				+ (target != null ? target.toString() : "target null"));

		if (kpiInst.getSbiMeasureUnit() != null) {
			scaleCode = kpiInst.getSbiMeasureUnit().getScaleCd();
			logger.debug("SbiKpiValue scaleCode: "
					+ (scaleCode != null ? scaleCode : "scaleCode null"));
			scaleName = kpiInst.getSbiMeasureUnit().getScaleNm();
			logger.debug("SbiKpiValue scaleName: "
					+ (scaleName != null ? scaleName : "scaleName null"));
		}
		SbiThreshold t = kpiInst.getSbiThreshold();
		if(t!=null){

			Set ts = t.getSbiThresholdValues();
			Iterator i = ts.iterator();
			while (i.hasNext()) {
				SbiThresholdValue tls = (SbiThresholdValue) i.next();

				IThresholdValueDAO thDao=(IThresholdValueDAO)DAOFactory.getThresholdValueDAO();
				ThresholdValue tr = thDao.toThresholdValue(tls);
				thresholdValues.add(tr);
			}
		}			
		// TODO for the moment get actual values of weight/target etc check if it is correct
		
		toReturn.setValueDescr(valueDescr);
		logger.debug("Kpi value descritpion setted");
		toReturn.setTarget(target);
		logger.debug("Kpi value target setted");
		toReturn.setBeginDate(beginDate);
		logger.debug("Kpi value begin date setted");
		toReturn.setEndDate(endDate);
		logger.debug("Kpi value end date setted");
		toReturn.setValue(val);
		logger.debug("Kpi value setted");
		toReturn.setKpiInstanceId(kpiInstanceID);
		logger.debug("Kpi value Instance ID setted");
		toReturn.setWeight(weight);
		logger.debug("Kpi value weight setted");
		toReturn.setR(r);
		logger.debug("Kpi value resource setted");
		toReturn.setScaleCode(scaleCode);
		logger.debug("Kpi value scale Code setted");
		toReturn.setScaleName(scaleName);
		logger.debug("Kpi value scale Name setted");
		toReturn.setThresholdValues(thresholdValues);
		logger.debug("Kpi value Thresholds setted");
		toReturn.setKpiValueId(value.getIdKpiInstanceValue());
		logger.debug("Kpi value ID setted");
		toReturn.setValueXml(value.getXmlData());
		logger.debug("Kpi value XML setted");

		OrganizationalUnitGrantNode grantNode = new OrganizationalUnitGrantNode();
		OrganizationalUnitNode node = new OrganizationalUnitNode();
		if(value.getSbiOrgUnit() != null){
			OrganizationalUnit ou = DAOFactory.getOrganizationalUnitDAO().getOrganizationalUnit(value.getSbiOrgUnit().getId());
			node.setOu(ou);
		}
		if(value.getSbiOrgUnitHierarchies() != null){
			OrganizationalUnitHierarchy hierarchy = DAOFactory.getOrganizationalUnitDAO().getHierarchy(value.getSbiOrgUnitHierarchies().getId());
			node.setHierarchy(hierarchy);
		}
		if(value.getSbiOrgUnit() != null && value.getSbiOrgUnitHierarchies() != null){
			grantNode.setOuNode(node);
			toReturn.setGrantNodeOU(grantNode);
		}
		logger.debug("Kpi value orgnaizational unit grant node setted");

		logger.debug("OUT");
		return toReturn;
	}

	public void deleteKpiValueFromInterval(Integer kpiInstanceId, Date from,
			Date to, Resource r, OrganizationalUnitGrantNode grantNode) throws EMFUserError {
		logger.debug("IN");
		KpiValue toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiKpiValue.class);
			finder.add(Expression.eq("sbiKpiInstance.idKpiInstance",
					kpiInstanceId));
			finder.add(Expression.eq("beginDt", from));
			finder.add(Expression.eq("endDt", to));
			finder.addOrder(Order.desc("beginDt"));
			finder.addOrder(Order.desc("idKpiInstanceValue"));
			logger.debug("Order Date Criteria setted");
			finder.setMaxResults(1);
			logger.debug("Max result to 1 setted");

			if (r != null) {
				finder.add(Expression.eq("sbiResources.resourceId", r.getId()));
			}
			if (grantNode != null) {
				finder.add(Expression.eq("sbiOrgUnit.id", grantNode.getOuNode().getOu().getId()));
				finder.add(Expression.eq("sbiOrgUnitHierarchies.id", grantNode.getOuNode().getHierarchy().getId()));
				if(grantNode.getOuNode().getHierarchy().getCompany() != null){
					finder.add(Expression.eq("company", grantNode.getOuNode().getHierarchy().getCompany()));
				}
			}
			List l = finder.list();
			if (!l.isEmpty()) {
				KpiValue tem = null;
				Iterator it = l.iterator();
				while (it.hasNext()) {					
					SbiKpiValue temp = (SbiKpiValue) it.next();
					aSession.delete(temp);
				}
			}
			tx.commit();

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10108);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

		logger.debug("OUT");
		
	}

	public List loadKpiListFiltered(String hsql,Integer offset, Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;

			String hql = "select count(*) "+hsql;
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}

			hibernateQuery = aSession.createQuery(hsql);
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toTransform = hibernateQuery.list();			

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiKpi hibKpi = (SbiKpi) iterator.next();
				Kpi kpi = toKpi(hibKpi);
				toReturn.add(kpi);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Threshold", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public KpiValue getDisplayKpiValue(Integer kpiInstanceId, Date d,
			Resource r, OrganizationalUnitGrantNode grantNode)
			throws EMFUserError {
		logger.debug("IN");
		KpiValue toReturn = null;
		Session aSession = null;
		Transaction tx = null;


		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiKpiValue.class);
			finder.add(Expression.eq("sbiKpiInstance.idKpiInstance",
					kpiInstanceId));
			finder.add(Expression.le("beginDt", d));
			finder.add(Expression.ge("endDt", d));
			finder.addOrder(Order.desc("beginDt"));
			finder.addOrder(Order.desc("idKpiInstanceValue"));
			logger.debug("Order Date Criteria setted");
			finder.setMaxResults(1);
			logger.debug("Max result to 1 setted");

			if (r != null) {
				finder.add(Expression.eq("sbiResources.resourceId", r.getId()));
			}
			if (grantNode != null) {
				Integer hierarchyId = grantNode.getOuNode().getHierarchy().getId();
				Integer ouId = grantNode.getOuNode().getOu().getId();
				finder.add(Expression.eq("sbiOrgUnit.id", ouId));
				finder.add(Expression.eq("sbiOrgUnitHierarchies.id", hierarchyId));
				if(grantNode.getOuNode().getHierarchy().getCompany() != null){
					finder.add(Expression.eq("company", grantNode.getOuNode().getHierarchy().getCompany()));
				}
			}
/*			if (company != null) {
				finder.add(Expression.eq("company", company));
			}*/
			List l = finder.list();
			if (!l.isEmpty()) {
				KpiValue tem = null;
				Iterator it = l.iterator();
				while (it.hasNext()) {
					SbiKpiValue temp = (SbiKpiValue) it.next();
					toReturn = toKpiValue(temp, d);
				}
			}else{
				Criteria finder2 = aSession.createCriteria(SbiKpiValue.class);
				finder2.add(Expression.eq("sbiKpiInstance.idKpiInstance",
						kpiInstanceId));
				finder2.add(Expression.le("beginDt", d));
				finder2.addOrder(Order.desc("beginDt"));
				logger.debug("Order Date Criteria setted");
				finder2.setMaxResults(1);
				logger.debug("Max result to 1 setted");

				if (r != null) {
					finder2.add(Expression.eq("sbiResources.resourceId", r.getId()));
				}

				List l2 = finder2.list();
				if (!l2.isEmpty()) {
					KpiValue tem = null;
					Iterator it = l2.iterator();
					while (it.hasNext()) {
						SbiKpiValue temp = (SbiKpiValue) it.next();
						toReturn = toKpiValue(temp, d);
					}
				}

			}

		} catch (HibernateException he) {

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10108);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
}
