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

package it.eng.spagobi.engines.whatif;

import it.eng.spagobi.engines.whatif.model.PivotJsonSerializer;
import it.eng.spagobi.engines.whatif.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Map;

import org.apache.log4j.Logger;

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
		try {
			whatIfEngineInstance = new WhatIfEngineInstance(template, env);
			initSerializers();
		} catch (Exception e) {
			logger.error("OUT", e);
			throw new SpagoBIEngineRuntimeException("error", e);

		}

		return whatIfEngineInstance;
	}

	private static void initSerializers() {
		PivotJsonSerializer pjs = new PivotJsonSerializer();
		SerializationManager.registerSerializer(pjs.getFormat(), pjs);
	}
}
