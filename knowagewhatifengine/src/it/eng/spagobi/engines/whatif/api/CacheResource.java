/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
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

import com.eyeq.pivot4j.PivotModel;

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
