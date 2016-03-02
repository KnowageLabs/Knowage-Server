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
package it.eng.spagobi.tools.scheduler.jobs;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ExecutePersistDatasetJob extends AbstractSpagoBIJob implements Job {

	static private Logger logger = Logger.getLogger(ExecutePersistDatasetJob.class);

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		try {
			this.setTenant(jobExecutionContext);
			this.executeInternal(jobExecutionContext);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	private void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		IEngUserProfile userProfile;
		String jobName;
		IDataSetDAO datasetDAO;

		logger.debug("IN");

		try {
			userProfile = UserProfile.createSchedulerUserProfile();
			jobName = jobExecutionContext.getJobDetail().getName();

			datasetDAO = DAOFactory.getDataSetDAO();
			if (userProfile != null) {
				datasetDAO.setUserProfile(userProfile);
			}

			logger.debug("Start persistence...");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(jobName);
			checkQbeDataset(((VersionedDataSet) dataset).getWrappedDataset());
			checkFileDataset(((VersionedDataSet) dataset).getWrappedDataset());

			PersistedTableManager ptm = new PersistedTableManager(userProfile);
			ptm.persistDataSet(dataset);
			logger.debug("Persistence ended succesfully!");
		} catch (Exception e) {
			logger.error("Error while executiong job ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void checkQbeDataset(IDataSet dataSet) {
		if (dataSet instanceof QbeDataSet) {
			SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
			Map parameters = dataSet.getParamsMap();
			if (parameters == null) {
				parameters = new HashMap();
				dataSet.setParamsMap(parameters);
			}
			dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
		}
	}

	private void checkFileDataset(IDataSet dataSet) {
		if (dataSet instanceof FileDataSet) {
			((FileDataSet) dataSet).setResourcePath(DAOConfig.getResourcePath());
		}
	}
}
