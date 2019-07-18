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
package it.eng.spagobi.behaviouralmodel.lov.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;

/**
 * Defines the Hibernate implementations for all DAO methods, for a list of values.
 *
 * @author sulis
 */

public class LovDAOHibImpl extends AbstractHibernateDAO implements IModalitiesValueDAO {

	private static transient Logger logger = Logger.getLogger(LovDAOHibImpl.class);

	/**
	 * Load modalities value by id.
	 *
	 * @param modalitiesValueID
	 *            the modalities value id
	 *
	 * @return the modalities value
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#loadModalitiesValueByID(Integer)
	 */
	@Override
	public ModalitiesValue loadModalitiesValueByID(Integer modalitiesValueID) throws EMFUserError {
		logger.debug("IN");
		ModalitiesValue modVal = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiLov hibLov = (SbiLov) aSession.load(SbiLov.class, modalitiesValueID);
			modVal = toModalityValue(hibLov);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

		return modVal;
	}

	/**
	 * Load modalities value by label.
	 *
	 * @param label
	 *            the label
	 *
	 * @return the modalities value
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#loadModalitiesValueByID(Integer)
	 */
	@Override
	public ModalitiesValue loadModalitiesValueByLabel(String label) throws EMFUserError {
		logger.debug("IN");
		ModalitiesValue modVal = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiLov s where s.label = '" + label + "'";
			String hql = "from SbiLov s where s.label = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, label);
			SbiLov hibLov = (SbiLov) hqlQuery.uniqueResult();
			modVal = toModalityValue(hibLov);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			aSession.close();
			logger.debug("OUT");
		}
		return modVal;
	}

	/**
	 * Modify modalities value.
	 *
	 * @param aModalitiesValue
	 *            the a modalities value
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#modifyModalitiesValue(it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue)
	 */
	@Override
	public void modifyModalitiesValue(ModalitiesValue aModalitiesValue) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiLov hibLov = (SbiLov) aSession.load(SbiLov.class, aModalitiesValue.getId());
			hibLov.setName(aModalitiesValue.getName());
			hibLov.setLabel(aModalitiesValue.getLabel());
			hibLov.setDescr(aModalitiesValue.getDescription());
			SbiDomains inpType = (SbiDomains) aSession.load(SbiDomains.class, new Integer(aModalitiesValue.getITypeId()));
			hibLov.setInputType(inpType);
			hibLov.setInputTypeCd(aModalitiesValue.getITypeCd());
			hibLov.setLovProvider(aModalitiesValue.getLovProvider());
			updateSbiCommonInfo4Update(hibLov);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Insert modalities value.
	 *
	 * @param aModalitiesValue
	 *            the a modalities value
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#insertModalitiesValue(it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue)
	 */
	@Override
	public Integer insertModalitiesValue(ModalitiesValue aModalitiesValue) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiLov hibLov = new SbiLov();
			hibLov.setName(aModalitiesValue.getName());
			hibLov.setLabel(aModalitiesValue.getLabel());
			hibLov.setDescr(aModalitiesValue.getDescription());
			SbiDomains inpType = (SbiDomains) aSession.load(SbiDomains.class, new Integer(aModalitiesValue.getITypeId()));
			hibLov.setInputType(inpType);
			hibLov.setInputTypeCd(aModalitiesValue.getITypeCd());
			hibLov.setLovProvider(aModalitiesValue.getLovProvider());
			updateSbiCommonInfo4Insert(hibLov);
			id = (Integer) aSession.save(hibLov);
			tx.commit();
			return id;
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Erase modalities value.
	 *
	 * @param aModalitiesValue
	 *            the a modalities value
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#eraseModalitiesValue(it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue)
	 */
	@Override
	public void eraseModalitiesValue(ModalitiesValue aModalitiesValue) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiLov hibLov = (SbiLov) aSession.load(SbiLov.class, aModalitiesValue.getId());
			aSession.delete(hibLov);
			tx.commit();
		} catch (HibernateException he) {
			logger.debug("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Load all modalities value.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#loadAllModalitiesValue()
	 */

	@Override
	public List loadAllModalitiesValue() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiLov");
			List hibList = hibQuery.list();
			tx.commit();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toModalityValue((SbiLov) it.next()));
			}
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("IN");
		}
		return realResult;
	}

	@Override
	public List<ModalitiesValue> loadModalitiesValueByParamaterId(Integer idParameter) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("select lov from SbiParameters as param " + "inner join param.sbiParuses as paruses "
					+ "inner join paruses.sbiLov as lov " + "where  param.parId = " + idParameter);
			;
			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toModalityValue((SbiLov) it.next()));
			}
			tx.commit();
			realResult = lovListAnDr(realResult);

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("IN");
		}

		return realResult;
	}

	@Override
	public List<ModalitiesValue> loadModalitiesValueByBIObjectLabel(String label) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession
					.createQuery("select lov from SbiObjects as obj " + "inner join obj.sbiObjPars as objPars " + "inner join objPars.sbiParameter as param "
							+ "inner join param.sbiParuses as paruses " + "inner join paruses.sbiLov as lov " + "where  obj.label = '" + label + "'");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toModalityValue((SbiLov) it.next()));
			}

			tx.commit();
			realResult = lovList(realResult);

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("IN");
		}
		return realResult;
	}

	/**
	 * Load all modalities value order by code.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#loadAllModalitiesValueOrderByCode()
	 */
	@Override
	public List loadAllModalitiesValueOrderByCode() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiLov s order by s.inputTypeCd");
			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toModalityValue((SbiLov) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
		return realResult;

	}

	/**
	 * Checks for parameters.
	 *
	 * @param lovId
	 *            the lov id
	 *
	 * @return true, if checks for parameters
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO#hasParameters(java.lang.String)
	 */
	@Override
	public boolean hasParameters(String lovId) throws EMFUserError {
		logger.debug("IN");
		boolean result = true;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			int lovIdInt = Integer.parseInt(lovId);
			String hql = "from SbiParuse s where s.sbiLov.lovId = :lovIdInt or s.sbiLovForDefault.lovId = :lovIdInt or s.sbiLovForMax.lovId = :lovIdInt";

			Query hibQuery = aSession.createQuery(hql);
			hibQuery.setInteger("lovIdInt", lovIdInt);
			List hibList = hibQuery.list();

			if (hibList.size() > 0) {
				result = true;
			} else {
				result = false;
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.debug("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

		return result;

	}

	/**
	 * From the hibernate LOV at input, gives the corrispondent <code>ModalitiesValue</code> object.
	 *
	 * @param hiObjPar
	 *            The hybernate LOV
	 * @return The corrispondent <code>ModalitiesValue</code> object
	 */
	private ModalitiesValue toModalityValue(SbiLov hibLov) {
		logger.debug("IN");
		ModalitiesValue modVal = new ModalitiesValue();
		modVal.setDescription(hibLov.getDescr());
		modVal.setId(hibLov.getLovId());
		modVal.setITypeCd(hibLov.getInputTypeCd());
		modVal.setITypeId(String.valueOf(hibLov.getInputType().getValueId()));
		// modVal.setDataset(hibLov.getDatasetId());
		// modVal.setDataset(hibLov.getDatasetId());
		modVal.setLovProvider(hibLov.getLovProvider());
		modVal.setName(hibLov.getName());
		modVal.setLabel(hibLov.getLabel());
		logger.debug("OUT");
		return modVal;
	}

	private List<ModalitiesValue> lovList(List<ModalitiesValue> dupLovList) {
		List<ModalitiesValue> realList = new ArrayList();

		for (int i = 0; i < dupLovList.size(); i++) {
			boolean contains = false;
			for (int j = 0; j < realList.size(); j++) {
				if (dupLovList.get(i).getId().equals(realList.get(j).getId())) {
					contains = true;
				}
			}
			if (!contains)
				realList.add(dupLovList.get(i));
		}

		return realList;
	}

	private List<ModalitiesValue> lovListAnDr(List<ModalitiesValue> dupLovList) {
		List<ModalitiesValue> realList = new ArrayList();

		for (int i = 0; i < dupLovList.size(); i++) {
			boolean contains = false;
			for (int j = 0; j < realList.size(); j++) {
				if (dupLovList.get(i).getId().equals(realList.get(j).getId())) {
					contains = true;
				}
			}
			if (!contains)
				realList.add(dupLovList.get(i));
		}

		return realList;
	}

}
