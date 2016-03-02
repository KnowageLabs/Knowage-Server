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

import it.eng.spagobi.services.dataset.service.DataSetSupplier;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TODO: create an initializer that look up for all job whose job class is equal to XExecuteBIDocumentJob and replace it with ExecuteBIDocumentJob. The remove
 * class ExecuteBIDocumentJob and rename XExecuteBIDocumentJob to ExecuteBIDocumentJob. NOTE: the old implementation of ExecuteBIDocumentJob has been saved in
 * CopyOfExecuteBIDocumentJob
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ExecuteBIDocumentJob implements Job {

	private static Logger logger = Logger.getLogger(ExecuteBIDocumentJob.class);

	private final DataSetSupplier dataSetSupplier = new DataSetSupplier();

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");

		Job job = new XExecuteBIDocumentJob();
		job.execute(jobExecutionContext);
		logger.debug("OUT");
	}

}
