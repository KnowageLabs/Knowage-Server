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
package it.eng.spagobi.engines.datamining;

import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.serializer.GenericSerializer;
import it.eng.spagobi.engines.datamining.serializer.SerializationManager;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Monica Franceschini
 */
public class DataMiningEngine {

	private static DataMiningEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(DataMiningEngine.class);

	// init engine
	static {
		engineConfig = DataMiningEngineConfig.getInstance();
	}

	public static DataMiningEngineConfig getConfig() {
		return engineConfig;
	}

	public static DataMiningEngineInstance createInstance(Object template, Map env) {
		DataMiningEngineInstance dataMiningEngineInstance = null;
		logger.debug("IN");
		if (template instanceof DataMiningTemplate) {
			dataMiningEngineInstance = new DataMiningEngineInstance((DataMiningTemplate) template, env);
		} else {
			dataMiningEngineInstance = new DataMiningEngineInstance(template, env);
		}

		SerializationManager.registerSerializer(AbstractDataMiningEngineService.OUTPUTFORMAT_JSONHTML, new GenericSerializer());
		logger.debug("OUT");
		return dataMiningEngineInstance;
	}
}
