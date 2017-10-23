/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api;

import java.net.URLEncoder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.hdfs.work.HDFSWriteWork;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 */

@Path("/1.0/hdfs")
@ManageAuthorization
public class HDFSResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(HDFSResource.class);

	@POST
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_BIG_DATA })
	public Response writeDataSet(@PathParam("id") Integer id, String body) {
		String requestUUID;

		try {
			Assert.assertNotNull(id, "DataSet id cannot be null");

			IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
			dataSetDAO.setUserProfile(getUserProfile());

			IDataSet dataSet = dataSetDAO.loadDataSetById(id);
			if (!dataSet.isPersistedHDFS()) {
				logger.warn("The request asks to write the dataSet [" + dataSet.getLabel()
						+ "] on HDFS, but the dataSet has not been set as exportable in the dataSet catalogue. Continue anyway...");
			}

			HDFSWriteWork hdfsWriteWork = new HDFSWriteWork(dataSet, getUserProfile());
			requestUUID = hdfsWriteWork.getUuid();

			logger.debug("Getting the JNDI Work Manager");
			WorkManager knowageWorkManager = new WorkManager(GeneralUtilities.getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
			Assert.assertNotNull(knowageWorkManager, "Impossible to save the store in background because the work manager is not properly initialized");

			commonj.work.WorkManager workManager = knowageWorkManager.getInnerInstance();
			workManager.schedule(hdfsWriteWork);

			return Response.ok(URLEncoder.encode(requestUUID, "UTF-8")).build();

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while init dataset writing on HDFS: " + e.getMessage(), e);
		}
	}

}
