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
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;

import java.util.Iterator;

import org.apache.log4j.Logger;

public class RCommandsExecutor {
	// private REngine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;
	static private Logger logger = Logger.getLogger(RCommandsExecutor.class);

	public RCommandsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	/*
	 * public REngine getRe() { return re; }
	 * 
	 * public void setRe(REngine re) { this.re = re; }
	 */

	/**
	 * Method called to evaluate all the command prepare the result based on the script each command needs, for the output with mode = auto. Both command and
	 * output must have mode=auto to be executed and displayed
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
