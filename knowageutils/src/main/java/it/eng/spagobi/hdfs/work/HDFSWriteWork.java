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
package it.eng.spagobi.hdfs.work;

import java.util.UUID;

import org.apache.log4j.Logger;

import commonj.work.Work;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class HDFSWriteWork implements Work {

	private final IDataSet dataSet;
	private final UserProfile userProfile;
	private final String uuid;

	private static transient Logger logger = Logger.getLogger(HDFSWriteWork.class);

	/**
	 * @param dataSet
	 * @param userProfile
	 */
	public HDFSWriteWork(IDataSet dataSet, UserProfile userProfile) {
		super();
		this.dataSet = dataSet;
		this.userProfile = userProfile;
		this.uuid = UUID.randomUUID().toString();
	}

	@Override
	public void run() {
		logger.debug("IN");
		logger.debug("Started write work with UUID [" + uuid + "]");
		Assert.assertNotNull(userProfile, "User profile cannot be null");
		Assert.assertNotNull(dataSet, "DataSet cannot be null");
		try {
			TenantManager.setTenant(new Tenant(userProfile.getOrganization()));
			IPersistedManager ptm = new PersistedHDFSManager(userProfile);
			ptm.persistDataSet(dataSet);
			logger.debug("Write work with UUID [" + uuid + "] successfully finished");
		} catch (Exception e) {
			logger.debug("Error while executing work with UUID [" + uuid + "]");
			throw new SpagoBIRuntimeException("An unexpected error occured while writing dataSet [" + dataSet.getLabel() + "] on HDFS", e);
		} finally {
			TenantManager.unset();
			logger.debug("OUT");
		}
	}

	@Override
	public boolean isDaemon() {
		return false;
	}

	@Override
	public void release() {
		logger.debug("Nothing to release here.");
	}

	public String getUuid() {
		return uuid;
	}

}
