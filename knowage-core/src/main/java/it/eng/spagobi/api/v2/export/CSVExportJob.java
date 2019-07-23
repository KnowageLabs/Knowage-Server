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
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.iterator.CsvStreamingOutput;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;

/**
 * Export dataset as CSV file.
 *
 * @author Marco Libanori
 */
public class CSVExportJob extends AbstractExportJob {

	private static final Logger logger = Logger.getLogger(CSVExportJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

		Integer dataSetId = getDataSetId(mergedJobDataMap);
		Map<String, Object> drivers = getDriversData(mergedJobDataMap);
		UUID id = getJobId(mergedJobDataMap);
		Map<String, String> parameters = getParametersData(mergedJobDataMap);
		String resourcePathAsStr = getResourcePathString(mergedJobDataMap);
		UserProfile userProfile = getUserProfile(mergedJobDataMap);

		logger.debug("Start CSV export for dataSetId " + dataSetId + " with id " + id + " by user " + userProfile.getUserId());

		java.nio.file.Path resourcePath = ExportPathBuilder.getInstance().getPerJobExportPath(resourcePathAsStr, userProfile, id);

		// The random id in the path let create a new directory every time
		try {
			Files.createDirectories(resourcePath);
			IDataSet dataSet = getDataSet(dataSetId, drivers, parameters, userProfile);

			DataIterator iterator = null;
			OutputStream exportFileOS = null;
			try {
				logger.debug("Starting iteration to transfer data");
				iterator = dataSet.iterator();

				StreamingOutput stream = new CsvStreamingOutput(iterator);
				java.nio.file.Path exportFile = resourcePath.resolve(dataSet.getName() + ".csv");
				exportFileOS = Files.newOutputStream(exportFile);
				stream.write(exportFileOS);
			} catch (Exception e) {
				if (iterator != null) {
					iterator.close();
				}
				throw e;
			} finally {
				if (exportFileOS != null) {
					exportFileOS.close();
				}
			}
		} catch (IOException e) {
			String msg = String.format("Error during create of directory \"%s\"!", resourcePath);
			logger.error(msg, e);
			throw new JobExecutionException(e);
		}

		logger.debug("End CSV export for dataSetId " + dataSetId + " with id " + id + " by user " + userProfile.getUserId());

	}

}
