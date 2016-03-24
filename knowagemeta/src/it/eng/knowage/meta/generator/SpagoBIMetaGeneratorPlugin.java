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
package it.eng.knowage.meta.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIMetaGeneratorPlugin {

	public static final String PLUGIN_ID = "it.eng.knowage.meta.generator"; //$NON-NLS-1$

	private static Logger logger = LoggerFactory.getLogger(SpagoBIMetaGeneratorPlugin.class);

	static {
		logger.debug("Plugin [{}] succesfully loaded", PLUGIN_ID);
	}

	private SpagoBIMetaGeneratorPlugin() {
		// super(PLUGIN_ID);
	}

	private static SpagoBIMetaGeneratorPlugin instance;

	public static SpagoBIMetaGeneratorPlugin getInstance() {
		if (instance == null)
			instance = new SpagoBIMetaGeneratorPlugin();
		return instance;
	}

}
