/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 *
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

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
		ModelConfig config = ei.getModelConfig();
		ModelConfig modelconfig;

		String modelConfig;

		try {
			modelConfig = RestUtilities.readBody(getServletRequest());

			modelconfig = (ModelConfig) deserialize(modelConfig, ModelConfig.class);

			config.setShowParentMembers(modelconfig.getShowParentMembers());
			config.setHideSpans(modelconfig.getHideSpans());
			config.setDrillType(modelconfig.getDrillType());
			config.setShowProperties(modelconfig.getShowProperties());
			config.setSuppressEmpty(modelconfig.getSuppressEmpty());
		} catch (SerializationException e) {
			logger.error(e.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}

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
