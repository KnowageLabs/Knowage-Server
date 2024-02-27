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
package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiConfig;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ConfigurationsInitializer extends SpagoBIInitializer {

	private static Logger logger = Logger.getLogger(ConfigurationsInitializer.class);

	public ConfigurationsInitializer() {
		targetComponentName = "Configurations";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/configs.xml";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		List<SbiConfig> configurationParameters;
		Map<String, SbiConfig> existingParametersMap;

		logger.debug("IN");

		try {
			configurationParameters = parseConfiguration(hibernateSession);
			if (configurationParameters == null || configurationParameters.isEmpty()) {
				return;
			}
			logger.debug("Configuration file contains the definition of [" + configurationParameters.size()
					+ "] configuration parameter(s)");

			existingParametersMap = getConfParametersFromDatabase(hibernateSession);
			if (existingParametersMap == null || existingParametersMap.keySet().size() == 0) {
				logger.debug("Database does not contain configuration parameters");
			} else {
				logger.debug(
						"Database contains [" + existingParametersMap.keySet().size() + "] configuration parameter(s)");
			}

			for (SbiConfig configurationParameter : configurationParameters) {
				if (existingParametersMap.containsKey(configurationParameter.getLabel())) {
					logger.debug("Parameter [" + configurationParameter.getLabel() + "] already present in database");
				} else {
					hibernateSession.save(configurationParameter);
					logger.debug(
							"Parameter [" + configurationParameter.getLabel() + "] succesfully saved into database");
				}
			}

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializeng Configurations", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private Map<String, SbiConfig> getConfParametersFromDatabase(Session hibernateSession) {
		Map<String, SbiConfig> existingParametersMap;

		logger.debug("IN");

		existingParametersMap = null;
		try {
			String hql = "from SbiConfig";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiConfig> configurationParametersFromDatabase = hqlQuery.list();
			existingParametersMap = new HashMap<>();
			for (SbiConfig configurationParameterFromDatabase : configurationParametersFromDatabase) {
				existingParametersMap.put(configurationParameterFromDatabase.getLabel(),
						configurationParameterFromDatabase);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(
					"An unexpected error occured while loading configuration parameters from database", t);
		} finally {
			logger.debug("OUT");
		}

		return existingParametersMap;
	}

	private List<SbiConfig> parseConfiguration(Session hibernateSession) {
		List<SbiConfig> configurations;

		logger.debug("IN");

		configurations = new ArrayList<>();
		try {
			SourceBean configSB = getConfiguration();
			if (configSB == null) {
				logger.info("Configuration file for predefined configuration parameters not found");
				return configurations;
			}

			List<SourceBean> configList = configSB.getAttributeAsList("CONFIG");
			if (configList == null || configList.isEmpty()) {
				logger.info("No predefined configuration parameters available from configuration file");
				return configurations;
			}

			for (SourceBean aConfigSB : configList) {
				SbiDomains hibDomain = null;

				String valueTypeCd = (String) aConfigSB.getAttribute("valueType");
				if (StringUtils.isNotEmpty(valueTypeCd)) {
					hibDomain = findDomain(hibernateSession, valueTypeCd, "PAR_TYPE");
					if (hibDomain == null) {
						logger.error("Could not find domain for configuration parameter");
						return configurations;
					}
				}

				String confLabel = ((String) aConfigSB.getAttribute("label"));
				String confName = ((String) aConfigSB.getAttribute("name"));
				String confDesc = ((String) aConfigSB.getAttribute("description"));
				String confIsActive = ((String) aConfigSB.getAttribute("isActive"));
				String confValueCheck = ((String) aConfigSB.getAttribute("valueCheck"));
				String confCategory = ((String) aConfigSB.getAttribute("category"));

				SbiConfig configuration = new SbiConfig();
				configuration.setLabel(confLabel);
				configuration.setName(confName);
				configuration.setDescription(confDesc);
				configuration.setValueCheck(confValueCheck);
				configuration.setSbiDomains(hibDomain);
				configuration.setCategory(confCategory);

				Boolean value = confIsActive != null ? Boolean.valueOf(confIsActive) : Boolean.FALSE;
				configuration.setIsActive(value.booleanValue());

				configurations.add(configuration);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(
					"Ab unexpected error occured while reading configuration parameters from file", t);
		} finally {
			logger.debug("OUT");
		}

		return configurations;
	}

}
