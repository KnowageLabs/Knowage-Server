/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmDefinition;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olap4j.OlapDataSource;

import sun.misc.BASE64Encoder;

import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class WhatIfEngineConfig {

	private static final BASE64Encoder ENCODER = new BASE64Encoder();

	private EnginConf engineConfig;

	private Map<String, List> includes;
	private Set<String> enabledIncludes;
	private Map<String, AllocationAlgorithmDefinition> algorithmsDefinitionMap;
	private static transient Logger logger = Logger.getLogger(WhatIfEngineConfig.class);
	private static final String PROPORTIONAL_ALGORITHM_CONF = "proportionalAlgorithmPersistQueryWhereClause";

	// -- singleton pattern --------------------------------------------
	private static WhatIfEngineConfig instance;

	public static WhatIfEngineConfig getInstance() {
		if (instance == null) {
			instance = new WhatIfEngineConfig();
		}
		return instance;
	}

	private WhatIfEngineConfig() {
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
	private final static String WRITEBACK_TAG = "WRITEBACK";
	private final static String ALGORITHMS_TAG = "ALGORITHMS";
	private final static String ALGORITHM_TAG = "ALGORITHM";
	private final static String NAME_ATTRIBUTE = "name";
	private final static String CLASS_ATTRIBUTE = "class";
	private final static String INMEMORY_ATTRIBUTE = "inMemory";
	private final static String PERSISTENT_ATTRIBUTE = "persistent";
	private final static String XMLA_URL = "url";
	private final static String DEFAULT_ATTRIBUTE = "default";

	public String getTemplateFilePath() {
		String templatePath = "";
		SourceBean sb = (SourceBean) getConfigSourceBean().getAttribute("TEMPLATE");
		if (sb != null) {
			templatePath = sb.getCharacters();
		}
		return templatePath;
	}

	public OlapDataSource getOlapDataSource(IDataSource ds, String reference, WhatIfTemplate template, IEngUserProfile profile, Locale locale, Map env) {

		String connectionString = null;
		Properties connectionProps = new Properties();

		if (template.getXmlaServerProperties() != null && !template.getXmlaServerProperties().isEmpty()) {

			logger.debug("The datasource is XMLA");
			try {
				Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
			} catch (ClassNotFoundException e) {
				logger.error("Error loading the class org.olap4j.driver.xmla.XmlaOlap4jDriver");
			}

			String url = template.getXmlaServerProperties().get(XMLA_URL);

			Iterator<String> keysIter = template.getXmlaServerProperties().keySet().iterator();

			while (keysIter.hasNext()) {
				String key = keysIter.next();
				if (!key.equals(XMLA_URL)) {
					connectionProps.put(key, template.getXmlaServerProperties().get(key));
				}

			}
			connectionString = "jdbc:xmla:Server=" + url;
			connectionProps.put("Locale", locale.toString());

		} else {
			logger.debug("The datasource is jdbc");
			if (ds.checkIsJndi()) {
				connectionProps.put("DataSource", ds.getJndi());
				connectionString = "jdbc:mondrian:DataSource=" + ds.getJndi();
			} else {
				connectionProps.put("JdbcUser", ds.getUser());
				connectionProps.put("JdbcPassword", ds.getPwd());
				connectionProps.put("JdbcDrivers", ds.getDriver());
				connectionString = "jdbc:mondrian:Jdbc=" + ds.getUrlConnection();
			}
			connectionProps.put("Catalog", reference);
			connectionProps.put("Provider", "Mondrian");
			connectionProps.put("Locale", locale.toString());

		}

		logger.debug("The connection string is " + connectionString);

		this.defineSchemaProcessorProperties(connectionProps, template, profile, env);

		OlapDataSource olapDataSource = new SimpleOlapDataSource();

		((SimpleOlapDataSource) olapDataSource).setConnectionString(connectionString);
		((SimpleOlapDataSource) olapDataSource).setConnectionProperties(connectionProps);

		return olapDataSource;
	}

	private void defineSchemaProcessorProperties(Properties connectionProps, WhatIfTemplate template, IEngUserProfile profile, Map env) {
		List<String> userProfileAttributes = template.getProfilingUserAttributes();
		// SpagoBIFilterDynamicSchemaProcessor extends
		// LocalizingDynamicSchemaProcessor, that is responsible for i18n,
		// therefore we put it
		// in the connection properties anyway
		connectionProps.put("DynamicSchemaProcessor", "it.eng.spagobi.engines.whatif.schema.SpagoBIFilterDynamicSchemaProcessor");
		if (!userProfileAttributes.isEmpty()) {
			// adds profile attributes values
			logger.debug("Template contains data access restriction based on user's attributes");
			Iterator<String> it = userProfileAttributes.iterator();
			while (it.hasNext()) {
				String attributeName = it.next();
				String value = this.getUserProfileEncodedValue(attributeName, profile);
				logger.debug("Adding profile attribute [" + attributeName + "]" + " with encoded value [" + value + "]");
				connectionProps.put(attributeName, value);
			}
		} else {
			logger.debug("Template does not contain any data access restriction based on user's attributes");
		}
		if (!env.isEmpty()) {
			// adds parameters values
			logger.debug("Template contains data access restriction based on user's attributes");
			Iterator<String> it = env.keySet().iterator();
			while (it.hasNext()) {
				String attributeName = it.next();
				Object value = env.get(attributeName);

				if (value == null)
					continue;

				String cl = value.getClass().getName();
				if (cl.contains("String") && !"".equals(value)) {
					// Adds String types
					logger.debug("Adding environment value [" + attributeName + "]" + " with encoded value [" + value + "]");
					String valueBase64 = encodeValue(value.toString());
					connectionProps.put(attributeName, valueBase64);
				} else if (cl.contains("[Ljava.lang.Object")) {
					// Adds list of String (uses implicit cast of DBs) for multiple values.
					Object[] arrList = (Object[]) value;
					String totalString = "";
					String quote = "'";
					for (int i = 0; i < arrList.length; i++) {
						String el = (String) arrList[i];
						totalString += quote + el + quote;
						totalString += (i < arrList.length - 1) ? "," : "";
					}
					String valueBase64 = encodeValue(totalString);
					logger.debug("Adding environment value [" + attributeName + "]" + " with encoded value [" + valueBase64 + "]");
					connectionProps.put(attributeName, valueBase64);
				}
			}
		} else {
			logger.debug("Template does not contain any data access restriction based on user's attributes");
		}
	}

	private String getUserProfileEncodedValue(String attributeName, IEngUserProfile profile) {
		String value;
		try {
			value = profile.getUserAttribute(attributeName) != null ? profile.getUserAttribute(attributeName).toString() : null;
		} catch (EMFInternalError e) {
			throw new SpagoBIEngineRuntimeException("Error while retrieving user profile [" + attributeName + "]", e);
		}
		logger.debug("Found profile attribute [" + attributeName + "]" + " with value [" + value + "]");

		// encoding value in Base64
		String valueBase64 = encodeValue(value);
		// if (value != null) {
		// try {
		// valueBase64 = ENCODER.encode(value.getBytes("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error("UTF-8 encoding not supported!!!!!", e);
		// valueBase64 = ENCODER.encode(value.getBytes());
		// }
		// }
		// logger.debug("Attribute value in Base64 encoding is " + valueBase64);

		return valueBase64;
	}

	private String encodeValue(String v) {
		// encoding value in Base64
		String valueBase64 = null;
		if (v != null) {
			try {
				valueBase64 = ENCODER.encode(v.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("UTF-8 encoding not supported!!!!!", e);
				valueBase64 = ENCODER.encode(v.getBytes());
			}
		}
		return valueBase64;
	}

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

	public boolean getProportionalAlghorithmConf() {
		SourceBean sb = (SourceBean) getConfigSourceBean().getAttribute(WRITEBACK_TAG);
		if (sb != null) {
			String conf = (String) sb.getAttribute(PROPORTIONAL_ALGORITHM_CONF);
			if (conf != null && conf.length() > 0) {
				return conf.equals("in");
			}
		}
		return true;
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

	public String getEngineResourcePath() {
		String path = null;
		if (getEngineConfig().getResourcePath() != null) {
			path = getEngineConfig().getResourcePath() + System.getProperty("file.separator");
		} else {
			path = ConfigSingleton.getRootPath() + System.getProperty("file.separator") + "resources";
		}
		return path;
	}

	/**
	 * Reads the configuration file and gets the list of allocation algorithms
	 * 
	 * @return
	 */
	public Map<String, AllocationAlgorithmDefinition> getAllocationAlgorithms() {

		if (algorithmsDefinitionMap == null) {
			SourceBean algorithmsBean;
			List<SourceBean> algorithmsListBean;

			algorithmsDefinitionMap = new HashMap<String, AllocationAlgorithmDefinition>();

			SourceBean writeBackBean = (SourceBean) getConfigSourceBean().getAttribute(WRITEBACK_TAG);
			if (writeBackBean != null) {
				algorithmsBean = (SourceBean) writeBackBean.getAttribute(ALGORITHMS_TAG);
				if (algorithmsBean != null) {
					algorithmsListBean = algorithmsBean.getAttributeAsList(ALGORITHM_TAG);
					if (algorithmsListBean != null) {
						for (int i = 0; i < algorithmsListBean.size(); i++) {
							SourceBean algorithmBean = algorithmsListBean.get(i);
							String name = (String) algorithmBean.getAttribute(NAME_ATTRIBUTE);
							String className = (String) algorithmBean.getAttribute(CLASS_ATTRIBUTE);
							String deafultString = (String) algorithmBean.getAttribute(DEFAULT_ATTRIBUTE);

							Boolean defaultBoolean = false;
							if (deafultString != null) {
								try {
									defaultBoolean = new Boolean(deafultString);
								} catch (Exception e) {
									logger.error("persistent attribute is a boolean, so the admissible values are [true, false] not [" + deafultString + "]");
								}
							}

							algorithmsDefinitionMap.put(name, new AllocationAlgorithmDefinition(name, className, defaultBoolean));

						}
					}
				}
			}
		}
		return algorithmsDefinitionMap;
	}
}
