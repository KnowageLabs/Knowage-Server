/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.datasources.impl;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.datasources.bo.SDKDataSource;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.List;

import org.apache.log4j.Logger;

public class DataSourcesSDKServiceImpl extends AbstractSDKService implements it.eng.spagobi.sdk.datasources.DataSourcesSDKService {

	static private Logger logger = Logger.getLogger(DataSourcesSDKServiceImpl.class);

	public SDKDataSource getDataSource(Integer dataSourceId) throws NotAllowedOperationException {
		SDKDataSource toReturn = null;
		logger.debug("IN: dataSourceId in input = " + dataSourceId);
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASOURCE_MANAGEMENT, "User cannot see datasource congifuration.");
			if (dataSourceId == null) {
				logger.warn("DataSource identifier in input is null!");
				return null;
			}
			IDataSource dataSource = DAOFactory.getDataSourceDAO().loadDataSourceByID(dataSourceId);
			if (dataSource == null) {
				logger.warn("DataSource with identifier [" + dataSourceId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromSpagoBiDataSourceToSDKDataSource(dataSource.toSpagoBiDataSource());
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKDataSources list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	public SDKDataSource[] getDataSources() throws NotAllowedOperationException {
		SDKDataSource[] toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASOURCE_MANAGEMENT, "User cannot see datasources congifuration.");
			List dataSourceList = DAOFactory.getDataSourceDAO().loadAllDataSources();
			toReturn = new SDKDataSource[dataSourceList.size()];
			for (int i = 0; i < dataSourceList.size(); i++) {
				IDataSource dataSource = (IDataSource) dataSourceList.get(i);
				SDKDataSource sdkDataSource = new SDKObjectsConverter().fromSpagoBiDataSourceToSDKDataSource(dataSource.toSpagoBiDataSource());
				toReturn[i] = sdkDataSource;
			}
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKSource list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}	

}
