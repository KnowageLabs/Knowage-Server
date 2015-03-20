/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi;

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.DomainDAOHibImpl;
import it.eng.spagobi.kpi.config.bo.Kpi;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.bo.KpiRel;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.config.dao.IKpiDAO;
import it.eng.spagobi.kpi.config.dao.IKpiErrorDAO;
import it.eng.spagobi.kpi.config.dao.KpiDAOImpl;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.exceptions.DatasetException;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class KpiValueComputation {

	private KpiParametrization parameters;
	private SpagoBIKpiInternalEngine engine;

	static transient Logger logger = Logger.getLogger(KpiValueComputation.class);

	public KpiParametrization getParameters() {
		return parameters;
	}

	public void setParameters(KpiParametrization parameters) {
		this.parameters = parameters;
	}

	public KpiValueComputation(SpagoBIKpiInternalEngine engine) {
		this.parameters = engine.parameters;
		this.engine = engine;
	}

	public KpiValue getNewKpiValue(IDataSet dataSet, KpiInstance kpiInst, Resource r, Integer modelInstanceId, OrganizationalUnitGrantNode grantNode)
			throws EMFUserError, EMFInternalError, SourceBeanException {

		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiValueComputation.getNewKpiValue");
		Integer kpiInstanceID = kpiInst.getKpiInstanceId();
		Date kpiInstBegDt = kpiInst.getD();

		KpiValue kVal = new KpiValue();
		kVal = setTimeAttributes(kVal, kpiInst);
		kVal.setKpiInstanceId(kpiInstanceID);
		logger.debug("Setted the KpiValue Instance ID:" + kpiInstanceID);
		logger.debug("kpiInstBegDt begin date: " + (kpiInstBegDt != null ? kpiInstBegDt.toString() : "Begin date null"));

		if (grantNode != null) {
			kVal.setGrantNodeOU(grantNode);
			logger.debug("Setted the OU label :" + grantNode.getOuNode().getOu().getLabel());
			logger.debug("Setted the hierarchy label :" + grantNode.getOuNode().getHierarchy().getLabel());
		}
		if ((this.parameters.getDateOfKPI().after(kpiInstBegDt) || this.parameters.getDateOfKPI().equals(kpiInstBegDt))) {
			// kpiInstance doesn't change
		} else {
			KpiInstance tempKIn = DAOFactory.getKpiInstanceDAO().loadKpiInstanceByIdFromHistory(kpiInstanceID, this.parameters.getDateOfKPI());
			if (tempKIn == null) {// kpiInstance doesn't change
			} else {
				// in case older thresholds have to be retrieved
				kpiInst = tempKIn;
			}
		}
		Kpi kpi = DAOFactory.getKpiDAO().loadKpiById(kpiInst.getKpi());

		kVal = getFromKpiInstAndSetKpiValueAttributes(kpiInst, kVal, kpi);

		// If it has to be calculated for a Resource. The resource will be set
		// as parameter
		HashMap temp = (HashMap) this.parameters.getParametersObject().clone();
		if (r != null) {
			String colName = r.getColumn_name();
			String value = r.getName();
			String code = r.getCode();
			kVal.setR(r);
			logger.info("Setted the Resource:" + r.getName());
			temp.put("ParKpiResource", value);
			temp.put("ParKpiResourceCode", code);
		}
		// cast Integer Ids to String
		temp.put("ParModelInstance", modelInstanceId.toString());
		temp.put("ParKpiInstance", kpiInstanceID.toString());

		// If not, the dataset will be calculated without the parameter Resource
		// and the DataSet won't expect a parameter of type resource
		// if(dataSet.hasBehaviour( QuerableBehaviour.class.getName()) ) {
		if (dataSet != null) {

			if (this.parameters.getBehaviour().equalsIgnoreCase("timeIntervalDefault")
					|| this.parameters.getBehaviour().equalsIgnoreCase("timeIntervalForceRecalculation")) {
				if (this.parameters.getDateIntervalFrom() != null && this.parameters.getDateIntervalTo() != null) {
					kVal.setBeginDate(this.parameters.getDateIntervalFrom());
					kVal.setEndDate(this.parameters.getDateIntervalTo());
				}
			}
			kVal = recursiveGetKpiValueFromKpiRel(kpi, dataSet, temp, kVal, this.parameters.getDateOfKPI(), kVal.getEndDate(), modelInstanceId);
			kVal = getKpiValueFromDataset(dataSet, temp, kVal, this.parameters.getDateOfKPI(), kVal.getEndDate(), true, modelInstanceId);
		}
		monitor.stop();
		logger.debug("OUT");
		return kVal;
	}

	protected KpiValue setTimeAttributes(KpiValue kVal, KpiInstance kpiInst) throws EMFUserError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiValueComputation.setTimeAttributes");

		Date begD = this.parameters.getDateOfKPI();
		Date endDate = null;
		logger.debug("behaviour: -" + this.parameters.getBehaviour() + "-");
		kVal.setBeginDate(begD);
		logger.debug("Setted the KpiValue begin Date:" + begD);
		if (parameters.getEndKpiValueDate() != null) {
			endDate = parameters.getEndKpiValueDate();
			kVal.setEndDate(parameters.getEndKpiValueDate());
		} else if (kpiInst.getPeriodicityId() != null) {
			Integer seconds = null;
			if (engine.periodInstID != null) {
				kpiInst.setPeriodicityId(engine.periodInstID);
				logger.debug("Setted new Periodicity ID:" + engine.periodInstID.toString());
			}
			seconds = DAOFactory.getPeriodicityDAO().getPeriodicitySeconds(kpiInst.getPeriodicityId());
			// Transforms seconds into milliseconds
			long milliSeconds = seconds.longValue() * 1000;
			long begDtTime = begD.getTime();
			long endTime = begDtTime + milliSeconds;
			endDate = new Date(endTime);
			kVal.setEndDate(endDate);
		} else {
			GregorianCalendar c = new GregorianCalendar();
			c.set(9999, 11, 31);
			endDate = c.getTime();
			kVal.setEndDate(endDate);
		}
		logger.debug("Setted the KpiValue end Date:" + endDate);
		monitor.stop();
		logger.debug("OUT");
		return kVal;
	}

	protected KpiValue getFromKpiInstAndSetKpiValueAttributes(KpiInstance kpiInst, KpiValue kVal, Kpi kpi) throws EMFUserError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiValueComputation.getFromKpiInstAndSetKpiValueAttributes");

		Double weight = null;
		Double target = null;
		String scaleCode = null;
		String scaleName = null;
		String measureTypeCd = null;
		String measureTypeName = null;
		List thresholdValues = null;
		String chartType = null;
		if (kpiInst != null) {
			Integer thresholdId = kpiInst.getThresholdId();
			if (thresholdId != null) {
				thresholdValues = DAOFactory.getThresholdValueDAO().loadThresholdValuesByThresholdId(thresholdId);
			}
			chartType = "BulletGraph";
			logger.debug("Requested date d: " + parameters.getDateOfKPI().toString() + " in between beginDate and EndDate");
			weight = kpiInst.getWeight();
			logger.debug("SbiKpiValue weight: " + (weight != null ? weight.toString() : "weight null"));
			target = kpiInst.getTarget();

			// scale type is defined on kpi not on kpiInstance
			scaleCode = kpi.getMetricScaleCd();
			logger.debug("SbiKpiValue scaleCode: " + (scaleCode != null ? scaleCode : "scaleCode null"));
			Integer scaleId = kpi.getMetricScaleId();
			DomainDAOHibImpl daoDomain = (DomainDAOHibImpl) DAOFactory.getDomainDAO();
			if (scaleId != null) {
				Domain scale = daoDomain.loadDomainById(scaleId);
				scaleName = scale.getValueName();

				measureTypeCd = kpi.getMeasureTypeCd();
				logger.debug("SbiKpiValue scaleName: " + (scaleName != null ? scaleName : "scaleName null"));
			}
		}
		if (kVal != null) {
			kVal.setWeight(weight);
			logger.debug("Setted the KpiValue weight:" + weight);
			kVal.setThresholdValues(thresholdValues);
			logger.debug("Setted the KpiValue thresholds");
			kVal.setScaleCode(scaleCode);
			logger.debug("Kpi value scale Code setted");
			kVal.setScaleName(scaleName);
			logger.debug("Kpi value scale Name setted");
			kVal.setTarget(target);
			logger.debug("Kpi value target setted");
			if (chartType != null)
				kVal.setChartType(chartType);
			logger.debug("OUT");
		}
		monitor.stop();
		return kVal;
	}

	public KpiValue recursiveGetKpiValueFromKpiRel(Kpi kpiParent, IDataSet dataSet, HashMap pars, KpiValue kVal, Date begD, Date endDate, Integer modInstNodeId)
			throws EMFUserError, EMFInternalError, SourceBeanException {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiValueComputation.recursiveGetKpiValueFromKpiRel");

		List<KpiRel> relations = DAOFactory.getKpiDAO().loadKpiRelListByParentId(kpiParent.getKpiId());
		logger.info("extracts relations for kpi parent : " + kpiParent.getKpiName());
		KpiDAOImpl kpiDao = (KpiDAOImpl) DAOFactory.getKpiDAO();
		OrganizationalUnitGrantNode ouGrant = kVal.getGrantNodeOU();
		String ouLabel = "";
		if (ouGrant != null) {
			ouLabel = ouGrant.getOuNode().getOu().getLabel();
		}
		logger.debug("kpi inst id= " + kVal.getKpiInstanceId() + " parent kpi is " + kpiParent.getKpiName() + " and OU :" + ouLabel);
		for (int i = 0; i < relations.size(); i++) {
			KpiRel rel = relations.get(i);
			Kpi child = rel.getKpiChild();
			IDataSet chDataSet = kpiDao.getDsFromKpiId(child.getKpiId());
			HashMap chPars = new HashMap();
			chPars.putAll(pars);
			// then the one in rel table
			String parameter = rel.getParameter();
			KpiValue kpiVal = recursiveGetKpiValueFromKpiRel(child, chDataSet, chPars, kVal, begD, endDate, modInstNodeId);
			pars.put(parameter, kpiVal.getValue());

		}
		// checks if it is to recalculate
		// calculate with dataset
		KpiValue value = getKpiValueFromDataset(dataSet, pars, kVal, begD, endDate, false, modInstNodeId);
		logger.debug("gets value from dataset : " + value.getValue());
		monitor.stop();
		logger.debug("OUT");
		return value;
	}

	public KpiValue getKpiValueFromDataset(IDataSet dataSet, HashMap pars, KpiValue kVal, Date begD, Date endDate, boolean doSave, Integer modInstNodeId)
			throws EMFInternalError, SourceBeanException, EMFUserError, DatasetException {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.KpiValueComputation.getKpiValueFromDataset");

		String dsName = dataSet.getName();
		logger.debug("Elaborating dataset: " + dsName);
		KpiValue kpiValTemp = null;

		dataSet.setParamsMap(pars);
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(engine.data.getProfile()));

		logger.info("Load Data Set. Label=" + dataSet.getLabel());

		// Handle in table SbiKpiError dataset Error
		try {
			dataSet.loadData();
			logger.debug("loaded dataset: " + dsName);
		} catch (RuntimeException e) {
			// Exception must be handled and recorded in table SbiKpiError, if
			// it is a datasetexception
			logger.error("Runtime error occured elaborating dataset: " + dsName, e);
			if (e instanceof DatasetException) {
				logger.error("DatasetException on dataset: " + dsName, e);
				IKpiErrorDAO dao = DAOFactory.getKpiErrorDAO();
				dao.setUserProfile(engine.data.getProfile());
				dao.insertKpiError((DatasetException) e, modInstNodeId, kVal.getR() != null ? kVal.getR().getName() : null);
			} else {
				logger.error("Exception not handled by table KpiError on dataset " + dsName, e);
			}
			kVal.setValue(null);
			return kVal;
		}

		IDataStore dataStore = dataSet.getDataStore();
		logger.debug("Got the datastore for " + dsName);

		if (dataStore != null && !dataStore.isEmpty()) {
			logger.debug("Datastore for " + dsName + " is not empty");
			// Transform result into KPIValue (I suppose that the result has a
			// unique value)
			IMetaData d = dataStore.getMetaData();
			int indexRes = d.getFieldIndex(engine.RESOURCE);

			if (indexRes != -1) {
				Iterator it = dataStore.iterator();
				while (it.hasNext()) {

					kpiValTemp = kVal.clone();
					IRecord record = (IRecord) it.next();
					List fields = record.getFields();
					kpiValTemp = engine.setKpiValuesFromDataset(kpiValTemp, fields, d, begD, endDate, dataSet.getLabel(), modInstNodeId, kVal);

					if (kpiValTemp.getR() != null && kVal.getR() != null && kpiValTemp.getR().getId() != null && kVal.getR().getId() != null
							&& kpiValTemp.getR().getId().equals(kVal.getR().getId())) {
						kVal = kpiValTemp.clone();
					}
					logger.debug("New value calculated");
					if (engine.templateConfiguration.isRegister_values() && kpiValTemp.getR().getName() != null) {

						if (doSave) {
							// Insert new Value into the DB
							IKpiDAO dao = DAOFactory.getKpiDAO();
							dao.setUserProfile(engine.data.getProfile());
							Integer kpiValueId = dao.insertKpiValue(kpiValTemp);
							kVal.setKpiValueId(kpiValueId);
							logger.info("New value inserted in the DB. Resource=" + kpiValTemp.getR().getName() + " KpiInstanceId="
									+ kpiValTemp.getKpiInstanceId());
							// Checks if the value is alarming (out of a certain range)
							// If the value is alarming a new line will be inserted
							// in the sbi_alarm_event table and scheduled to be sent
							DAOFactory.getAlarmDAO().isAlarmingValue(kpiValTemp);
							logger.debug("Alarms sent if the value is over the thresholds");
						}

					}

				}
			} else {

				IRecord record = dataStore.getRecordAt(0);
				List fields = record.getFields();
				kVal = engine.setKpiValuesFromDataset(kVal, fields, d, begD, endDate, dataSet.getLabel(), modInstNodeId, kVal);
				logger.debug("New value calculated");
				if (engine.templateConfiguration.isRegister_values()) {
					if (doSave) {
						logger.debug("Kpi value is equal to: " + kVal.getValue());
						logger.debug("Kpi value descr is equal to: " + kVal.getValueDescr());
						logger.debug("Kpi value xml is equal to: " + kVal.getValueXml());
						// Insert new Value into the DB
						Integer kpiValueId = DAOFactory.getKpiDAO().insertKpiValue(kVal);
						kVal.setKpiValueId(kpiValueId);
						logger.debug("New value inserted in the DB with id [" + kpiValueId + "]");
						// Checks if the value is alarming (out of a certain range)
						// If the value is alarming a new line will be inserted in the
						// sbi_alarm_event table and scheduled to be sent
						DAOFactory.getAlarmDAO().isAlarmingValue(kVal);
						logger.debug("Alarms sent if the value is over the thresholds");
					}
				}

			}
		} else {
			logger.warn("The Data Set " + dsName + " doesn't return any value!!!!!");
			if (engine.templateConfiguration.isRegister_values()) {
				if (doSave) {
					// Insert new Value into the DB
					Integer kpiValueId = DAOFactory.getKpiDAO().insertKpiValue(kVal);
					kVal.setKpiValueId(kpiValueId);
					logger.debug("New value inserted in the DB");
					DAOFactory.getAlarmDAO().isAlarmingValue(kVal);
					logger.debug("Alarms sent if the value is over the thresholds");
				}
			}
		}
		monitor.stop();
		logger.debug("OUT");

		return kVal;
	}

}
