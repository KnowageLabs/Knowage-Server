/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif;

import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.PivotJsonSerializer;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;

import java.util.Map;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class WhatIfEngine {

	private static WhatIfEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(WhatIfEngine.class);

	// init engine
	static {
		engineConfig = WhatIfEngineConfig.getInstance();
	}

	public static WhatIfEngineConfig getConfig() {
		return engineConfig;
	}

	/**
	 * Creates the instance.
	 * 
	 * @param template
	 *            the template
	 * @param env
	 *            the env
	 * 
	 * @return the WhatIf engine instance
	 */
	public static WhatIfEngineInstance createInstance(Object template, Map env) {
		WhatIfEngineInstance whatIfEngineInstance = null;
		logger.debug("IN");
		whatIfEngineInstance = new WhatIfEngineInstance(template, env);
		initSerializers(whatIfEngineInstance.getOlapConnection(), whatIfEngineInstance.getModelConfig());
		logger.debug("OUT");

		return whatIfEngineInstance;
	}

	private static void initSerializers(OlapConnection connection, ModelConfig config) {
		PivotJsonSerializer pjs = new PivotJsonSerializer(connection, config);
		SerializationManager.registerSerializer(pjs.getFormat(), pjs);
	}
}
