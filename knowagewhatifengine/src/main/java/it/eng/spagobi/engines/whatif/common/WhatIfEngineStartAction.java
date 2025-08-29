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
package it.eng.spagobi.engines.whatif.common;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/olap/startwhatif")
@ManageAuthorization
public class WhatIfEngineStartAction extends WhatIfEngineAbstractStartAction {

	// INPUT PARAMETERS
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String COUNTRY = "SBI_COUNTRY";

	// OUTPUT PARAMETERS

	/** Logger component. */
	public static Logger logger = Logger.getLogger(WhatIfEngineStartAction.class);

	@GET
	@Path("/")
	@Produces("text/html")
	public void startWhatIfActionOlapGet() {
        startAction();
	}

	@POST
	@Path("/")
	@Produces("text/html")
	public void startWhatIfActionOlapPost() {
        startAction();
	}

	/**
	 * @return
	 *
	 */
	private void startAction() {
		logger.debug("Starting WHATIF");
        startAction(true);
	}

}