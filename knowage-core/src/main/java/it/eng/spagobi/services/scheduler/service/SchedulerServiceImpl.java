/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.services.scheduler.service;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.scheduler.SchedulerService;
import it.eng.spagobi.services.security.exceptions.SecurityException;

/**
/**
 * @author Marco Libanori
 */
@WebService(
		name = "SchedulerServiceService",
		portName = "SchedulerServicePort",
		serviceName = "SchedulerService",
		targetNamespace = "http://scheduler.services.spagobi.eng.it/"
	)
public class SchedulerServiceImpl extends AbstractServiceImpl implements SchedulerService {

	private static Logger logger = Logger.getLogger(SchedulerServiceImpl.class);

	private ISchedulerServiceSupplier supplier = null;

	/**
	 * Lazy initialization of the supplier.
	 */
	private ISchedulerServiceSupplier getSupplier() {
		if (supplier == null) {
			supplier = SchedulerServiceSupplierFactory
					.getSupplier();
		}
		return supplier;
	}

	/**
	 * Gets the job list.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 *
	 * @return the job list
	 */
	@Override
	public String getJobList(String token, String user) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobList");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().getJobList();
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Gets the job schedulation list.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param jobName
	 *            the job name
	 * @param jobGroup
	 *            the job group
	 *
	 * @return the job schedulation list
	 */
	@Override
	public String getJobSchedulationList(String token, String user,
			String jobName, String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobSchedulationList");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().getJobSchedulationList(jobName, jobGroup);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Delete schedulation.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param triggerName
	 *            the trigger name
	 * @param triggerGroup
	 *            the trigger group
	 *
	 * @return the string
	 */
	@Override
	public String deleteSchedulation(String token, String user,
			String triggerName, String triggerGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.deleteSchedulation");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().deleteSchedulation(triggerName, triggerGroup);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Delete job.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param jobName
	 *            the job name
	 * @param jobGroupName
	 *            the job group name
	 *
	 * @return the string
	 */
	@Override
	public String deleteJob(String token, String user, String jobName,
			String jobGroupName) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.deleteJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().deleteJob(jobName, jobGroupName);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Define job.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param xmlRequest
	 *            the xml request
	 *
	 * @return the string
	 */
	@Override
	public String defineJob(String token, String user, String xmlRequest) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.defineJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().defineJob(xmlRequest);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Gets the job definition.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param jobName
	 *            the job name
	 * @param jobGroup
	 *            the job group
	 *
	 * @return the job definition
	 */
	@Override
	public String getJobDefinition(String token, String user, String jobName,
			String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().getJobDefinition(jobName, jobGroup);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Schedule job.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param xmlRequest
	 *            the xml request
	 *
	 * @return the string
	 */
	@Override
	public String scheduleJob(String token, String user, String xmlRequest) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.scheduleJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().scheduleJob(xmlRequest);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Gets the job schedulation definition.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param triggerName
	 *            the trigger name
	 * @param triggerGroup
	 *            the trigger group
	 *
	 * @return the job schedulation definition
	 */
	@Override
	public String getJobSchedulationDefinition(String token, String user,
			String triggerName, String triggerGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobSchedulationDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().getJobSchedulationDefinition(triggerName,
					triggerGroup);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Exist job definition.
	 *
	 * @param token
	 *            the token
	 * @param user
	 *            the user
	 * @param jobName
	 *            the job name
	 * @param jobGroup
	 *            the job group
	 *
	 * @return the string
	 */
	@Override
	public String existJobDefinition(String token, String user, String jobName,
			String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.existJobDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return getSupplier().existJobDefinition(jobName, jobGroup);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

}
