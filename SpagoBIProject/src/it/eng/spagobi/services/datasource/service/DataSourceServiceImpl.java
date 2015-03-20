/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.datasource.service;

import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.datasource.DataSourceService;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Provide the Data Source information
 */
public class DataSourceServiceImpl extends AbstractServiceImpl implements
		DataSourceService {
	
	static private Logger logger = Logger
			.getLogger(DataSourceServiceImpl.class);
	
	private DataSourceSupplier supplier = new DataSourceSupplier();

	/**
	 * Instantiates a new data source service impl.
	 */
	public DataSourceServiceImpl() {
		super();
	}

	/**
	 * Gets the data source.
	 * 
	 * @param token
	 *            String
	 * @param user
	 *            String
	 * @param documentId
	 *            String
	 * 
	 * @return SpagoBiDataSource
	 */
	public SpagoBiDataSource getDataSource(String token, String user,
			String documentId) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.datasource.getDataSource");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getDataSource(documentId);
		} catch (Exception e) {
			logger.error("Error while getting datasource for document with id " + documentId, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * Gets the data source by label.
	 * 
	 * @param token
	 *            String
	 * @param user
	 *            String
	 * @param label
	 *            String
	 * 
	 * @return SpagoBiDataSource
	 */
	public SpagoBiDataSource getDataSourceByLabel(String token, String user,
			String label) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.datasource.getDataSourceByLabel");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getDataSourceByLabel(label);
		} catch (Exception e) {
			logger.error("Error while getting datasource with label  " + label, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}
	
	/**
	 * Gets the data source by label.
	 * 
	 * @param token
	 *            String
	 * @param user
	 *            String
	 * @param id
	 *            int
	 * 
	 * @return SpagoBiDataSource
	 */
	
	public SpagoBiDataSource getDataSourceById(String token, String user,	Integer id) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.datasource.getDataSourceById");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getDataSourceById(id);
		} catch (Exception e) {
			logger.error("Error while getting datasource with id  " + id, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	/**
	 * Gets the all data source.
	 * 
	 * @param token
	 *            String
	 * @param user
	 *            String
	 * 
	 * @return SpagoBiDataSource[]
	 */
	public SpagoBiDataSource[] getAllDataSource(String token, String user) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.datasource.getAllDataSource");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getAllDataSource();
		} catch (Exception e) {
			logger.error("Error while getting all datasources", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}

	}


}
