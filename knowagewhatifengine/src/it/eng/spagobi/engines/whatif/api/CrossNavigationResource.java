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

package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.crossnavigation.CrossNavigationManager;
import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.pivot4j.PivotModel;

@Path("/1.0/crossnavigation")
public class CrossNavigationResource extends AbstractWhatIfEngineService {
	public static transient Logger logger = Logger.getLogger(CrossNavigationResource.class);

	/**
	 * Service to set targets in cells
	 * 
	 * @return the rendered pivot table
	 */
	@GET
	@Path("/initialize")
	@Produces("text/html; charset=UTF-8")
	public String initialize() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		SpagoBIPivotModel modelWrapper = (SpagoBIPivotModel) model;
		try {
			SpagoBICrossNavigationConfig crossNavigation = modelWrapper.getCrossNavigation();
			if (crossNavigation.isButtonClicked())
				crossNavigation.setButtonClicked(false);
			else
				crossNavigation.setButtonClicked(true);
			crossNavigation.setModelStatus(ei.getModelConfig().getStatus());
			if (crossNavigation != null) {
				ei.getModelConfig().setCrossNavigation(crossNavigation);
			}
		} catch (Exception e) {
			logger.error("Error cross navigation targets titles initialization ");
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
		}

		logger.debug("OUT");
		String table = renderModel(model);
		return table;
	}

	/**
	 * Service to create the js function parent.execCrossNavigation with
	 * parameters
	 *
	 * @return the js function
	 */
	@POST
	@Path("/getCrossNavigationUrl/{targetIndex}/{ordinal}")
	@Produces("text/html; charset=UTF-8")
	public String getCrossNavigationUrl(@PathParam("targetIndex") int targetIndex, @PathParam("ordinal") int ordinal) {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		String jsFunction = new String();
		try {
			SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) model.getCellSet();
			SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(ordinal);
			jsFunction = CrossNavigationManager.buildCrossNavigationUrl(targetIndex, cellWrapper, ei);
		} catch (Exception e) {
			logger.error("Error cross navigation js function creation ");
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
		}
		String table = renderModel(model);
		logger.debug("OUT");
		return jsFunction;
	}
}
