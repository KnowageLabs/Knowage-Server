/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.rosuda.JRI.Rengine;

public class CommandsExecutor {
	private Rengine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;
	static private Logger logger = Logger.getLogger(CommandsExecutor.class);

	public CommandsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public Rengine getRe() {
		return re;
	}

	public void setRe(Rengine re) {
		this.re = re;
	}

	/**
	 * Method called to evaluate all the command prepare the result based on the
	 * script each command needs, for the output with mode = auto. Both command
	 * and output must have mode=auto to be executed and displayed
	 * 
	 * @param dataminingInstance
	 * @return the command for the auto output mode
	 */
	protected DataMiningCommand detectCommandOuputToExecute() {
		logger.debug("IN");
		if (dataminingInstance.getCommands() != null && !dataminingInstance.getCommands().isEmpty()) {
			for (Iterator it = dataminingInstance.getCommands().iterator(); it.hasNext();) {
				DataMiningCommand command = (DataMiningCommand) it.next();

				if (command.getOutputs() != null && !command.getOutputs().isEmpty()) {
					for (Iterator it2 = command.getOutputs().iterator(); it2.hasNext();) {
						Output output = (Output) it2.next();
						if (output.getOutputMode().equals(DataMiningConstants.EXECUTION_TYPE_AUTO)) {
							return command;

						}
					}
				}

			}
		}
		logger.debug("IN");
		return null;
	}

}
