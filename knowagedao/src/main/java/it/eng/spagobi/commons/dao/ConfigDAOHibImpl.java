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
package it.eng.spagobi.commons.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.metadata.SbiConfig;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.security.utils.EncryptionPBEWithMD5AndDESManager;
import it.eng.spagobi.utilities.cache.ConfigurationCache;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the Hibernate implementations for all DAO methods, for a domain.
 *
 * @author Monia Spinelli
 */
public class ConfigDAOHibImpl extends AbstractHibernateDAO implements IConfigDAO {

	private static final Logger LOGGER = LogManager.getLogger(ConfigDAOHibImpl.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.commons.dao.IUserFunctionalityDAO#loadAllConfigParameters()
	 */
	@Override
	public List<Config> loadAllConfigParameters() throws Exception {
		LOGGER.debug("Loading all configurations");

		ArrayList<Config> toReturn = new ArrayList<>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria hibQuery = aSession.createCriteria(SbiConfig.class);
			List<SbiConfig> hibList = hibQuery.list();
			Iterator<SbiConfig> it = hibList.iterator();
			while (it.hasNext()) {
				SbiConfig hibMap = it.next();
				if (hibMap != null) {
					Config biMap = hibMap.toConfig();
					toReturn.add(biMap);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all configurations", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("End loading all configurations");
		}
		return toReturn;

	}

	/**
	 * Load configuration by id.
	 *
	 * @param id the configuration id
	 *
	 * @return the config object
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersById(integer)
	 */
	@Override
	public Config loadConfigParametersById(int id) throws Exception {
		LOGGER.debug("Load configuration by id {}", id);
		Config toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiConfig hibMap = (SbiConfig) tmpSession.load(SbiConfig.class, id);
			toReturn = hibMap.toConfig();
			tx.commit();

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
			LOGGER.debug("End loading configuration by id {}", id);
		}
		return toReturn;
	}

	/**
	 * Load configuration by complete label.
	 *
	 * @param label the configuration label
	 *
	 * @return the config object
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersById(string)
	 */
	@Override
	public Config loadConfigParametersByLabel(String label) throws Exception {
		LOGGER.debug("Loading configuration by label {}", label);
		Config toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiConfig.class);
			criteria.add(labelCriterrion);

			SbiConfig hibConfig = (SbiConfig) criteria.uniqueResult();
			if (hibConfig == null) {
				return null;
			}
			toReturn = hibConfig.toConfig();

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
			LOGGER.debug("End loading configuration by label {}", label);
		}
		return toReturn;
	}

	/**
	 * Load configuration by a property node.
	 *
	 * @param prop the configuration label
	 *
	 * @return a list with all children of the property node
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.common.bo.dao.ISbiConfigDAO#loadConfigParametersByProperties(string)
	 */
	@Override
	public List<Config> loadConfigParametersByProperties(String prop) throws Exception {
		LOGGER.debug("Loading configuration by property {}", prop);

		ArrayList<Config> toReturn = new ArrayList<>();
		List<Config> allConfig = loadAllConfigParameters();
		// filter with the 'prop' parameter
		Iterator<Config> it = allConfig.iterator();
		while (it.hasNext()) {
			Config tmpConf = it.next();
			if (tmpConf.isActive() && tmpConf.getLabel().startsWith(prop))
				toReturn.add(tmpConf);
		}

		LOGGER.debug("End loading configuration by property {}", prop);
		return toReturn;
	}

	public SbiConfig fromConfig(Config config) {
		SbiConfig hibConfig = new SbiConfig(config.getId());
		hibConfig.setValueCheck(config.getValueCheck());
		
		hibConfig.setName(config.getName());
		hibConfig.setLabel(config.getLabel());
		hibConfig.setDescription(config.getDescription());
		hibConfig.setCategory(config.getCategory());
		hibConfig.setIsActive(config.isActive());
		return hibConfig;
	}

	/**
	 * Save config by id.
	 *
	 * @param id the id
	 *
	 * @return void
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 */
	@Override
	public void saveConfig(Config config) throws EMFUserError {
		LOGGER.debug("Saving configuration {}", config);
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiConfig hibConfig = null;
			Integer id = config.getId();

			Criterion domainCriterrion = Restrictions.eq("valueId", config.getValueTypeId());
			Criteria domainCriteria = aSession.createCriteria(SbiDomains.class);
			domainCriteria.add(domainCriterrion);

			SbiDomains hibDomains = (SbiDomains) domainCriteria.uniqueResult();

			if (id != null) {
				// modification
				LOGGER.debug("Updating configuration by id {}", id);
				hibConfig = (SbiConfig) aSession.load(SbiConfig.class, id);
				updateSbiCommonInfo4Update(hibConfig);
				hibConfig.setLabel(config.getLabel());
				hibConfig.setDescription(config.getDescription());
				hibConfig.setName(config.getName());

				String valueCheck = config.getValueCheck();
				if (hibConfig.getLabel().toLowerCase().endsWith(".password")) {
					if (valueCheck == null) {
						try {
							Config existingConfig = loadConfigParametersByLabel(config.getLabel());
							valueCheck = (existingConfig == null) ? null : existingConfig.getValueCheck();

						} catch (Exception e) {
							throw new SpagoBIRuntimeException(
									"An error occurred while getting configuration with label [" + config.getLabel()
											+ "]",
									e);
						}
					} else {
						valueCheck = EncryptionPBEWithMD5AndDESManager.encrypt(valueCheck);
					}
				}
				hibConfig.setValueCheck(valueCheck);

				hibConfig.setIsActive(config.isActive());
				hibConfig.setSbiDomains(hibDomains);
				hibConfig.setCategory(config.getCategory());
			} else {
				// insertion
				LOGGER.debug("Inserting new configuration");
				hibConfig = fromConfig(config);
				updateSbiCommonInfo4Insert(hibConfig);
				hibConfig.setSbiDomains(hibDomains);
			}

			Integer newId = (Integer) aSession.save(hibConfig);

			tx.commit();

			config.setId(newId);

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error saving configuration {}", config, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		ConfigurationCache.getCache().clear();
		LOGGER.debug("End saving configuration {}", config);
	}

	/**
	 * Delete config by id.
	 *
	 * @param id the id
	 *
	 * @return void
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 */
	@Override
	public void delete(Integer idConfig) throws EMFUserError {
		LOGGER.debug("Deleting configuration by id {}", idConfig);
		Session sess = null;
		Transaction tx = null;

		try {
			sess = getSession();
			tx = sess.beginTransaction();

			Criterion aCriterion = Restrictions.eq("id", idConfig);
			Criteria criteria = sess.createCriteria(SbiConfig.class);
			criteria.add(aCriterion);
			SbiConfig aSbiConfig = (SbiConfig) criteria.uniqueResult();
			if (aSbiConfig != null)
				sess.delete(aSbiConfig);
			tx.commit();

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error deleting configuration by id {}", idConfig, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(sess);
		}
		LOGGER.debug("End deleting configuration by id {}", idConfig);
	}

	@Override
	public List<Config> loadConfigParametersByCategory(String category) throws Exception {
		LOGGER.debug("Loading configurations by category {}", category);

		if (StringUtils.isEmpty(category)) {
			throw new IllegalArgumentException("Category cannot be null");
		}

		List<Config> ret = new ArrayList<>();
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("category", category);
			Criteria criteria = tmpSession.createCriteria(SbiConfig.class);
			criteria.add(labelCriterrion);

			List<SbiConfig> matchinConfigs = criteria.list();
			for (SbiConfig currConf : matchinConfigs) {
				ret.add(currConf.toConfig());
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
			LOGGER.debug("End loading configurations by category {}", category);
		}
		return ret;
	}

	@Override
	public Optional<Config> loadConfigParametersByLabelIfExist(String label) throws Exception {
		return Optional.ofNullable(loadConfigParametersByLabel(label));
	}

}