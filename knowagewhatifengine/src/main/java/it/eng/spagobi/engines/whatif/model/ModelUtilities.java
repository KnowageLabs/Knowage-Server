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
package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import org.apache.log4j.Logger;
import org.pivot4j.PivotModel;

public class ModelUtilities {
	public static transient Logger logger = Logger.getLogger(ModelUtilities.class);

	public void reloadModel(WhatIfEngineInstance instance, PivotModel model) {
		logger.debug("IN");
		SpagoBIPivotModel modelWrapper = (SpagoBIPivotModel) model;

		logger.debug("Cleaning the cache and restoring the model");
		CacheManager.flushCache(instance.getOlapDataSource());
		String mdx = modelWrapper.getCurrentMdx();
		modelWrapper.setMdx(mdx);
		modelWrapper.initialize();
		// force query riexecution
		modelWrapper.getCellSet();
		logger.debug("Finish to clean the cache and restoring the model");

		logger.debug("OUT");
	}

}
