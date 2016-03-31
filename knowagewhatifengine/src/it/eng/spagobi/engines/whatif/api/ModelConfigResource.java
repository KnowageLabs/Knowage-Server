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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/modelconfig")
public class ModelConfigResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(ModelConfigResource.class);

	/**
	 * Sets the model configuration defined by the toolbar
	 *
	 * @return the html table representing the cellset
	 */
	@POST
	@Produces("text/html; charset=UTF-8")
	public String setModelConfig() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig config = ei.getModelConfig();
		ModelConfig modelconfig;
		model.removeSubset();
		String modelConfig;

		try {
			modelConfig = RestUtilities.readBody(getServletRequest());

			modelconfig = (ModelConfig) deserialize(modelConfig, ModelConfig.class);

			config.setShowParentMembers(modelconfig.getShowParentMembers());
			config.setHideSpans(modelconfig.getHideSpans());
			config.setDrillType(modelconfig.getDrillType());
			config.setShowProperties(modelconfig.getShowProperties());
			config.setShowCompactProperties(modelconfig.getShowCompactProperties());
			config.setSuppressEmpty(modelconfig.getSuppressEmpty());
			config.setEnableDrillThrough(modelconfig.getEnableDrillThrough());
			config.setStartRow(modelconfig.getStartRow());
			config.setRowsSet(modelconfig.getRowsSet());
			config.setRowCount(modelconfig.getRowCount());
			config.setStartColumn(modelconfig.getStartColumn());
			config.setColumnSet(modelconfig.getColumnSet());
			config.setColumnCount(modelconfig.getColumnCount());
		} catch (SerializationException e) {
			logger.error(e.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
		model.setSubset(config.getStartRow(), config.getStartColumn(), config.getRowsSet());
		String table = renderModel(ei.getPivotModel());
		logger.debug("OUT");
		return table;
	}

	/**
	 * Gets the drill type
	 *
	 * @return the drill type
	 */
	@GET
	@Path("/")
	public String getDrillType() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ModelConfig config = ei.getModelConfig();

		String configSerialized = null;
		try {
			configSerialized = serialize(config);
		} catch (SerializationException e) {
			logger.error("Error serializing the model config", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the model config", e);
		}

		logger.debug("OUT");
		return configSerialized;
	}

}
