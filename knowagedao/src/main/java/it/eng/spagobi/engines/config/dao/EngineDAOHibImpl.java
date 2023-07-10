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
package it.eng.spagobi.engines.config.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.metadata.SbiEngines;

/**
 * Defines the Hibernate implementations for all DAO methods, for an engine.
 *
 * @author zoppello
 */
public class EngineDAOHibImpl extends AbstractHibernateDAO implements IEngineDAO {

	private static final Logger LOGGER = Logger.getLogger(EngineDAOHibImpl.class);

	/**
	 * Load engine by id.
	 *
	 * @param engineID the engine id
	 *
	 * @return the engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */
	@Override
	public Engine loadEngineByID(Integer engineID) throws EMFUserError {
		LOGGER.debug("IN");
		Engine toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		LOGGER.debug("engine Id is " + engineID);
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, engineID);
			LOGGER.debug("hib engine loaded");
			toReturn = toEngine(hibEngine);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();
			LOGGER.error("error in loading engine by Id", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	/**
	 * Load engine by label.
	 *
	 * @param engineLabel the engine label
	 *
	 * @return the engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */

	@Override
	public Engine loadEngineByLabel(String engineLabel) throws EMFUserError {
		LOGGER.debug("IN");
		boolean isFound = false;
		Engine engine = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			LOGGER.debug("engine label is " + engineLabel);
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQueryProd = aSession
					.createQuery("select opt.sbiProductType from SbiOrganizationProductType opt " + "where opt.sbiOrganizations.name = :tenant ");
			hibQueryProd.setString("tenant", getTenant());

			List<SbiProductType> hibListProd = hibQueryProd.list();
			Iterator<SbiProductType> productIt = hibListProd.iterator();

			while (productIt.hasNext() && !isFound) {
				SbiProductType productType = productIt.next();

				Query hibQueryEng = aSession.createQuery("select pte.sbiEngines from SbiProductTypeEngine pte "
						+ "where pte.sbiProductType.label = :productType " + "and pte.sbiEngines.label = :engine");

				hibQueryEng.setString("productType", productType.getLabel());
				hibQueryEng.setString("engine", engineLabel);

				SbiEngines hibEngine = (SbiEngines) hibQueryEng.uniqueResult();

				if (hibEngine != null) {
					isFound = true;
					engine = toEngine(hibEngine);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("Error in retrieving engine by label " + engineLabel, he);

			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSession(aSession);
		}
		if (engine == null) {
			LOGGER.debug("A null engine has been returned for label " + engineLabel);
		}
		LOGGER.debug("OUT");
		return engine;
	}

	/**
	 * Load engine by driver name.
	 *
	 * @param engineLabel the driver name
	 *
	 * @return the engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#loadEngineByID(java.lang.Integer)
	 */

	@Override
	public Engine loadEngineByDriver(String driver) throws EMFUserError {
		LOGGER.debug("IN");
		boolean isFound = false;
		Engine engine = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			LOGGER.debug("engine driver is " + driver);
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQueryProd = aSession
					.createQuery("select opt.sbiProductType from SbiOrganizationProductType opt " + "where opt.sbiOrganizations.name = :tenant ");
			hibQueryProd.setString("tenant", getTenant());

			List<SbiProductType> hibListProd = hibQueryProd.list();
			Iterator<SbiProductType> productIt = hibListProd.iterator();

			while (productIt.hasNext() && !isFound) {
				SbiProductType productType = productIt.next();

				Query hibQueryEng = aSession.createQuery("select pte.sbiEngines from SbiProductTypeEngine pte "
						+ "where pte.sbiProductType.label = :productType " + "and pte.sbiEngines.driverNm = :driver");

				hibQueryEng.setString("productType", productType.getLabel());
				hibQueryEng.setString("driver", driver);

				SbiEngines hibEngine = (SbiEngines) hibQueryEng.uniqueResult();

				if (hibEngine != null) {
					isFound = true;
					engine = toEngine(hibEngine);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("Error in retrieving engine by label " + driver, he);

			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSession(aSession);
		}
		if (engine == null) {
			LOGGER.debug("No engine with driver [" + driver + "] was found.");
		}
		LOGGER.debug("OUT");
		return engine;
	}

	/**
	 * Load all engines.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<Engine> loadAllEngines() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<Engine> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiEngines");

			List<SbiEngines> hibList = hibQuery.list();
			Iterator<SbiEngines> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Error in loading all engines", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Load paged engines list
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<Engine> loadPagedEnginesList(Integer offset, Integer fetchSize) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<Engine> realResult = new ArrayList<>();
		try {

			if (offset == null) {
				LOGGER.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				LOGGER.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			aSession = getSession();
			tx = aSession.beginTransaction();

			Query countQuery = aSession.createQuery("select count(*) from SbiEngines sb ");
			Long resultNumber = (Long) countQuery.uniqueResult();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue()) : resultNumber.intValue();
			}

			Query listQuery = aSession.createQuery("from SbiEngines h order by h.label ");
			listQuery.setFirstResult(offset);
			if (fetchSize > 0)
				listQuery.setMaxResults(fetchSize);

			List<SbiEngines> enginesList = listQuery.list();

			Iterator<SbiEngines> it = enginesList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine(it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Error in loading all engines", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public List<Engine> loadAllEnginesByTenant() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Set<String> addedEngines = new HashSet<>();
		List<Engine> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQueryProd = aSession
					.createQuery("select opt.sbiProductType from SbiOrganizationProductType opt " + "where opt.sbiOrganizations.name = :tenant ");
			hibQueryProd.setString("tenant", getTenant());

			List<SbiProductType> hibListProd = hibQueryProd.list();
			Iterator<SbiProductType> productIt = hibListProd.iterator();

			while (productIt.hasNext()) {
				SbiProductType productType = productIt.next();

				Query hibQueryEng = aSession
						.createQuery("select pte.sbiEngines from SbiProductTypeEngine pte " + "where pte.sbiProductType.label = :productType ");

				hibQueryEng.setString("productType", productType.getLabel());

				List<SbiEngines> hibListEngine = hibQueryEng.list();
				Iterator<SbiEngines> engineIt = hibListEngine.iterator();
				while (engineIt.hasNext()) {
					SbiEngines sbiEngine = engineIt.next();
					if (addedEngines.add(sbiEngine.getLabel())) {
						realResult.add(toEngine(sbiEngine));
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Error in loading all engines", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		if (realResult.isEmpty()) {
			LOGGER.debug("No engines was found.");
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Load all engines for bi object type.
	 *
	 * @param biobjectType the biobject type
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<Engine> loadAllEnginesForBIObjectType(String biobjectType) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		LOGGER.debug("BiObject Type is " + biobjectType);
		List<Engine> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiEngines engines where engines.biobjType.valueCd = ?");
			hibQuery.setString(0, biobjectType);
			List<SbiEngines> hibList = hibQuery.list();
			Iterator<SbiEngines> it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toEngine(it.next()));
			}
		} catch (HibernateException he) {
			LOGGER.debug("Error in loading ecgines for biObject Type " + biobjectType, he);
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public List<Engine> loadAllEnginesForBIObjectTypeAndTenant(String biobjectType) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		LOGGER.debug("BiObject Type is " + biobjectType);
		Set<String> addedEngines = new HashSet<>();
		List<Engine> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQueryProd = aSession
					.createQuery("select opt.sbiProductType from SbiOrganizationProductType opt " + "where opt.sbiOrganizations.name = :tenant ");
			hibQueryProd.setString("tenant", getTenant());

			List<SbiProductType> hibListProd = hibQueryProd.list();
			Iterator<SbiProductType> productIt = hibListProd.iterator();

			while (productIt.hasNext()) {
				SbiProductType productType = productIt.next();

				Query hibQueryEng = aSession.createQuery("select pte.sbiEngines from SbiProductTypeEngine pte "
						+ "where pte.sbiProductType.label = :productType " + "and pte.sbiEngines.biobjType.valueCd = :biobjectType");

				hibQueryEng.setString("productType", productType.getLabel());
				hibQueryEng.setString("biobjectType", biobjectType);

				List<SbiEngines> hibListEngine = hibQueryEng.list();
				Iterator<SbiEngines> engineIt = hibListEngine.iterator();
				while (engineIt.hasNext()) {
					SbiEngines sbiEngine = engineIt.next();
					if (addedEngines.add(sbiEngine.getLabel())) {
						realResult.add(toEngine(sbiEngine));
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.debug("Error in loading ecgines for biObject Type " + biobjectType, he);
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		if (realResult.isEmpty()) {
			LOGGER.debug("No engines was found.");
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Modify engine.
	 *
	 * @param aEngine the a engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#modifyEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	@Override
	public void modifyEngine(Engine aEngine) throws EMFUserError {
		LOGGER.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, aEngine.getId());
			SbiDomains hibDomainBiobjType = (SbiDomains) aSession.load(SbiDomains.class, aEngine.getBiobjTypeId());
			SbiDomains hibDomainEngineType = (SbiDomains) aSession.load(SbiDomains.class, aEngine.getEngineTypeId());

			hibEngine.setName(aEngine.getName());
			hibEngine.setLabel(aEngine.getLabel());
			hibEngine.setDescr(aEngine.getDescription());
			hibEngine.setDriverNm(aEngine.getDriverName());
			hibEngine.setEncrypt((short) aEngine.getCriptable().intValue());
			hibEngine.setMainUrl(aEngine.getUrl());
			hibEngine.setObjUplDir(aEngine.getDirUpload());
			hibEngine.setObjUseDir(aEngine.getDirUsable());
			hibEngine.setSecnUrl(aEngine.getSecondaryUrl());
			hibEngine.setEngineType(hibDomainEngineType);
			hibEngine.setClassNm(aEngine.getClassName());
			hibEngine.setBiobjType(hibDomainBiobjType);
			hibEngine.setUseDataSet(aEngine.getUseDataSet());
			hibEngine.setUseDataSource(aEngine.getUseDataSource());
			updateSbiCommonInfo4Update(hibEngine, true);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Error in modifying engine ", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");

	}

	/**
	 * Insert engine.
	 *
	 * @param aEngine the a engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#insertEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	@Override
	public void insertEngine(Engine aEngine) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDomains hibDomainBiobjType = (SbiDomains) aSession.load(SbiDomains.class, aEngine.getBiobjTypeId());
			SbiDomains hibDomainEngineType = (SbiDomains) aSession.load(SbiDomains.class, aEngine.getEngineTypeId());
			SbiEngines hibEngine = new SbiEngines();
			hibEngine.setName(aEngine.getName());
			hibEngine.setLabel(aEngine.getLabel());
			hibEngine.setDescr(aEngine.getDescription());
			hibEngine.setDriverNm(aEngine.getDriverName());
			hibEngine.setEncrypt((short) aEngine.getCriptable().intValue());
			hibEngine.setMainUrl(aEngine.getUrl());
			hibEngine.setObjUplDir(aEngine.getDirUpload());
			hibEngine.setObjUseDir(aEngine.getDirUsable());
			hibEngine.setSecnUrl(aEngine.getSecondaryUrl());
			hibEngine.setEngineType(hibDomainEngineType);
			hibEngine.setClassNm(aEngine.getClassName());
			hibEngine.setBiobjType(hibDomainBiobjType);
			hibEngine.setUseDataSet(aEngine.getUseDataSet());
			hibEngine.setUseDataSource(aEngine.getUseDataSource());
			updateSbiCommonInfo4Insert(hibEngine, true);
			aSession.save(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Inserting new engine ", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
	}

	/**
	 * Erase engine.
	 *
	 * @param aEngine the a engine
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#eraseEngine(it.eng.spagobi.engines.config.bo.Engine)
	 */
	@Override
	public void eraseEngine(Engine aEngine) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, aEngine.getId());
			aSession.delete(hibEngine);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("Error in erasing engine ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
	}

	/**
	 * From the hibernate Engine at input, gives the corrispondent <code>Engine</code> object.
	 *
	 * @param hibEngine The hybernate engine
	 *
	 * @return The corrispondent <code>Engine</code> object
	 */
	public Engine toEngine(SbiEngines hibEngine) {
		LOGGER.debug("IN");
		if (hibEngine != null)
			LOGGER.debug("Label is " + hibEngine.getLabel());
		Engine eng = new Engine();
		eng.setCriptable(hibEngine.getEncrypt().intValue());
		eng.setDescription(hibEngine.getDescr());
		eng.setDirUpload(hibEngine.getObjUplDir());
		eng.setDirUsable(hibEngine.getObjUseDir());
		eng.setDriverName(hibEngine.getDriverNm());
		eng.setId(hibEngine.getEngineId());
		eng.setName(hibEngine.getName());
		eng.setLabel(hibEngine.getLabel());
		eng.setUseDataSet(hibEngine.getUseDataSet().booleanValue());
		eng.setUseDataSource(hibEngine.getUseDataSource().booleanValue());
		eng.setSecondaryUrl(hibEngine.getSecnUrl());
		eng.setUrl(hibEngine.getMainUrl());
		eng.setLabel(hibEngine.getLabel());
		eng.setEngineTypeId(hibEngine.getEngineType().getValueId());
		eng.setClassName(hibEngine.getClassNm());
		eng.setBiobjTypeId(hibEngine.getBiobjType().getValueId());
		LOGGER.debug("OUT");

		return eng;
	}

	/**
	 * Checks for bi obj associated.
	 *
	 * @param engineId the engine id
	 *
	 * @return true, if checks for bi obj associated
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.engines.config.dao.IEngineDAO#hasBIObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasBIObjAssociated(String engineId) throws EMFUserError {
		/**
		 * TODO Hibernate Implementation
		 */
		boolean bool = false;
		LOGGER.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer engineIdInt = Integer.valueOf(engineId);

			String hql = " from SbiObjects s where s.sbiEngines.engineId = ?";
			Query aQuery = aSession.createQuery(hql);
			aQuery.setInteger(0, engineIdInt);
			List<SbiObjects> biObjectsAssocitedWithEngine = aQuery.list();
			if (!biObjectsAssocitedWithEngine.isEmpty())
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			LOGGER.error("HAs biObject associated", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}
		LOGGER.debug("OUT");
		return bool;
	}

	@Override
	public List<String> getAssociatedExporters(Engine engine) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		List<String> toReturn = new ArrayList<>();
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			IConfigDAO sbiConfigDAO = DAOFactory.getSbiConfigDAO();
			String engineLabel = engine.getLabel();
			String configLabel = ConfigurationConstants.DOCUMENT_EXPORTER_PREFIX + engineLabel;
			Config exportersConfig = sbiConfigDAO.loadConfigParametersByLabel(configLabel);
			List<String> exporters = Arrays.asList(exportersConfig.getValueCheck().split(ConfigurationConstants.DOCUMENT_EXPORTER_SEPARATOR));

			for (String exporter : exporters) {
				toReturn.add(exporter);
			}
			tx.commit();
			LOGGER.debug("OUT");
			return toReturn;

		} catch (Exception he) {
			logException(he);
			LOGGER.error("error in getting Associated Exporters", he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSession(aSession);
		}

	}

	@Override
	public Integer countEngines() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiEngines ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = temp.intValue();

		} catch (HibernateException he) {
			LOGGER.error("Error while loading the list of BIEngines", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			closeSession(aSession);
		}
		return resultNumber;
	}

}
