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
package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.iterator.CsvStreamingOutput;

/**
 * Export dataset as CSV file.
 *
 * @author Marco Libanori
 */
public class CSVExportJob extends AbstractExportJob {

	private static final Logger logger = Logger.getLogger(CSVExportJob.class);

	@Override
	protected void export(JobExecutionContext context) throws JobExecutionException {

		logger.debug("Start CSV export for dataSetId " + getDataSetId() + " with id " + getId() + " by user " + getUserProfile().getUserId());

		OutputStream exportFileOS = getDataOutputStream();
		try {
			IDataSet dataSet = getDataSet();

			IDataStore dataStore = null;
			try {
				logger.debug("Starting iteration to transfer data");
				dataSet.loadData();
				dataStore = dataSet.getDataStore();
				StreamingOutput stream = new CsvStreamingOutput(dataStore);
				stream.write(exportFileOS);
			} finally {
				if (exportFileOS != null) {
					exportFileOS.close();
				}
			}
		} catch (IOException e) {
			String msg = String.format("Error writing data file \"%s\"!", getDataFile());
			logger.error(msg, e);
			throw new JobExecutionException(e);
		}

		LogMF.info(logger, "CSV export completed for user {0}. DataSet is {1}. Final file: dimension (in bytes): {2,number}, path: [{3}], ",
				this.getUserProfile().getUserId(), this.getDataSet().getLabel(), getDataFile().toFile().length(), getDataFile().toString());

	}

	@Override
	protected String extension() {
		return "csv";
	}

	@Override
	protected String mime() {
		return "text/csv";
	}

}
