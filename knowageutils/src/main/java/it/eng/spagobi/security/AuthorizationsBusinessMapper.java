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
package it.eng.spagobi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;

/**
 * @author Bernabei Angelo
 *
 */
public class AuthorizationsBusinessMapper {
	private static Logger logger = Logger.getLogger(AuthorizationsBusinessMapper.class);

	private static AuthorizationsBusinessMapper instance = null;
	private HashMap _mapActions = null;
	private HashMap _mapPages = null;
	private HashMap _mapPagesModules = null;

	/**
	 * Instantiates a new authorizations business mapper.
	 */
	public AuthorizationsBusinessMapper() {
		// logger.debug("IN");
		ConfigSingleton config = ConfigSingleton.getInstance();
		_mapActions = new HashMap();
		List actions = config.getAttributeAsList("BUSINESS_MAP.MAP_ACTIONS");
		Iterator it = actions.iterator();
		while (it.hasNext()) {
			SourceBean mapActions = (SourceBean) it.next();
			List actionsList = mapActions.getAttributeAsList("MAP_ACTION");
			Iterator actionListIt = actionsList.iterator();
			while (actionListIt.hasNext()) {
				SourceBean mapAction = (SourceBean) actionListIt.next();
				String actionName = (String) mapAction.getAttribute("actionName");
				String businessProcessName = (String) mapAction.getAttribute("businessProcess");
				String actStr = "ACTION[" + actionName + "]";
				// logger.debug("PUT:actStr"+actStr);
				if (_mapActions.get(actStr.toUpperCase()) != null) {
					// Action already present, add businessProcess to the corresponding List
					List<String> businessProcessNames = (List<String>) _mapActions.get(actStr.toUpperCase());
					businessProcessNames.add(businessProcessName);
					_mapActions.put(actStr.toUpperCase(), businessProcessNames);
				} else {
					// Action not present, create a new List for this Action and put the businessProcess
					List<String> businessProcessNames = new ArrayList<String>();
					businessProcessNames.add(businessProcessName);
					_mapActions.put(actStr.toUpperCase(), businessProcessNames);
				}
			}
		}
		_mapPages = new HashMap();
		_mapPagesModules = new HashMap();
		List pageModules = config.getAttributeAsList("BUSINESS_MAP.MAP_PAGE_MODULES");
		it = pageModules.iterator();
		while (it.hasNext()) {
			SourceBean mapPageModules = (SourceBean) it.next();
			List mapPageModuleList = mapPageModules.getAttributeAsList("MAP_PAGE_MODULE");
			Iterator mapPageModuleListIt = mapPageModuleList.iterator();
			while (mapPageModuleListIt.hasNext()) {
				SourceBean mapModules = (SourceBean) mapPageModuleListIt.next();
				String pageName = (String) mapModules.getAttribute("pageName");
				String moduleName = (String) mapModules.getAttribute("moduleName");
				String businessProcessName = (String) mapModules.getAttribute("businessProcess");
				if (moduleName == null) {
					String pgStr = "PAGE[" + pageName + "]";
					// logger.debug("PUT:pgStr"+pgStr);
					_mapPages.put(pgStr.toUpperCase(), businessProcessName);
				} else {
					String pgMdlStr = "PAGE[" + pageName + "]MODULE[" + moduleName + "]";
					// logger.debug("PUT:pgMdlStr"+pgMdlStr);
					_mapPagesModules.put(pgMdlStr.toUpperCase(), businessProcessName);
				}
			}
		}
		// logger.debug("OUT");
	}

	/**
	 * Gets the single instance of AuthorizationsBusinessMapper.
	 *
	 * @return single instance of AuthorizationsBusinessMapper
	 */
	public static AuthorizationsBusinessMapper getInstance() {
		if (instance == null) {
			synchronized (AuthorizationsBusinessMapper.class) {
				if (instance == null) {
					try {
						instance = new AuthorizationsBusinessMapper();
					} catch (Exception ex) {
						logger.error("Exception", ex);
					}
				}
			}
		}
		return instance;
	}

	/**
	 * Map action to business process.
	 *
	 * @param actionName
	 *            the action name
	 *
	 * @return the string
	 */
	public List<String> mapActionToBusinessProcess(String actionName) {
		// logger.debug("IN. actionName="+actionName);
		String actStr = "ACTION[" + actionName + "]";
		List<String> businessProcessNames = (List<String>) _mapActions.get(actStr.toUpperCase());
		if (businessProcessNames == null) {
			logger.warn("mapping per action [" + actionName + "] non trovato");
		}
		// logger.debug("OUT,businessProcessName="+businessProcessName);
		return businessProcessNames;
	}

	/**
	 * Map page module to business process.
	 *
	 * @param pageName
	 *            the page name
	 * @param moduleName
	 *            the module name
	 *
	 * @return the string
	 */
	public String mapPageModuleToBusinessProcess(String pageName, String moduleName) {
		// logger.debug("IN. pageName="+pageName+" moduleName="+moduleName);
		String pgMdlStr = "PAGE[" + pageName + "]MODULE[" + moduleName + "]";
		String businessProcessName = (String) _mapPagesModules.get(pgMdlStr.toUpperCase());
		if (businessProcessName == null) {
			logger.warn("mapping per page [" + pageName + "] e module [" + moduleName + "] non trovato");
			String pgStr = "PAGE[" + pageName + "]";
			businessProcessName = (String) _mapPages.get(pgStr.toUpperCase());
			if (businessProcessName == null) {
				logger.warn(" mapping per page [" + pageName + "] non trovato");
			}
		}
		// logger.debug("OUT,businessProcessName="+businessProcessName);
		return businessProcessName;
	}

	public static void main(String[] args) {
		logger.debug(Pattern.matches("SERVICE\\[/DATASETS.*\\]", "SERVICE[/DATASETS]"));

	}
}
