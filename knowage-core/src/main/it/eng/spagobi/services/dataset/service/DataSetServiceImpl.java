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
package it.eng.spagobi.services.dataset.service;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.dataset.DataSetService;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Andrea Gioia
 */
public class DataSetServiceImpl extends AbstractServiceImpl implements DataSetService {

	private final DataSetSupplier supplier = new DataSetSupplier();

	static private Logger logger = Logger.getLogger(DataSetServiceImpl.class);

	/**
	 * Instantiates a new data source service impl.
	 */
	public DataSetServiceImpl() {
		super();
	}

	@Override
	public SpagoBiDataSet getDataSet(String token, String user, String documentId) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.service.dataset.getDataSet");
		try {
			validateTicket(token, user);
			IEngUserProfile profile = this.setTenantByUserId(user);
			return supplier.getDataSet(documentId, (UserProfile)profile);
		} catch (Exception e) {
			logger.error("Error while getting dataset for document with id " + documentId, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	@Override
	public SpagoBiDataSet getDataSetByLabel(String token, String user, String label) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.service.dataset.getDataSetByLabel");
		try {
			validateTicket(token, user);
			IEngUserProfile profile = this.setTenantByUserId(user);
			return supplier.getDataSetByLabel(label,profile);
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
	@Override
	public SpagoBiDataSet[] getAllDataSet(String token, String user) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.service.dataset.getAllDataSet");
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

	@Override
	public SpagoBiDataSet saveDataSet(String token, String user, SpagoBiDataSet dataset) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.service.dataset.saveDataSet");
		try {
			validateTicket(token, user);
			this.setTenantByUserId(user);

			// START -> section added to manage the QBE datamart retriever in DataSetFactory
			IEngUserProfile profile = GeneralUtilities.createNewUserProfile(user);
			Assert.assertNotNull(profile, "Impossible to find the user profile");
			String userId = ((UserProfile) profile).getUserId().toString();
			// END

			return supplier.saveDataSet(dataset, userId, null);
		} catch (Exception e) {
			logger.error("Errors saving dataset " + dataset, e);
			return null;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

}
