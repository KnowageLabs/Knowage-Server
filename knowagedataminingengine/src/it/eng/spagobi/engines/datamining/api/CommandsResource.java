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
package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.engines.datamining.serializer.SerializationException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

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

@Path("/1.0/command")
public class CommandsResource extends AbstractDataMiningEngineService {
	public static transient Logger logger = Logger.getLogger(CommandsResource.class);

	@GET
	@Produces("text/html; charset=UTF-8")
	public String getCommands() {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		String commandsJson = "";
		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			commandsJson = serializeList(commands);
		}

		if (!isNullOrEmpty(commandsJson)) {
			logger.debug("Returning commands list");
		} else {
			logger.debug("No commands list found");
		}

		logger.debug("OUT");
		return commandsJson;
	}

	@GET
	@Path("/{command}")
	@Produces("text/html; charset=UTF-8")
	public String setAutoMode(@PathParam("command") String commandName) {
		logger.debug("IN");
		String autoOutputJson = "";
		try {
			Output out = setAutoModeCommand(commandName, null);
			autoOutputJson = serialize(out);
		} catch (SerializationException e) {
			throw new SpagoBIEngineRuntimeException("Error serializing output", e);
		}

		logger.debug("OUT");
		return autoOutputJson;
	}

	public Output setAutoModeCommand(String commandName, DataMiningEngineInstance dataMiningEngineInstance) {
		Output outputResult = null;

		dataMiningEngineInstance = dataMiningEngineInstance == null ? getDataMiningEngineInstance() : dataMiningEngineInstance;

		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			if (commands != null) {
				for (Iterator it = commands.iterator(); it.hasNext();) {
					DataMiningCommand cmd = (DataMiningCommand) it.next();
					if (cmd.getName().equals(commandName)) {
						cmd.setMode(DataMiningConstants.EXECUTION_TYPE_AUTO);
						if (cmd.getOutputs() != null && !cmd.getOutputs().isEmpty()) {
							for (Iterator it2 = cmd.getOutputs().iterator(); it2.hasNext();) {
								Output output = (Output) it2.next();
								if (output.getOutputMode().equals(DataMiningConstants.EXECUTION_TYPE_AUTO)) {
									outputResult = output;
								}
							}
						}
					} else {
						cmd.setMode(DataMiningConstants.EXECUTION_TYPE_MANUAL);
					}
				}
			}
		}
		return outputResult;
	}

	@GET
	@Path("/getVariables/{command}")
	@Produces("text/html; charset=UTF-8")
	public String getVariables(@PathParam("command") String commandName) {
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
						List variables = cmd.getVariables();
						variablesJson = serializeList(variables);
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
	@Path("/setVariables/{command}")
	@Produces("text/html; charset=UTF-8")
	public String setVariables(@Context HttpServletRequest request, @PathParam("command") String commandName) {
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
							List variables = cmd.getVariables();
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
		logger.debug("OUT");
		return getJsonSuccess();
	}
}
