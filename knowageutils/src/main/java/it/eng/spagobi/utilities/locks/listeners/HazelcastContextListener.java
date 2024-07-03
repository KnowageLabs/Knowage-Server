package it.eng.spagobi.utilities.locks.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.map.IMap;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tools.license.HostInfo;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

public class HazelcastContextListener implements ServletContextListener {

	private static final Logger LOGGER = LogManager.getLogger(HazelcastContextListener.class);

	/**
	 * insert into distributed map info for license
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			LOGGER.debug("Initializing Hazelcast");
			// recover map and insert
			IMap<String, HostInfo> mapLocks = DistributedLockFactory.getDistributedMap(
					SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_LICENSE);

			LOGGER.debug("Got distributed map for licenses with {}",
					mapLocks != null ? mapLocks.keySet().size() + "values" : "is null");

			// Get localhost and add itsa info to distributed map
			String hostname = SpagoBIUtilities.getCurrentHostName();

			LOGGER.debug("Put in distributed map infos for current host {}", hostname);
			HostInfo hostInfo = new HostInfo();
			mapLocks.put(hostname, hostInfo);
			LOGGER.debug("Info put now size is {}", mapLocks.size());

			LOGGER.debug(System.identityHashCode(mapLocks));

			LOGGER.debug("End initializing Hazelcast");
		} catch (Exception e) {
			LOGGER.error("Error initializing Hazelcast", e);
			throw e;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		try {
			LOGGER.debug("Shutting down Hazelcast");
			Hazelcast.shutdownAll();
		} catch (Exception e) {
			LOGGER.error("Error shutting down Hazelcast", e);
			throw e;
		}
	}
}
