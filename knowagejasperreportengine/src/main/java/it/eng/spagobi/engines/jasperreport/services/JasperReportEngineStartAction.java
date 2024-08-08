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
package it.eng.spagobi.engines.jasperreport.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.engines.jasperreport.JasperReportEngine;
import it.eng.spagobi.engines.jasperreport.JasperReportEngineInstance;
import it.eng.spagobi.engines.jasperreport.JasperReportEngineTemplate;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @authors Andrea Gioia (andrea.gioia@eng.it) Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class JasperReportEngineStartAction extends AbstractEngineStartServlet {

	private static final String CONNECTION_NAME = "connectionName";
	private static final String OUTPUT_TYPE = "outputType";

	/** Logger component. */
	private static final Logger LOGGER = LogManager.getLogger(JasperReportEngineStartAction.class);

	@Override
	public void service(EngineStartServletIOManager servletIOManager) throws SpagoBIEngineException {

		IDataSource dataSource;
		IDataSet dataSet;
		String connectionName;
		String outputType;

		JasperReportEngineTemplate template;
		JasperReportEngineInstance engineInstance;

		LOGGER.debug("IN");

		try {
			LOGGER.debug("User: [{}]", servletIOManager.getUserId());
			LOGGER.debug("Document: [{}]", servletIOManager.getDocumentId());

			dataSource = servletIOManager.getDataSource();
			LOGGER.debug("Datasource: [{}]", (dataSource == null ? dataSource : dataSource.getLabel()));
			if (dataSource == null) {
				LOGGER.warn("This document doesn't have the Data Source");
			}

			dataSet = servletIOManager.getDataSet();
			LOGGER.debug("Dataset: [{}]", (dataSet == null ? dataSource : dataSet.getName()));

			// read and log builtin parameters
			connectionName = servletIOManager.getParameterAsString(CONNECTION_NAME);
			LOGGER.debug("Parameter [{}] is equal to [{}]", CONNECTION_NAME, connectionName);

			outputType = servletIOManager.getParameterAsString(OUTPUT_TYPE);
			LOGGER.debug("Parameter [{}] is equal to [{}]", OUTPUT_TYPE, outputType);
			if (StringUtils.isEmpty(outputType)) {
				outputType = JasperReportEngine.getConfig().getDefaultOutputType();
				servletIOManager.getEnv().put(OUTPUT_TYPE, outputType);
				LOGGER.debug("Parameter [{}] has been set to the default value [{}]", OUTPUT_TYPE, servletIOManager.getEnv().get(OUTPUT_TYPE));
			}

			// this proxy is used by ScriptletChart to execute and embed external chart into report
			servletIOManager.getEnv().put(EngineConstants.ENV_DOCUMENT_EXECUTE_SERVICE_PROXY,
					servletIOManager.getDocumentExecuteServiceProxy());

			servletIOManager.auditServiceStartEvent();

			template = new JasperReportEngineTemplate(servletIOManager.getTemplateName(),
					servletIOManager.getTemplate(false));

			Path reportOutputDir = JasperReportEngine.getConfig().getReportOutputDir();
			Path reportFile = reportOutputDir.resolve(Paths.get("report" + "." + outputType).normalize());
			DataSetServiceProxy proxyDataset = servletIOManager.getDataSetServiceProxy();

			engineInstance = JasperReportEngine.createInstance(template, servletIOManager.getEnv(), proxyDataset);
			engineInstance.setId(servletIOManager.getParameterAsString("SBI_EXECUTION_ID"));
			servletIOManager.getHttpSession().setAttribute(engineInstance.getId(), engineInstance);
			engineInstance.setOutputType(outputType);

			engineInstance.runReport(reportFile.toFile(), servletIOManager.getRequest());

			servletIOManager.writeBackToClient(200, reportFile.toFile(), true, "report." + outputType,
					JasperReportEngine.getConfig().getMIMEType(outputType));

			// instant cleaning
			Files.delete(reportOutputDir);

			servletIOManager.auditServiceEndEvent();
		} catch (Exception e) {
			throw new SpagoBIEngineException(
					"An error occurred while executing report. Check log file for more information", e);
		} finally {
			LOGGER.debug("OUT");
		}

	}

}
