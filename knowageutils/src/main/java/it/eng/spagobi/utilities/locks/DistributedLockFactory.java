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

package it.eng.spagobi.utilities.locks;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.constants.HazelcastConstant;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class DistributedLockFactory {

	private static Logger logger = Logger.getLogger(DistributedLockFactory.class);
	private static volatile Config defaultConfig = null;

	@SuppressWarnings("rawtypes")
	public static IMap getDistributedMap(String instanceName, String mapName) {
		logger.debug("Getting Hazelcast map with name [" + mapName + "]");
		HazelcastInstance hz = getHazelcastInstance(instanceName);
		return hz.getMap(mapName);
	}

	public static synchronized HazelcastInstance getHazelcastInstance(String instanceName) {
		logger.debug("Getting Hazelcast instance with name [" + instanceName + "]");
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(instanceName);
		if (hz == null) {
			logger.debug("No Hazelcast instance with name [" + instanceName + "] found");
			logger.debug("Creating Hazelcast instance with name [" + instanceName + "]");
			Config config = getDefaultConfig();
			config.setInstanceName(instanceName);
			hz = Hazelcast.newHazelcastInstance(config);
		}
		return hz;
	}

	private static Config getDefaultConfig() {
		try {
			if (defaultConfig == null) {
				String configFilename = System.getProperty(HazelcastConstant.HAZELCAST_CONFIG);
				if (configFilename != null) {
					logger.debug("Creating Hazelcast instance from system property config [" + configFilename + "]");
					defaultConfig = new FileSystemXmlConfig(configFilename);
				} else {
					logger.debug("Creating Hazelcast instance from classpath config [" + configFilename + "]");
					defaultConfig = new ClasspathXmlConfig("hazelcast.xml");
				}
			}
		} catch (FileNotFoundException ex) {
			throw new SpagoBIRuntimeException("Impossible to load system property config for Hazelcast", ex);
		}
		return defaultConfig;
	}

	public static void setDefaultConfig(Config config) {
		defaultConfig = config;
	}
}
