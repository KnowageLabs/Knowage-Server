/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.scheduler.service;

import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class SchedulerServiceImpl extends AbstractServiceImpl {

	static private Logger logger = Logger.getLogger(SchedulerServiceImpl.class);
	private ISchedulerServiceSupplier supplier = SchedulerServiceSupplierFactory
			.getSupplier();

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
	public String getJobList(String token, String user) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobList");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getJobList();
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
	public String getJobSchedulationList(String token, String user,
			String jobName, String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobSchedulationList");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getJobSchedulationList(jobName, jobGroup);
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
	public String deleteSchedulation(String token, String user,
			String triggerName, String triggerGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.deleteSchedulation");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.deleteSchedulation(triggerName, triggerGroup);
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
	public String deleteJob(String token, String user, String jobName,
			String jobGroupName) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.deleteJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.deleteJob(jobName, jobGroupName);
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
	public String defineJob(String token, String user, String xmlRequest) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.defineJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.defineJob(xmlRequest);
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
	public String getJobDefinition(String token, String user, String jobName,
			String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getJobDefinition(jobName, jobGroup);
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
	public String scheduleJob(String token, String user, String xmlRequest) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.scheduleJob");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.scheduleJob(xmlRequest);
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
	public String getJobSchedulationDefinition(String token, String user,
			String triggerName, String triggerGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.getJobSchedulationDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getJobSchedulationDefinition(triggerName,
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
	public String existJobDefinition(String token, String user, String jobName,
			String jobGroup) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.scheduler.existJobDefinition");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.existJobDefinition(jobName, jobGroup);
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
