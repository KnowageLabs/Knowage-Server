/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2009 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.engines.datamining;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Monica Franceschini
 */
public class DataMiningEngineConfig {

	private EnginConf engineConfig;

	private Map<String, List> includes;
	private Set<String> enabledIncludes;

	private static transient Logger logger = Logger.getLogger(DataMiningEngineConfig.class);

	// -- singleton pattern --------------------------------------------
	private static DataMiningEngineConfig instance;

	public static DataMiningEngineConfig getInstance() {
		if (instance == null) {
			instance = new DataMiningEngineConfig();
		}
		return instance;
	}

	private DataMiningEngineConfig() {
		setEngineConfig(EnginConf.getInstance());
	}

	// -- singleton pattern --------------------------------------------

	// -- CORE SETTINGS ACCESSOR Methods---------------------------------

	public List getIncludes() {
		List results;

		// includes = null;
		if (includes == null) {
			initIncludes();
		}

		results = new ArrayList();
		Iterator<String> it = enabledIncludes.iterator();
		while (it.hasNext()) {
			String includeName = it.next();
			List urls = includes.get(includeName);
			results.addAll(urls);
			logger.debug("Added [" + urls.size() + "] for include [" + includeName + "]");
		}

		return results;
	}

	// -- PARSE Methods -------------------------------------------------

	private final static String INCLUDES_TAG = "INCLUDES";
	private final static String INCLUDE_TAG = "INCLUDE";
	private final static String URL_TAG = "URL";

	public void initIncludes() {
		SourceBean includesSB;
		List includeSBList;
		SourceBean includeSB;
		List urlSBList;
		SourceBean urlSB;

		includes = new HashMap();
		enabledIncludes = new LinkedHashSet();

		includesSB = (SourceBean) getConfigSourceBean().getAttribute(INCLUDES_TAG);
		if (includesSB == null) {
			logger.debug("Tag [" + INCLUDES_TAG + "] not specifeid in [engine-config.xml] file");
			return;
		}

		includeSBList = includesSB.getAttributeAsList(INCLUDE_TAG);
		if (includeSBList == null || includeSBList.size() == 0) {
			logger.debug("Tag [" + INCLUDES_TAG + "] does not contains any [" + INCLUDE_TAG + "] tag");
			return;
		}

		for (int i = 0; i < includeSBList.size(); i++) {
			includeSB = (SourceBean) includeSBList.get(i);
			String name = (String) includeSB.getAttribute("name");
			String bydefault = (String) includeSB.getAttribute("default");

			logger.debug("Include [" + name + "]: [" + bydefault + "]");

			List urls = new ArrayList();

			urlSBList = includeSB.getAttributeAsList(URL_TAG);
			for (int j = 0; j < urlSBList.size(); j++) {
				urlSB = (SourceBean) urlSBList.get(j);
				String url = urlSB.getCharacters();
				urls.add(url);
				logger.debug("Url [" + name + "] added to include list");
			}

			includes.put(name, urls);
			if (bydefault.equalsIgnoreCase("enabled")) {
				enabledIncludes.add(name);
			}
		}
	}

	// -- ACCESS Methods -----------------------------------------------
	public EnginConf getEngineConfig() {
		return engineConfig;
	}

	private void setEngineConfig(EnginConf engineConfig) {
		this.engineConfig = engineConfig;
	}

	public SourceBean getConfigSourceBean() {
		return getEngineConfig().getConfig();
	}
}
