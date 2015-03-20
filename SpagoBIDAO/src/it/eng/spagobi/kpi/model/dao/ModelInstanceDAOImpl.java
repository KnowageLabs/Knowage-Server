/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.kpi.config.bo.KpiInstPeriod;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.dao.IKpiInstanceDAO;
import it.eng.spagobi.kpi.config.metadata.SbiKpi;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstPeriod;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstanceHistory;
import it.eng.spagobi.kpi.config.metadata.SbiKpiPeriodicity;
import it.eng.spagobi.kpi.config.metadata.SbiKpiValue;
import it.eng.spagobi.kpi.model.bo.Model;
import it.eng.spagobi.kpi.model.bo.ModelInstance;
import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModel;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;

public class ModelInstanceDAOImpl extends AbstractHibernateDAO implements
IModelInstanceDAO {

	static private Logger logger = Logger.getLogger(ModelInstanceDAOImpl.class);

	public static ModelInstanceNode toModelInstanceNode(
			SbiKpiModelInst hibSbiKpiModelInst)
	throws EMFUserError {

		logger.debug("IN");
		ModelInstanceNode toReturn = new ModelInstanceNode();

		String modelCode = "";

		if (hibSbiKpiModelInst.getSbiKpiModel() != null) {
			modelCode = hibSbiKpiModelInst.getSbiKpiModel().getKpiModelCd();
		}

		Integer id = hibSbiKpiModelInst.getKpiModelInst();
		logger.debug("SbiKpiModelInstanceNode id: "
				+ (id != null ? id : "id null"));
		String descr = hibSbiKpiModelInst.getDescription();
		logger.debug("SbiKpiModelInstanceNode description: "
				+ (descr != null ? descr : "Description null"));
		String name = hibSbiKpiModelInst.getName();
		logger.debug("SbiKpiModelInstanceNode name: "
				+ (name != null ? name : "name null"));
		SbiKpiInstance kpiInst = hibSbiKpiModelInst.getSbiKpiInstance();

		IKpiInstanceDAO kpiInstDAO = DAOFactory.getKpiInstanceDAO();
		KpiInstance kpiInstanceAssociated = null;
		if (kpiInst != null) {
			kpiInstanceAssociated = kpiInstDAO.toKpiInstance(kpiInst);
		}

		Set resources = hibSbiKpiModelInst.getSbiKpiModelResourceses();
		List res = new ArrayList();
		if (!resources.isEmpty()) {
			Iterator i = resources.iterator();
			IModelResourceDAO resDAO = DAOFactory.getModelResourcesDAO();
			while (i.hasNext()) {
				SbiKpiModelResources dls = (SbiKpiModelResources) i.next();
				Resource r = resDAO.toResource(dls);
				logger.debug("SbiKpiModelInstanceNode resource name: "
						+ (r.getName() != null ? r.getName()
								: "Resource name null"));
				res.add(r);
			}
		}
		// gets father id
		SbiKpiModelInst father = hibSbiKpiModelInst.getSbiKpiModelInst();
		Integer fatherId = null;
		Boolean isRoot = false;
		if (father != null) {
			fatherId = father.getKpiModelInst();
			logger
			.debug("SbiKpiModelInstanceNode fatherId: "
					+ (fatherId != null ? fatherId.toString()
							: "fatherId null"));
		} else {
			isRoot = true;
		}

		// gets list of children id
		Set children = hibSbiKpiModelInst.getSbiKpiModelInsts();
		List childrenIds = new ArrayList();
		Iterator iCI = children.iterator();
		logger.debug("Started list of children");
		while (iCI.hasNext()) {
			SbiKpiModelInst skml = (SbiKpiModelInst) iCI.next();
			Integer childId = skml.getKpiModelInst();
			logger.debug("SbiKpiModelInstanceNode childrenId: "
					+ (childId != null ? childId.toString() : "childId null"));
			childrenIds.add(childId);
		}

		// gets ModelNode referenced
		Integer reference = hibSbiKpiModelInst.getSbiKpiModel().getKpiModelId();
		logger
		.debug("SbiKpiModelInstanceNode modelNodeReference: "
				+ (reference != null ? reference.toString()
						: "reference null"));

		toReturn.setModelInstanceNodeId(id);
		logger.debug("KpiModelInstanceNode id setted");
		toReturn.setDescr(descr);
		logger.debug("KpiModelInstanceNode description setted");
		toReturn.setName(name);
		logger.debug("KpiModelInstanceNode name setted");
		toReturn.setKpiInstanceAssociated(kpiInstanceAssociated);
		logger.debug("KpiModelInstanceNode kpiInstanceAssociated setted");
		toReturn.setResources(res);
		logger.debug("KpiModelInstanceNode resources setted");
		toReturn.setFatherId(fatherId);
		logger.debug("KpiModelInstanceNode fatherId setted");
		toReturn.setModelReference(reference);
		logger.debug("KpiModelInstanceNode ModelNode reference setted");
		toReturn.setIsRoot(isRoot);
		logger.debug("KpiModelInstanceNode isRoot setted");
		toReturn.setChildrenIds(childrenIds);
		logger.debug("KpiModelInstanceNode childrenIds setted");
		toReturn.setModelCode(modelCode);
		logger.debug("KpiModelInstanceNode childrenIds setted");

		toReturn.setModelInstaceReferenceLabel(hibSbiKpiModelInst
				.getModelUUID());
		logger.debug("OUT");
		return toReturn;
	}

	public ModelInstanceNode loadModelInstanceById(Integer id,
			Date requestedDate) throws EMFUserError {
		logger.debug("IN");
		ModelInstanceNode toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiModelInst hibSbiKpiModelInst = (SbiKpiModelInst) aSession
			.get(SbiKpiModelInst.class, id);
			toReturn = toModelInstanceNode(hibSbiKpiModelInst);

		} catch (HibernateException he) {
			logger.error("Error while loading the Model Instance with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

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

	public ModelInstanceNode loadModelInstanceByLabel(String label,
			Date requestedDate) throws EMFUserError {
		logger.debug("IN");
		ModelInstanceNode toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion nameCriterrion = Expression.eq("label", label);
			Criteria criteria = aSession.createCriteria(SbiKpiModelInst.class);
			criteria.add(nameCriterrion);
			SbiKpiModelInst hibSbiKpiModelInst = (SbiKpiModelInst) criteria
			.uniqueResult();
			if (hibSbiKpiModelInst == null)
				return null;
			toReturn = toModelInstanceNode(hibSbiKpiModelInst);

		} catch (HibernateException he) {
			logger.error("Error while loading the Model Instance with name "
					+ ((label == null) ? "null" : label), he);

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

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

	public List loadModelsInstanceRoot() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria crit = aSession.createCriteria(SbiKpiModelInst.class);
			crit.add(Expression.isNull("sbiKpiModelInst"));
			List sbiKpiModelInstanceList = crit.list();
			for (Iterator iterator = sbiKpiModelInstanceList.iterator(); iterator
			.hasNext();) {
				SbiKpiModelInst sbiKpiModelInst = (SbiKpiModelInst) iterator
				.next();
				ModelInstance aModelInst = toModelInstanceWithoutChildren(
						sbiKpiModelInst, aSession);
				toReturn.add(aModelInst);
			}
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
		return toReturn;

	}

	public static ModelInstance toModelInstanceWithoutChildren(SbiKpiModelInst value,
			Session aSession) {
		logger.debug("IN");
		ModelInstance toReturn = new ModelInstance();

		String name = value.getName();
		String description = value.getDescription();
		String label = value.getLabel();
		Date startDate = value.getStartDate();
		Date endDate = value.getEndDate();
		Integer id = value.getKpiModelInst();
		SbiKpiModel sbiKpiModel = value.getSbiKpiModel();
		String modelUUID = value.getModelUUID();

		// insert Parent
		if (value.getSbiKpiModelInst() != null) {
			toReturn.setParentId(value.getSbiKpiModelInst().getKpiModelInst());
		}

		// load with Dao to get also domains
		try {
			SbiKpiModel sbiKpiModel2 = null;
			IModelDAO modelDao = (IModelDAO) DAOFactory.getModelDAO();
			Integer modelId = sbiKpiModel.getKpiModelId();
			// sbiKpiModel2=modelDao.l(modelId);

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Model aModel = ModelDAOImpl.toModelWithoutChildren(sbiKpiModel,
				aSession);
		SbiKpiInstance sbiKpiInstance = value.getSbiKpiInstance();

		if (sbiKpiInstance != null) {
			// toKpiInstance
			KpiInstance aKpiInstance = new KpiInstance();
			aKpiInstance.setKpiInstanceId(sbiKpiInstance.getIdKpiInstance());
			aKpiInstance.setKpi(sbiKpiInstance.getSbiKpi().getKpiId());
			if (sbiKpiInstance.getSbiThreshold() != null) {
				aKpiInstance.setThresholdId(sbiKpiInstance.getSbiThreshold()
						.getThresholdId());
			}
			if (sbiKpiInstance.getChartType() != null) {
				aKpiInstance.setChartTypeId(sbiKpiInstance.getChartType()
						.getValueId());
			}
			// TODO
			if (sbiKpiInstance.getSbiKpiInstPeriods() != null
					&& !(sbiKpiInstance.getSbiKpiInstPeriods().isEmpty())) {
				SbiKpiInstPeriod instPeriod = (SbiKpiInstPeriod) sbiKpiInstance
				.getSbiKpiInstPeriods().toArray()[0];

				aKpiInstance.setPeriodicityId(instPeriod.getSbiKpiPeriodicity()
						.getIdKpiPeriodicity());
			} //
			aKpiInstance.setWeight(sbiKpiInstance.getWeight());
			aKpiInstance.setTarget(sbiKpiInstance.getTarget());
			aKpiInstance.setD(sbiKpiInstance.getBeginDt());
			//
			toReturn.setKpiInstance(aKpiInstance);
		}

		toReturn.setId(id);
		toReturn.setName(name);
		toReturn.setDescription(description);
		toReturn.setLabel(label);
		toReturn.setStartDate(startDate);
		toReturn.setEndDate(endDate);
		toReturn.setModel(aModel);
		toReturn.setModelUUID(modelUUID);

		logger.debug("OUT");
		return toReturn;
	}

	public ModelInstance loadModelInstanceWithoutChildrenById(Integer id)
	throws EMFUserError {
		logger.debug("IN");
		ModelInstance toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiModelInst hibSbiKpiModelInstance = (SbiKpiModelInst) aSession
			.load(SbiKpiModelInst.class, id);
			toReturn = toModelInstanceWithoutChildren(hibSbiKpiModelInstance,
					aSession);

		} catch (HibernateException he) {
			logger.error("Error while loading the Model Instance with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

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

	public ModelInstance loadModelInstanceWithoutChildrenByLabel(String label)
	throws EMFUserError {
		logger.debug("IN");
		ModelInstance toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion nameCriterrion = Expression.eq("label", label);
			Criteria criteria = aSession.createCriteria(SbiKpiModelInst.class);
			criteria.add(nameCriterrion);
			SbiKpiModelInst hibSbiKpiModelInst = (SbiKpiModelInst) criteria
			.uniqueResult();
			if (hibSbiKpiModelInst == null)
				return null;
			toReturn = toModelInstanceWithoutChildren(hibSbiKpiModelInst,
					aSession);

		} catch (HibernateException he) {
			logger.error("Error while loading the Model Instance with label "
					+ ((label == null) ? "null" : label), he);

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

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

	// ModelInstance toReturn = null;
	// Session aSession = null;
	// Transaction tx = null;
	// try {
	// aSession = getSession();
	// tx = aSession.beginTransaction();
	// SbiKpiModelInst hibSbiKpiModelInstance = (SbiKpiModelInst) aSession
	// .load(SbiKpiModelInst.class, label);
	// toReturn = toModelInstanceWithoutChildren(hibSbiKpiModelInstance,
	// aSession);

	// } catch (HibernateException he) {
	// logger.error("Error while loading the Model Instance with label "
	// + ((label == null) ? "" : label.toString()), he);

	// if (tx != null)
	// tx.rollback();

	// throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

	// } finally {
	// if (aSession != null) {
	// if (aSession.isOpen())
	// aSession.close();
	// logger.debug("OUT");
	// }
	// }
	// logger.debug("OUT");
	// return toReturn;
	// }

	public void modifyModelInstance(ModelInstance value) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer kpiModelInstanceId = value.getId();
			String kpiModelInstanceDesc = value.getDescription();
			String kpiModelInstanceNm = value.getName();
			String kpiModelInstanceLb = value.getLabel();
			Date kpiModelInstanceStartDate = value.getStartDate();
			Date kpiModelInDateEndDate = value.getEndDate();
			String modelUUID = value.getModelUUID();

			SbiKpiModelInst sbiKpiModelInst = (SbiKpiModelInst) aSession.load(
					SbiKpiModelInst.class, kpiModelInstanceId);
			
			sbiKpiModelInst.setDescription(kpiModelInstanceDesc);
			sbiKpiModelInst.setName(kpiModelInstanceNm);
			sbiKpiModelInst.setLabel(kpiModelInstanceLb);
			sbiKpiModelInst.setStartDate(kpiModelInstanceStartDate);
			sbiKpiModelInst.setEndDate(kpiModelInDateEndDate);
			sbiKpiModelInst.setModelUUID(modelUUID);

			SbiKpiInstance oldSbiKpiInstance = sbiKpiModelInst
			.getSbiKpiInstance();

			//default behaviour
			boolean newKpiInstanceHistory = true;
			boolean deleteOldHistory = false;
			boolean dontSaveKpiHistory = false;
			if(value.getKpiInstance() != null && !value.getKpiInstance().isSaveKpiHistory()){
				dontSaveKpiHistory = true;
			}

			// new kpiInstance is null
			if (value.getKpiInstance() == null) {
				newKpiInstanceHistory = false;
				deleteOldHistory = true;
			}

			if (value.getKpiInstance() != null
					&& (!value.getKpiInstance().isSaveKpiHistory())) {
				newKpiInstanceHistory = false;
				dontSaveKpiHistory = true;
			}

			// old kpiInstance is null and new kpiInstance has a value
			if (oldSbiKpiInstance == null && value.getKpiInstance() != null) {
				newKpiInstanceHistory = false;
			}

			// old kpiId is different from new kpiId
			if (newKpiInstanceHistory
					&& !(areBothNull(oldSbiKpiInstance.getSbiKpi(), value
							.getKpiInstance().getKpi()) || (oldSbiKpiInstance
									.getSbiKpi() != null && areNullOrEquals(
											oldSbiKpiInstance.getSbiKpi().getKpiId(), value
											.getKpiInstance().getKpi())))) {
				newKpiInstanceHistory = false;
				deleteOldHistory = true;
				// create new sbiKpiInstance
			}

			// check if same value is changed
			if (newKpiInstanceHistory
					&& !((areBothNull(oldSbiKpiInstance.getSbiThreshold(),
							value.getKpiInstance().getThresholdId()) || (oldSbiKpiInstance
									.getSbiThreshold() != null && areNullOrEquals(
											oldSbiKpiInstance.getSbiThreshold()
											.getThresholdId(), value.getKpiInstance()
											.getThresholdId())))
											&& (areBothNull(oldSbiKpiInstance.getChartType(),
													value.getKpiInstance().getChartTypeId()) || (oldSbiKpiInstance
															.getChartType() != null && areNullOrEquals(
																	oldSbiKpiInstance.getChartType()
																	.getValueId(), value
																	.getKpiInstance().getChartTypeId())))
																	/*
																	 * TODO && (areBothNull(oldSbiKpiInstance
																	 * .getSbiKpiPeriodicity(), value
																	 * .getKpiInstance().getPeriodicityId()) ||
																	 * (oldSbiKpiInstance .getSbiKpiPeriodicity() != null &&
																	 * areNullOrEquals( oldSbiKpiInstance.getSbiKpiPeriodicity()
																	 * .getIdKpiPeriodicity(), value .getKpiInstance()
																	 * .getPeriodicityId())))
																	 */
																	&& areNullOrEquals(oldSbiKpiInstance.getWeight(), value
																			.getKpiInstance().getWeight()))) {
				// create new History
				Calendar now = Calendar.getInstance();
				SbiKpiInstanceHistory sbiKpiInstanceHistory = new SbiKpiInstanceHistory();
				sbiKpiInstanceHistory.setSbiKpiInstance(oldSbiKpiInstance);
				sbiKpiInstanceHistory.setSbiThreshold(oldSbiKpiInstance
						.getSbiThreshold());
				sbiKpiInstanceHistory.setSbiDomains(oldSbiKpiInstance
						.getChartType());
				sbiKpiInstanceHistory.setWeight(oldSbiKpiInstance.getWeight());
				sbiKpiInstanceHistory
				.setBeginDt(oldSbiKpiInstance.getBeginDt());
				sbiKpiInstanceHistory.setEndDt(now.getTime());
				updateSbiCommonInfo4Insert(sbiKpiInstanceHistory);
				aSession.save(sbiKpiInstanceHistory);
			}

			SbiKpiInstance kpiInstanceToCreate = null;



			if (value.getKpiInstance() != null) {
				if (newKpiInstanceHistory || dontSaveKpiHistory) {
					kpiInstanceToCreate = setSbiKpiInstanceFromModelInstance(
							aSession, value, oldSbiKpiInstance);
				} else {
					// create new kpiInstance
					kpiInstanceToCreate = new SbiKpiInstance();
					Calendar now = Calendar.getInstance();
					kpiInstanceToCreate.setBeginDt(now.getTime());
					kpiInstanceToCreate = setSbiKpiInstanceFromModelInstance(
							aSession, value, kpiInstanceToCreate);
				}
				updateSbiCommonInfo4Update(kpiInstanceToCreate);
				aSession.saveOrUpdate(kpiInstanceToCreate);
				sbiKpiModelInst.setSbiKpiInstance(kpiInstanceToCreate);
			} else {
				sbiKpiModelInst.setSbiKpiInstance(null);
			}
			updateSbiCommonInfo4Update(sbiKpiModelInst);
			aSession.update(sbiKpiModelInst);

			//adds or updates periodicity
			setSbiKpiPeriodicity(aSession, value, kpiInstanceToCreate, oldSbiKpiInstance);

			if (deleteOldHistory && oldSbiKpiInstance != null) {
				deleteKpiInstance(aSession, oldSbiKpiInstance
						.getIdKpiInstance());
			}

			//DAOFactory.getUdpDAOValue().insertOrUpdateRelatedUdpValues(value, sbiKpiModelInst, aSession, "MODEL");
			
			tx.commit();

		}

		catch (org.hibernate.exception.ConstraintViolationException ce) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to modify a Model Instance Instance", ce);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 101);

		}

		catch (HibernateException he) {
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
	}

	private SbiKpiInstance setSbiKpiInstanceFromModelInstance(Session aSession,
			ModelInstance value, SbiKpiInstance sbiKpiInstance) {
		if (sbiKpiInstance == null) {
			sbiKpiInstance = new SbiKpiInstance();
		}
		if (value.getKpiInstance().getD() != null) {
			sbiKpiInstance.setBeginDt(value.getKpiInstance().getD());
		} else {
			sbiKpiInstance.setBeginDt(new Date());
		}

		if (value.getKpiInstance().getKpi() != null) {
			sbiKpiInstance.setSbiKpi((SbiKpi) aSession.load(SbiKpi.class, value
					.getKpiInstance().getKpi()));
		} else {
			sbiKpiInstance.setSbiKpi(null);
		}
		if (value.getKpiInstance().getThresholdId() != null) {
			sbiKpiInstance.setSbiThreshold((SbiThreshold) aSession
					.load(SbiThreshold.class, value.getKpiInstance()
							.getThresholdId()));
		} else {
			sbiKpiInstance.setSbiThreshold(null);
		}

		if (value.getKpiInstance().getChartTypeId() != null) {
			sbiKpiInstance.setChartType((SbiDomains) aSession.load(
					SbiDomains.class, value.getKpiInstance().getChartTypeId()));
		} else {
			sbiKpiInstance.setChartType(null);
		}


		sbiKpiInstance.setWeight(value.getKpiInstance().getWeight());
		sbiKpiInstance.setTarget((value.getKpiInstance().getTarget()));
		return sbiKpiInstance;
	}
	private SbiKpiInstance setSbiKpiPeriodicity(Session aSession,
			ModelInstance value, SbiKpiInstance sbiKpiInstance, SbiKpiInstance oldSbiKpiInstance) {
		if (sbiKpiInstance != null) {

			if (value.getKpiInstance().getPeriodicityId() != null) {
				// AGGIUNTA O AGGIORNAMENTO RIGA
				// TODO

				SbiKpiPeriodicity sbiKpiPeriodicity = (SbiKpiPeriodicity) aSession
				.load(SbiKpiPeriodicity.class, value.getKpiInstance()
						.getPeriodicityId());

				Criteria critt = aSession.createCriteria(SbiKpiInstPeriod.class);
				critt.add(Expression.eq("sbiKpiInstance", sbiKpiInstance));
				List instPeriodsList = critt.list();


				if (instPeriodsList == null || instPeriodsList.isEmpty()) {
					SbiKpiInstPeriod toInsert = new SbiKpiInstPeriod();
					toInsert.setSbiKpiInstance(sbiKpiInstance);
					toInsert.setSbiKpiPeriodicity(sbiKpiPeriodicity);
					toInsert.setDefault_(true);

					aSession.save(toInsert);

				} else {
					((SbiKpiInstPeriod) instPeriodsList.get(0))
					.setSbiKpiPeriodicity(sbiKpiPeriodicity);
					aSession.update(instPeriodsList.get(0));
				}

			} else {
				// RIMOZIONE DELLA RIGA DAL DB
				Set InstPeriods = sbiKpiInstance.getSbiKpiInstPeriods();
				for (Iterator iterator = InstPeriods.iterator(); iterator.hasNext();) {
					SbiKpiInstPeriod sbiKpiInstPeriod = (SbiKpiInstPeriod) iterator
					.next();
					aSession.delete(sbiKpiInstPeriod);
				}
				//
			}
		}else{
			if(oldSbiKpiInstance != null){
				//delete reference to old kpi instance
				Set InstPeriods = oldSbiKpiInstance.getSbiKpiInstPeriods();
				for (Iterator iterator = InstPeriods.iterator(); iterator.hasNext();) {
					SbiKpiInstPeriod sbiKpiInstPeriod = (SbiKpiInstPeriod) iterator
					.next();
					aSession.delete(sbiKpiInstPeriod);
				}
			}

		}
		return sbiKpiInstance;
	}
	private boolean areBothNull(Object a, Object b) {
		boolean toReturn = false;
		if (a == null && b == null)
			toReturn = true;
		return toReturn;
	}

	private boolean areNullOrEquals(Object a, Object b) {
		boolean toReturn = false;
		if (a == null && b == null)
			toReturn = true;
		else
			toReturn = false;

		if (!toReturn && a != null && b != null && a.equals(b))
			toReturn = true;
		return toReturn;
	}

	public Integer insertModelInstanceWithKpi(ModelInstance toCreate)
	throws EMFUserError {
		logger.debug("IN");
		Integer idToReturn;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Integer parentId = toCreate.getParentId();
			// set the sbiKpiModel
			SbiKpiModelInst sbiKpiModelInst = new SbiKpiModelInst();
			sbiKpiModelInst.setName(toCreate.getName());
			sbiKpiModelInst.setDescription(toCreate.getDescription());
			sbiKpiModelInst.setLabel(toCreate.getLabel());
			sbiKpiModelInst.setStartDate(toCreate.getStartDate());
			sbiKpiModelInst.setEndDate(toCreate.getEndDate());
			sbiKpiModelInst.setModelUUID(toCreate.getModelUUID());

			Model aModel = toCreate.getModel();
			if (aModel != null && aModel.getId() != null) {
				SbiKpiModel sbiKpiModel = (SbiKpiModel) aSession.load(
						SbiKpiModel.class, aModel.getId());
				sbiKpiModelInst.setSbiKpiModel(sbiKpiModel);

				// set the sbiKpiInstance
				KpiInstance kpiInst = toCreate.getKpiInstance();

				//if already present
				if(kpiInst != null){
					SbiKpiInstance sbiKpiInstance = new SbiKpiInstance();
					if(kpiInst.getKpi() != null){

						SbiKpi sbiKpi =  (SbiKpi) aSession.load(SbiKpi.class, kpiInst.getKpi());						
						sbiKpiInstance.setSbiKpi(sbiKpi);
						if(kpiInst.getThresholdId() != null){
							SbiThreshold sbiThr =  (SbiThreshold) aSession.load(SbiThreshold.class, kpiInst.getThresholdId());						
							sbiKpiInstance.setSbiThreshold(sbiThr);
						}
						sbiKpiInstance.setWeight(kpiInst.getWeight());

						sbiKpiInstance.setTarget(kpiInst.getTarget());						

						//if periodicity exists then set it
						if(kpiInst.getPeriodicityId() != null){
							Set periods = new HashSet<SbiKpiInstPeriod>();
							SbiKpiPeriodicity sbiPeriodicity =  (SbiKpiPeriodicity) aSession.load(SbiKpiPeriodicity.class, kpiInst.getPeriodicityId());	
							if(sbiPeriodicity != null){
								periods.add(sbiPeriodicity);
								sbiKpiInstance.setSbiKpiInstPeriods(periods);
							}
						}
						if(kpiInst.getChartTypeId() != null){
							SbiDomains chartType =  (SbiDomains) aSession.load(SbiDomains.class, kpiInst.getChartTypeId());	
							sbiKpiInstance.setChartType(chartType);
						}
						Calendar now = Calendar.getInstance();
						sbiKpiInstance.setBeginDt(now.getTime());
						updateSbiCommonInfo4Insert(sbiKpiInstance);
						aSession.save(sbiKpiInstance);
						sbiKpiModelInst.setSbiKpiInstance(sbiKpiInstance);
					}
				}
			}
			if (parentId != null) {
				SbiKpiModelInst sbiKpiModelInstParent = (SbiKpiModelInst) aSession
				.load(SbiKpiModelInst.class, parentId);
				sbiKpiModelInst.setSbiKpiModelInst(sbiKpiModelInstParent);
			}
			updateSbiCommonInfo4Insert(sbiKpiModelInst);
			idToReturn = (Integer) aSession.save(sbiKpiModelInst);

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

	public Integer insertModelInstance(ModelInstance toCreate)
	throws EMFUserError {
		logger.debug("IN");
		Integer idToReturn;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Integer parentId = toCreate.getParentId();
			// set the sbiKpiModel
			SbiKpiModelInst sbiKpiModelInst = new SbiKpiModelInst();
			sbiKpiModelInst.setName(toCreate.getName());
			sbiKpiModelInst.setDescription(toCreate.getDescription());
			sbiKpiModelInst.setLabel(toCreate.getLabel());
			sbiKpiModelInst.setStartDate(toCreate.getStartDate());
			sbiKpiModelInst.setEndDate(toCreate.getEndDate());
			sbiKpiModelInst.setModelUUID(toCreate.getModelUUID());

			Model aModel = toCreate.getModel();
			if (aModel != null && aModel.getId() != null) {
				SbiKpiModel sbiKpiModel = (SbiKpiModel) aSession.load(
						SbiKpiModel.class, aModel.getId());
				sbiKpiModelInst.setSbiKpiModel(sbiKpiModel);

				// set the sbiKpiInstance
				SbiKpi sbiKpi = sbiKpiModel.getSbiKpi();
				if (sbiKpi != null) {
					SbiKpiInstance sbiKpiInstance = new SbiKpiInstance();
					sbiKpiInstance.setSbiKpi(sbiKpi);
					sbiKpiInstance.setSbiThreshold(sbiKpi.getSbiThreshold());
					sbiKpiInstance.setWeight(sbiKpi.getWeight());
					Calendar now = Calendar.getInstance();
					sbiKpiInstance.setBeginDt(now.getTime());
					aSession.save(sbiKpiInstance);
					sbiKpiModelInst.setSbiKpiInstance(sbiKpiInstance);
				}

			}
			if (parentId != null) {
				SbiKpiModelInst sbiKpiModelInstParent = (SbiKpiModelInst) aSession
				.load(SbiKpiModelInst.class, parentId);
				sbiKpiModelInst.setSbiKpiModelInst(sbiKpiModelInstParent);
			}
			updateSbiCommonInfo4Insert(sbiKpiModelInst);
			idToReturn = (Integer) aSession.save(sbiKpiModelInst);

			// insert or update the udp values
			//DAOFactory.getUdpDAOValue().insertOrUpdateRelatedUdpValues(toCreate, sbiKpiModelInst, aSession, "MODEL");

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

	public ModelInstance loadModelInstanceWithChildrenById(Integer id)
	throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		ModelInstance toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiModelInst hibSbiKpiModelInst = (SbiKpiModelInst) aSession
			.load(SbiKpiModelInst.class, id);
			toReturn = toModelInstanceWithChildren(aSession,
					hibSbiKpiModelInst, null);
		} catch (HibernateException he) {
			logger.error("Error while loading the ModelInstance with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

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

	public ModelInstance loadModelInstanceWithChildrenByLabel(String label)
	throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		ModelInstance toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiModelInst hibSbiKpiModelInst = (SbiKpiModelInst) aSession
			.load(SbiKpiModelInst.class, label);
			toReturn = toModelInstanceWithChildren(aSession,
					hibSbiKpiModelInst, null);
		} catch (HibernateException he) {
			logger.error("Error while loading the ModelInstance with label "
					+ ((label == null) ? "" : label.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

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

	private ModelInstance toModelInstanceWithChildren(Session session,
			SbiKpiModelInst value, Integer parentId) {
		logger.debug("IN");
		ModelInstance toReturn = new ModelInstance();
		String name = value.getName();
		String description = value.getDescription();
		String label = value.getLabel();
		Date startDate = value.getStartDate();
		Date endDate = value.getEndDate();
		Integer id = value.getKpiModelInst();
		SbiKpiModel sbiKpiModel = value.getSbiKpiModel();
		String modelUUID = value.getModelUUID();
		Model aModel = ModelDAOImpl
		.toModelWithoutChildren(sbiKpiModel, session);
		SbiKpiInstance sbiKpiInstance = value.getSbiKpiInstance();

		if (sbiKpiInstance != null) {
			// toKpiInstance
			KpiInstance aKpiInstance = new KpiInstance();
			aKpiInstance.setKpiInstanceId(sbiKpiInstance.getIdKpiInstance());
			aKpiInstance.setKpi(sbiKpiInstance.getSbiKpi().getKpiId());
			if (sbiKpiInstance.getSbiThreshold() != null) {
				aKpiInstance.setThresholdId(sbiKpiInstance.getSbiThreshold()
						.getThresholdId());
			}
			if (sbiKpiInstance.getChartType() != null) {
				aKpiInstance.setChartTypeId(sbiKpiInstance.getChartType()
						.getValueId());
			}
			// TODO
			if (sbiKpiInstance.getSbiKpiInstPeriods() != null
					&& !(sbiKpiInstance.getSbiKpiInstPeriods().isEmpty())) {
				SbiKpiInstPeriod instPeriod = (SbiKpiInstPeriod) sbiKpiInstance
				.getSbiKpiInstPeriods().toArray()[0];

				aKpiInstance.setPeriodicityId(instPeriod.getSbiKpiPeriodicity()
						.getIdKpiPeriodicity());
			}
			aKpiInstance.setWeight(sbiKpiInstance.getWeight());
			aKpiInstance.setTarget(sbiKpiInstance.getTarget());
			aKpiInstance.setD(sbiKpiInstance.getBeginDt());
			//
			toReturn.setKpiInstance(aKpiInstance);
		}

		List childrenNodes = new ArrayList();

		Criteria critt = session.createCriteria(SbiKpiModelInst.class);
		critt.add(Expression.eq("sbiKpiModelInst", value));
		critt.createCriteria("sbiKpiModel").addOrder(Order.asc("kpiModelCd"));

		List children = critt.list();

		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			SbiKpiModelInst sbiKpichild = (SbiKpiModelInst) iterator.next();
			ModelInstance child = toModelInstanceWithChildren(session,
					sbiKpichild, id);
			childrenNodes.add(child);
		}

		toReturn.setId(id);
		toReturn.setName(name);
		toReturn.setDescription(description);
		toReturn.setLabel(label);
		toReturn.setStartDate(startDate);
		toReturn.setEndDate(endDate);
		toReturn.setChildrenNodes(childrenNodes);
		toReturn.setParentId(parentId);
		toReturn.setModel(aModel);
		toReturn.setModelUUID(modelUUID);

		logger.debug("OUT");
		return toReturn;
	}

	public List getCandidateModelChildren(Integer parentId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiModelInst sbiKpiModelInst = (SbiKpiModelInst) aSession.load(
					SbiKpiModelInst.class, parentId);
			SbiKpiModel aModel = (SbiKpiModel) sbiKpiModelInst.getSbiKpiModel();

			// Load all Children
			if (aModel != null) {
				Set modelChildren = aModel.getSbiKpiModels();
				// Load all ModelInstance Children
				Set modelInstanceChildren = sbiKpiModelInst
				.getSbiKpiModelInsts();
				// Remove all Children just instantiated
				for (Iterator iterator = modelInstanceChildren.iterator(); iterator
				.hasNext();) {
					SbiKpiModelInst child = (SbiKpiModelInst) iterator.next();
					modelChildren.remove(child.getSbiKpiModel());
				}
				for (Iterator iterator = modelChildren.iterator(); iterator
				.hasNext();) {
					SbiKpiModel sbiKpiModelCandidate = (SbiKpiModel) iterator
					.next();
					Model modelCandidate = new Model();
					modelCandidate.setId(sbiKpiModelCandidate.getKpiModelId());
					modelCandidate
					.setName(sbiKpiModelCandidate.getKpiModelNm());
					toReturn.add(modelCandidate);
				}
			}

		} catch (HibernateException he) {
			logger.error(
					"Error while loading the model canidate children of the parent "
					+ ((parentId == null) ? "" : parentId.toString()),
					he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

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

	private void deleteKpiInstance(Session aSession, Integer kpiInstId)
	throws EMFUserError {
		SbiKpiInstance sbiKpiInst = (SbiKpiInstance) aSession.load(
				SbiKpiInstance.class, kpiInstId);

		// deleteKpiHistory(Integer sbiKpiInstance)
		Criteria critt = aSession.createCriteria(SbiKpiInstanceHistory.class);
		critt.add(Expression.eq("sbiKpiInstance", sbiKpiInst));
		List sbiKpiInstanceHistory = critt.list();

		for (Iterator iterator = sbiKpiInstanceHistory.iterator(); iterator
		.hasNext();) {
			SbiKpiInstanceHistory sbiKpiH = (SbiKpiInstanceHistory) iterator
			.next();

			aSession.delete(sbiKpiH);
		}

		deleteKpiValue(aSession, kpiInstId);

		aSession.delete(sbiKpiInst);
	}

	private void deleteKpiValue(Session aSession, Integer kpiInstId) {
		SbiKpiInstance sbiKpiInst = (SbiKpiInstance) aSession.load(
				SbiKpiInstance.class, kpiInstId);
		Criteria critt = aSession.createCriteria(SbiKpiValue.class);
		critt.add(Expression.eq("sbiKpiInstance", sbiKpiInst));
		List sbiKpiValueList = critt.list();

		for (Iterator iterator = sbiKpiValueList.iterator(); iterator.hasNext();) {
			SbiKpiValue sbiKpiValue = (SbiKpiValue) iterator.next();

			aSession.delete(sbiKpiValue);

		}
	}

	public void deleteKpiValue(Integer kpiInstId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiInstance sbiKpiInst = (SbiKpiInstance) aSession.load(
					SbiKpiInstance.class, kpiInstId);
			Criteria critt = aSession.createCriteria(SbiKpiValue.class);
			critt.add(Expression.eq("sbiKpiInstance", sbiKpiInst));
			List sbiKpiValueList = critt.list();

			for (Iterator iterator = sbiKpiValueList.iterator(); iterator
			.hasNext();) {
				SbiKpiValue sbiKpiValue = (SbiKpiValue) iterator.next();

				aSession.delete(sbiKpiValue);
			}
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}

	}

	public boolean deleteModelInstance(Integer modelId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiKpiModelInst aModelInst = (SbiKpiModelInst) aSession.load(
					SbiKpiModelInst.class, modelId);
			recursiveStepDelete(aSession, aModelInst);
			deleteModelInstKpiInstResourceValue(aSession, aModelInst);

			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Model Instance", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10015);
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Model ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);
		} finally {
			aSession.close();
		}
		return true;
	}

	private void recursiveStepDelete(Session aSession,
			SbiKpiModelInst aModelInst) throws EMFUserError {
		Set children = aModelInst.getSbiKpiModelInsts();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			SbiKpiModelInst modelInstChild = (SbiKpiModelInst) iterator.next();
			recursiveStepDelete(aSession, modelInstChild);
			// delete Model Instance, Kpi Inst, History, Resource and Value
			deleteModelInstKpiInstResourceValue(aSession, modelInstChild);
		}
	}

	private void deleteModelInstKpiInstResourceValue(Session aSession,
			SbiKpiModelInst aModelInst) throws EMFUserError {
		// Delete associations between the model and resources
		DAOFactory.getModelResourcesDAO().removeAllModelResource(
				aModelInst.getKpiModelInst());
		// delete the model Inst
		aSession.delete(aModelInst);
		// Delete Kpi Instance Kpi Instance History Value
		if (aModelInst.getSbiKpiInstance() != null) {			
			//look up for periodicities
			List<KpiInstPeriod> instPeriods = DAOFactory.getKpiInstPeriodDAO().loadKpiInstPeriodId(aModelInst.getSbiKpiInstance()
					.getIdKpiInstance());
			if(instPeriods != null){
				for(int i=0; i<instPeriods.size(); i++){
					KpiInstPeriod instPer = instPeriods.get(i);
					SbiKpiInstPeriod sbiKpiInstPer = (SbiKpiInstPeriod)aSession.load(SbiKpiInstPeriod.class, instPer.getId());
					aSession.delete(sbiKpiInstPer);
				}
				aSession.flush();
			}
			deleteKpiInstance(aSession, aModelInst.getSbiKpiInstance()
					.getIdKpiInstance());
		}
	}

	public List loadModelsInstanceRoot(String fieldOrder, String typeOrder)
	throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria crit = aSession.createCriteria(SbiKpiModelInst.class);
			crit.add(Expression.isNull("sbiKpiModelInst"));

			if (fieldOrder != null && typeOrder != null) {
				if (typeOrder.toUpperCase().trim().equals("ASC"))
					crit.addOrder(Order
							.asc(getModelInstanceProperty(fieldOrder)));
				if (typeOrder.toUpperCase().trim().equals("DESC"))
					crit.addOrder(Order
							.desc(getModelInstanceProperty(fieldOrder)));
			}

			List sbiKpiModelInstanceList = crit.list();
			for (Iterator iterator = sbiKpiModelInstanceList.iterator(); iterator
			.hasNext();) {
				SbiKpiModelInst sbiKpiModelInst = (SbiKpiModelInst) iterator
				.next();
				ModelInstance aModelInst = toModelInstanceWithoutChildren(
						sbiKpiModelInst, aSession);
				toReturn.add(aModelInst);
			}
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
		return toReturn;
	}

	private String getModelInstanceProperty(String property) {
		String toReturn = null;
		if (property != null && property.equals("NAME"))
			toReturn = "name";
		return toReturn;
	}

	/**
	 * Returns the root of a model instance
	 * 
	 * @param mi
	 *            The model instance
	 * @return
	 * @throws EMFUserError
	 */
	public ModelInstance loadModelInstanceRoot(ModelInstance mi)
	throws EMFUserError {
		logger.debug("IN");
		if (mi.getParentId() == null) {
			logger.debug("OUT");
			return mi;
		} else {
			ModelInstance miPar = loadModelInstanceWithoutChildrenById(mi
					.getParentId());
			logger.debug("Searching model instance parent.");
			return loadModelInstanceRoot(miPar);
		}
	}

	public Integer getExistentRootsByName(String name)
			throws EMFUserError {
		logger.debug("IN");
		Integer toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion nameCriterrion = Expression.eq("name", name);
			Criteria criteria = aSession.createCriteria(SbiKpiModelInst.class);
			criteria.add(nameCriterrion);
			criteria.add(Expression.isNull("sbiKpiModelInst"));
			List<SbiKpiModelInst> hibSbiKpiModelInsts = (List<SbiKpiModelInst>) criteria.list();
			if (hibSbiKpiModelInsts != null){
				//looks up for progressive names
				nameCriterrion = Expression.like("name", name+"_%");
				criteria = aSession.createCriteria(SbiKpiModelInst.class);
				criteria.add(nameCriterrion);
				criteria.add(Expression.isNull("sbiKpiModelInst"));
				List<SbiKpiModelInst> progrMI = (List<SbiKpiModelInst>) criteria.list();
				if (progrMI != null && progrMI.size() != 0){
					toReturn =  progrMI.size();
				}else{
					toReturn = hibSbiKpiModelInsts.size();
				}
				
			}


		} catch (HibernateException he) {
			logger.error("Error while loading the Model Instance root with name "
					+ ((name == null) ? "null" : name), he);

			if (tx != null)
				tx.rollback();
			logger.error(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

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
