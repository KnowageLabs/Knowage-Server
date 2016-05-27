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
package it.eng.knowage.engines.svgviewer;

import it.eng.spago.error.EMFUserError;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class SvgViewerEngine {

	private static SvgViewerEngineConfig engineConfig;

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(SvgViewerEngine.class);

	// init engine
	static {
		engineConfig = SvgViewerEngineConfig.getInstance();
	}

	public static SvgViewerEngineConfig getConfig() {
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
	 * @return the svg viewer engine instance
	 * @throws EMFUserError
	 */
	public static SvgViewerEngineInstance createInstance(String template, Map env) throws EMFUserError {
		SvgViewerEngineInstance svgViewerEngineInstance = null;
		logger.debug("IN");
		// TO-DO ceck if document is crossable

		/*
		 * ICrossNavigationDAO crossDao = DAOFactory.getCrossNavigationDAO(); crossDao.setUserProfile((IEngUserProfile) env.get("ENV_USER_PROFILE")); boolean
		 * isCross = false; if (crossDao.documentIsCrossable(env.get("DOCUMENT_NAME").toString())) { isCross = true; } try { JSONObject templ = new
		 * JSONObject(template); templ.put("crossNavigation", isCross); templ.put("crossNavigationMultiselect", isCross); template = templ.toString(); } catch
		 * (JSONException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		svgViewerEngineInstance = new SvgViewerEngineInstance(template, env);
		logger.debug("OUT");
		return svgViewerEngineInstance;
	}
}
