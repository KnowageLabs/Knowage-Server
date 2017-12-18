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
package it.eng.spagobi.services.datasource.service;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
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
	@Override
	public SpagoBiDataSource getDataSource(String token, String user,
			String documentId) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.datasource.getDataSource");
		try {
			validateTicket(token, user);
			IEngUserProfile profile = this.setTenantByUserId(user);
			return supplier.getDataSource(documentId, (UserProfile)profile);
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
	@Override
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

	@Override
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
	@Override
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
