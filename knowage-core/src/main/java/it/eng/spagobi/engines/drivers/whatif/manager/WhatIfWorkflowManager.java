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
package it.eng.spagobi.engines.drivers.whatif.manager;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.whatif.dao.IWhatifWorkflowDAO;

import org.apache.log4j.Logger;

public class WhatIfWorkflowManager {

	private static transient Logger logger = Logger.getLogger(WhatIfWorkflowManager.class);
	
	 public String getActiveUser(int modelId) throws EMFUserError {
		 logger.debug("Loading active user for model "+modelId);
		 IWhatifWorkflowDAO iwfd = DAOFactory.getWhatifWorkflowDAO();
		 String user = iwfd.getActiveUserIdByModel(modelId);
		 logger.debug("Loaded active user "+user);
		 return user;
	 };
	 
	 public void goNextUser(int modelId) throws EMFUserError {
		 logger.debug("pass control to next user for model "+modelId);
		 IWhatifWorkflowDAO iwfd = DAOFactory.getWhatifWorkflowDAO();
		 iwfd.goNextUserByModel(modelId);
		 logger.debug("Done passing controll to next user for model "+modelId);
	 };


}
