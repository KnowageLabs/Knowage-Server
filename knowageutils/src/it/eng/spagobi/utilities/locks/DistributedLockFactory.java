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

import org.apache.log4j.Logger;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class DistributedLockFactory {

	static private Logger logger = Logger.getLogger(DistributedLockFactory.class);

	@SuppressWarnings("rawtypes")
	public static IMap getDistributedMap(String instanceName, String mapName) {
		logger.debug("Getting Hazelcast instance with name [" + instanceName + "]");
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(instanceName);
		if (hz == null) {
			logger.debug("No Hazelcast instance with name [" + instanceName + "] found");
			logger.debug("Creating Hazelcast instance with name [" + instanceName + "]");
			ClasspathXmlConfig cfg = new ClasspathXmlConfig("hazelcast.xml");
			cfg.setInstanceName(instanceName);
			hz = Hazelcast.newHazelcastInstance(cfg);
		}
		logger.debug("Getting Hazelcast map with name [" + mapName + "]");
		return hz.getMap(mapName);
	}

	public static HazelcastInstance getHazelcastInstance(String instanceName) {
		ClasspathXmlConfig cfg = new ClasspathXmlConfig("hazelcast.xml");
		cfg.setInstanceName(instanceName);
		return Hazelcast.getOrCreateHazelcastInstance(cfg);
	}
}
