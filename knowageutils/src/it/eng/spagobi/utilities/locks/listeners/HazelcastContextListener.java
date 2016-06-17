package it.eng.spagobi.utilities.locks.listeners;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class HazelcastContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		DistributedLockFactory.getHazelcastInstance(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME).getLifecycleService().shutdown();
	}
}
