/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

@Path("/1.0/output")
public class OutputsResource extends AbstractDataMiningEngineService {
	public static transient Logger logger = Logger.getLogger(OutputsResource.class);

	@GET
	@Path("/{command}")
	@Produces("text/html; charset=UTF-8")
	public String getOutputs(@PathParam("command") String commandName) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		String outputsJson = "";
		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			if (commands != null && !commands.isEmpty()) {
				for (Iterator it = commands.iterator(); it.hasNext();) {
					DataMiningCommand command = (DataMiningCommand) it.next();
					if (command.getName().equals(commandName)) {
						List<Output> outputs = command.getOutputs();
						outputsJson = serializeList(outputs);
					}

				}
			}
		}

		if (!isNullOrEmpty(outputsJson)) {
			logger.debug("Returning outputs list");
		} else {
			logger.debug("No outputs list found");
		}

		logger.debug("OUT");
		return outputsJson;
	}

	@GET
	@Path("/setAutoMode/{output}")
	@Produces("text/html; charset=UTF-8")
	public String setAutoMode(@PathParam("output") String outputName) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			for (Iterator it = commands.iterator(); it.hasNext();) {
				DataMiningCommand cmd = (DataMiningCommand) it.next();
				if (cmd.getMode().endsWith(DataMiningConstants.EXECUTION_TYPE_AUTO)) {
					List<Output> outputs = cmd.getOutputs();
					if (outputs != null && !outputs.isEmpty()) {
						for (Iterator it2 = outputs.iterator(); it2.hasNext();) {
							Output output = (Output) it2.next();
							if (output.getOutputName().equals(outputName)) {
								output.setOutputMode(DataMiningConstants.EXECUTION_TYPE_AUTO);
								// and starts execution!
							} else {
								output.setOutputMode(DataMiningConstants.EXECUTION_TYPE_MANUAL);
							}
						}
					}

				}
			}
		}
		logger.debug("OUT");
		return getJsonSuccess();
	}

	@GET
	@Path("/getVariables/{command}/{output}")
	@Produces("text/html; charset=UTF-8")
	public String getVariables(@PathParam("command") String commandName, @PathParam("output") String outputName) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		String variablesJson = "";
		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			if (commands != null) {
				for (Iterator it = commands.iterator(); it.hasNext();) {
					DataMiningCommand cmd = (DataMiningCommand) it.next();
					if (cmd.getName().equals(commandName)) {
						List<Output> outputs = cmd.getOutputs();
						if (outputs != null && !outputs.isEmpty()) {
							for (Iterator it2 = outputs.iterator(); it2.hasNext();) {
								Output output = (Output) it2.next();
								if (output.getOutputName().equals(outputName)) {
									List variables = output.getVariables();
									variablesJson = serializeList(variables);
								}
							}
						}
					}
				}
			}
		}

		if (!isNullOrEmpty(variablesJson)) {
			logger.debug("Returning variables list");
		} else {
			logger.debug("No variables list found");
		}

		logger.debug("OUT");
		return variablesJson;
	}

	@POST
	@Path("/setVariables/{command}/{output}")
	@Produces("text/html; charset=UTF-8")
	public String setVariables(@Context HttpServletRequest request, @PathParam("command") String commandName, @PathParam("output") String outputName) {
		logger.debug("IN");
		Map parameters = request.getParameterMap();
		if (parameters != null && !parameters.isEmpty()) {

			DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();

			List<DataMiningCommand> commands = null;
			if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
				commands = dataMiningEngineInstance.getCommands();
				if (commands != null) {
					for (Iterator it = commands.iterator(); it.hasNext();) {
						DataMiningCommand cmd = (DataMiningCommand) it.next();
						if (cmd.getName().equals(commandName)) {
							List<Output> outputs = cmd.getOutputs();
							if (outputs != null && !outputs.isEmpty()) {
								for (Iterator it2 = outputs.iterator(); it2.hasNext();) {
									Output output = (Output) it2.next();
									if (output.getOutputName().equals(outputName)) {
										List variables = output.getVariables();
										if (variables != null) {
											for (int i = 0; i < variables.size(); i++) {
												Variable var = (Variable) variables.get(i);
												// get the value from parameters
												if (request.getParameterMap().containsKey(var.getName())) {
													String paramVal = request.getParameter(var.getName());
													if (paramVal != null) {
														var.setValue(paramVal);
													}
												}
											}

										}
									}
								}
							}
						}
					}
				}

			}
		}
		logger.debug("OUT");
		return getJsonSuccess();
	}
}
