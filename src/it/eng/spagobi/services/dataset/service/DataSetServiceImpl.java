/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.dataset.service;

import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.dataset.DataSetService;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Andrea Gioia
 */
public class DataSetServiceImpl extends AbstractServiceImpl implements
		DataSetService {

	private DataSetSupplier supplier = new DataSetSupplier();

	static private Logger logger = Logger.getLogger(DataSetServiceImpl.class);

	/**
	 * Instantiates a new data source service impl.
	 */
	public DataSetServiceImpl() {
		super();
	}

	public SpagoBiDataSet getDataSet(String token, String user,
			String documentId) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.dataset.getDataSet");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getDataSet(documentId);
		} catch (Exception e) {
			logger.error("Error while getting dataset for document with id "
					+ documentId, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	public SpagoBiDataSet getDataSetByLabel(String token, String user,
			String label) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.dataset.getDataSetByLabel");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getDataSetByLabel(label);
		} catch (Exception e) {
			logger.error("Error while getting dataset with label " + label, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	/**
	 * 
	 * @param token
	 *            String
	 * @param user
	 *            String
	 * @return SpagoBiDataSet[]
	 */
	public SpagoBiDataSet[] getAllDataSet(String token, String user) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.dataset.getAllDataSet");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.getAllDataSet();
		} catch (Exception e) {
			logger.error("Error while getting all datasets", e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	public SpagoBiDataSet saveDataSet(String token, String user,
			SpagoBiDataSet dataset) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.dataset.saveDataSet");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);
			return supplier.saveDataSet(dataset);
		} catch (Exception e) {
			logger.error("Errors saving dataset "
					+ dataset, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}
	
}
