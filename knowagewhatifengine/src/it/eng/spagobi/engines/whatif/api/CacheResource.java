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
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;

@Path("1.0/cache")
public class CacheResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(CacheResource.class);

	@POST
	@Produces("text/html; charset=UTF-8")
	public String flushCache(@Context HttpServletRequest request) {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		OlapDataSource olapDataSource = ei.getOlapDataSource();

		CacheManager.flushCache(olapDataSource);

		logger.debug("Cleaning the cache and restoring the model");
		CacheManager.flushCache(olapDataSource);
		String mdx = ei.getPivotModel().getCurrentMdx();
		ei.getPivotModel().setMdx(mdx);
		ei.getPivotModel().initialize();
		logger.debug("Finish to clean the cache and restoring the model");

		PivotModel model = ei.getPivotModel();
		String table = renderModel(model);
		logger.debug("OUT");
		return table;
	}

}
