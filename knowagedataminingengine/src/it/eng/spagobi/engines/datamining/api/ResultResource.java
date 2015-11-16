/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.compute.DataMiningUtils;
import it.eng.spagobi.engines.datamining.compute.DataMiningExecutor;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.serializer.SerializationException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

@Path("/1.0/result")
public class ResultResource extends AbstractDataMiningEngineService {
	public static transient Logger logger = Logger.getLogger(ResultResource.class);

	/**
	 * Service to get Result
	 * 
	 * @return
	 * 
	 */
	@GET
	@Path("/{command}/{output}/{rerun}")
	@Produces("text/html; charset=UTF-8")
	public String getResult(@PathParam("command") String commandName, @PathParam("output") String outputName, @PathParam("rerun") Boolean rerun) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		String outputOfExecution = null;

		DataMiningExecutor executor = new DataMiningExecutor(dataMiningEngineInstance, getUserProfile());

		List<DataMiningCommand> commands = null;
		if (dataMiningEngineInstance.getCommands() != null && !dataMiningEngineInstance.getCommands().isEmpty()) {
			commands = dataMiningEngineInstance.getCommands();
			for (Iterator it = commands.iterator(); it.hasNext();) {
				DataMiningCommand cmd = (DataMiningCommand) it.next();
				if (cmd.getName().equals(commandName)) {
					List<Output> outputs = cmd.getOutputs();
					if (outputs != null && !outputs.isEmpty()) {
						for (Iterator it2 = outputs.iterator(); it2.hasNext();) {
							Output output = (Output) it2.next();
							if (output.getOutputName().equals(outputName)) {
								// and starts execution!
								try {
									HashMap params = (HashMap) dataMiningEngineInstance.getAnalyticalDrivers();

									DataMiningResult result = executor.execute(params, cmd, output, getUserProfile(), rerun);
									outputOfExecution = serialize(result);
								} catch (SerializationException e) {
									logger.error("Error serializing the result", e);
									throw new SpagoBIEngineRuntimeException("Error serializing the result", e);
								} catch (IOException e) {
									logger.error("Error executing script", e);
									throw new SpagoBIEngineRuntimeException("Error executing script", e);
								} catch (Exception e) {
									logger.error("Error in script code generation", e);
									throw new SpagoBIEngineRuntimeException("Error in script code generation", e);
								}
							}
						}
					}

				}
			}
		}

		if (!isNullOrEmpty(outputOfExecution)) {
			logger.debug("Returning result");
		} else {
			logger.debug("No result found");
		}

		logger.debug("OUT");
		return outputOfExecution;

	}

	/**
	 * Checks whether the result panel has to be displayed ad first execution
	 * 
	 * @throws IOException
	 * 
	 */
	@GET
	@Path("/needsResultAtForstExec")
	@Produces("text/html; charset=UTF-8")
	public String needsResultAtForstExec() throws IOException {
		logger.debug("IN");
		Boolean resNeeded = true;
		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();

		resNeeded = DataMiningUtils.areDatasetsProvided(dataMiningEngineInstance, getUserProfile());
		if (!resNeeded) {
			return getJsonKo();
		}
		logger.debug("OUT");
		return getJsonOk();

	}

}
