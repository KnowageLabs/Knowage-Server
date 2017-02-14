package it.eng.spagobi.utilities.locks.listeners;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tools.license.HostInfo;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class HazelcastContextListener implements ServletContextListener {

	static private Logger logger = Logger.getLogger(HazelcastContextListener.class);

	private static final long DEFAULT_HAZELCAST_LEASETIME = 240;

	/**
	 * insert into distributed map info for license
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		logger.debug("IN");
		// recover map and insert
		IMap<String, HostInfo> mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
				SpagoBIConstants.DISTRIBUTED_MAP_FOR_LICENSE);

		logger.debug("Got distributed map for licenses with " + mapLocks != null ? mapLocks.keySet().size() + "values" : "is null");

		// Get localhost and add itsa info to distributed map
		String hostname = SpagoBIUtilities.getCurrentHostName();
		logger.debug("Put in distributed map infos for current host " + hostname);
		try {
			// current hostname, build HostInfo and set them on map,
			if (mapLocks.tryLock(hostname, 1, TimeUnit.SECONDS, DEFAULT_HAZELCAST_LEASETIME, TimeUnit.SECONDS)) {
				HostInfo hostInfo = new HostInfo();
				mapLocks.put(hostname, hostInfo);

				// HostInfo hi = new HostInfo();
				// hi.setHardwareId("testOtherId");
				// mapLocks.put("testOther", hi);

			}
		} catch (InterruptedException e) {
			logger.error("The current thread has failed to release the lock for hostname [" + hostname + "] in time.", e);
		} finally {
			mapLocks.unlock(hostname);
		}

		logger.debug("OUT");

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// DistributedLockFactory.getHazelcastInstance(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME).getLifecycleService().shutdown();
		Hazelcast.shutdownAll();
	}
}
