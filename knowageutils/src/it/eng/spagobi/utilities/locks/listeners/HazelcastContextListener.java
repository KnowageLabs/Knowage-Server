package it.eng.spagobi.utilities.locks.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hazelcast.core.Hazelcast;

public class HazelcastContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		// DistributedLockFactory.getHazelcastInstance(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME).getLifecycleService().shutdown();
		Hazelcast.shutdownAll();
	}
}
