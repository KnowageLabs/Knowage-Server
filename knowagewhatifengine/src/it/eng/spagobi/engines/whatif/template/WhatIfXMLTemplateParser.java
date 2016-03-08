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
package it.eng.spagobi.engines.whatif.template;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.crossnavigation.TargetClickable;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.writeback4j.SbiAlias;
import it.eng.spagobi.writeback4j.SbiAliases;
import it.eng.spagobi.writeback4j.SbiScenario;
import it.eng.spagobi.writeback4j.SbiScenarioVariable;
import it.eng.spagobi.writeback4j.WriteBackEditConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WhatIfXMLTemplateParser implements IWhatIfTemplateParser {

	public static String TAG_ROOT = "OLAP";
	public static String TAG_CUBE = "CUBE";
	public static String TAG_WRITEBACK = "CUBE";
	public static String TAG_SCENARIO = "SCENARIO";
	public static String TAG_ALIASES = "ALIASES";
	public static String TAG_MDX_QUERY = "MDXquery";
	public static String TAG_PARAMETER = "parameter";
	public static String TAG_MDX_MONDRIAN_QUERY = "MDXMondrianQuery";
	public static String PROP_SCHEMA_REFERENCE = "reference";
	public final static String MEASURE_TAG = "MEASURE";
	public final static String WRITEBACK_TAG = "WRITEBACK";
	public final static String EDIT_CUBE_ATTRIBUTE = "editCube";
	public final static String INITIAL_VERSION_ATTRIBUTE = "initialVersion";
	public static String PROP_PARAMETER_NAME = "name";
	public static String PROP_PARAMETER_ALIAS = "as";
	public static String SCENARIO_VARIABLES_TAG = "SCENARIO_VARIABLES";
	public static String VARIABLE_TAG = "VARIABLE";
	public static String DIMENSION_TAG = "DIMENSION";
	public static String HIERARCHY_TAG = "HIERARCHY";
	public static String ALIAS_TAG = "ALIAS";
	public static String PROP_NAME = "name";
	public static String PROP_VALUE = "value";
	public static String PROP_TYPE = "type";
	public static String PROP_ALIAS = "alias";
	public static String TAG_DATA_ACCESS = "DATA-ACCESS";
	public static String TAG_USER_ATTRIBUTE = "ATTRIBUTE";
	public static String PROP_USER_ATTRIBUTE_NAME = "name";
	public static final String TAG_TOOLBAR = "TOOLBAR";
	public static final String TAG_VISIBLE = "visible";
	public static final String TAG_MENU = "menu";
	public static final String TRUE = "true";
	public static final String TAG_CONNECTION = "CONNECTION";
	public static final String TAG_STAND_ALONE = "STANDALONE";
	public static final String TAG_USR = "USR";
	public static final String TAG_PWD = "PWD";
	public static final String TAG_JNDI_NAME = "JNDI_NAME";
	public static final String TAG_CATALOG = "CATALOG";
	public static final String TAG_MDX = "MDX";
	public static final String TAG_CONNECTIONSTRING = "CONNECTIONSTRING";
	public static final String TAG_DRIVER = "DRIVER";
	public static final String TAG_DIALECT = "DIALECT";
	public static final String TAG_XMLA_DATASOURCE = "xmlaserver";
	public static String TAG_CROSS_NAVIGATION = "CROSS_NAVIGATION";
	public static String TAG_CN_TARGET = "TARGET";
	public static String TAG_CN_DESCRIPTION = "DESCRIPTION";
	public static String TAG_TG_DOCUMENT_LABEL = "documentLabel";
	public static String TAG_TG_CUSTOMIZED_VIEW = "customizedView";
	public static String TAG_TG_TARGET = "target";
	public static String TAG_TG_TITLE = "title";
	public static String TAG_TN_PARAMETERS = "PARAMETERS";
	public static String TAG_TN_PARAMETER = "PARAMETER";
	public static String TAG_MDX_CLICKABLE = "clickable";
	public static String TAG_CNC_DOCUMENT = "targetDocument";
	public static String TAG_CNC_TARGET = "target";
	public static String TAG_CNC_TITLE = "title";
	public static String TAG_CNC_UNIQUENAME = "uniqueName";
	public static String TAG_CNC_PARAMETERS = "clickParameter";
	public static String TAG_CNC_NAME = "name";
	public static String TAG_CNC_VALUE = "value";

	public static final String STAD_ALONE_DS_LABEL = "STAD_ALONE_DS_LABEL";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(WhatIfXMLTemplateParser.class);

	public WhatIfTemplate parse(Object template) {
		Assert.assertNotNull(template, "Input parameter [template] cannot be null");
		Assert.assertTrue(template instanceof SourceBean, "Input parameter [template] cannot be of type [" + template.getClass().getName() + "]");
		return parse((SourceBean) template);
	}

	private WhatIfTemplate parse(SourceBean template) {

		WhatIfTemplate toReturn = null;

		try {
			logger.debug("Starting template parsing....");

			toReturn = new WhatIfTemplate();

			// init the xmla datasource
			boolean isXMLA = initXMLADataSource(template, toReturn);

			if (!isXMLA) {
				SourceBean cubeSB = (SourceBean) template.getAttribute(TAG_CUBE);
				logger.debug(TAG_CUBE + ": " + cubeSB);
				Assert.assertNotNull(cubeSB, "Template is missing " + TAG_CUBE + " tag");
				String reference = (String) cubeSB.getAttribute(PROP_SCHEMA_REFERENCE);
				logger.debug(PROP_SCHEMA_REFERENCE + ": " + reference);
				toReturn.setMondrianSchema(reference);
			}

			SourceBean mdxSB = (SourceBean) template.getAttribute(TAG_MDX_QUERY);
			logger.debug(TAG_MDX_QUERY + ": " + mdxSB);
			Assert.assertNotNull(mdxSB, "Template is missing " + TAG_MDX_QUERY + " tag");
			String mdxQuery = mdxSB.getCharacters();
			toReturn.setMdxQuery(mdxQuery);

			// TODO: OSMOSIT init Targets Clickable
			List<TargetClickable> targetsClickable = initTargetsClickable(mdxSB);
			toReturn.setTargetsClickable(targetsClickable);

			SourceBean mdxMondrianSB = (SourceBean) template.getAttribute(TAG_MDX_MONDRIAN_QUERY);
			logger.debug(TAG_MDX_MONDRIAN_QUERY + ": " + mdxMondrianSB);
			// Assert.assertNotNull(mdxMondrianSB, "Template is missing " +
			// TAG_MDX_MONDRIAN_QUERY + " tag");
			String mdxMondrianQuery = mdxMondrianSB.getCharacters();
			toReturn.setMondrianMdxQuery(mdxMondrianQuery);

			// add the scenario (writeback config & variables)
			SbiScenario scenario = initScenario(template);
			toReturn.setScenario(scenario);

			// add the model aliases
			SbiAliases aliases = initAliases(template);
			toReturn.setAliases(aliases);

			// init the toolbar config
			initToolbar(template, toReturn);

			// TODO:
			// OSMOSIT
			// Check for cross navigation configuration and button adding in toolbar
			SourceBean crossNavigation = (SourceBean) template.getAttribute(TAG_CROSS_NAVIGATION);
			if (crossNavigation != null) {
				SpagoBICrossNavigationConfig cninfo = new SpagoBICrossNavigationConfig(crossNavigation);
				toReturn.setCrossNavigation(cninfo);
				if (toReturn.getToolbarVisibleButtons() == null) {
					toReturn.setToolbarVisibleButtons(new ArrayList<String>());
				}
				toReturn.getToolbarVisibleButtons().add(new String("BUTTON_CROSS_NAVIGATION"));

			}

			// init stand alone configuration
			initStandAlone(template, toReturn);

			List<WhatIfTemplate.Parameter> parameters = new ArrayList<WhatIfTemplate.Parameter>();
			List parametersSB = mdxSB.getAttributeAsList(TAG_PARAMETER);
			Iterator it = parametersSB.iterator();
			while (it.hasNext()) {
				SourceBean parameterSB = (SourceBean) it.next();
				logger.debug("Found " + TAG_PARAMETER + " definition :" + parameterSB);
				String name = (String) parameterSB.getAttribute(PROP_PARAMETER_NAME);
				String alias = (String) parameterSB.getAttribute(PROP_PARAMETER_ALIAS);
				Assert.assertNotNull(name, "Missing parameter's " + PROP_PARAMETER_NAME + " attribute");
				Assert.assertNotNull(alias, "Missing parameter's " + PROP_PARAMETER_ALIAS + " attribute");
				WhatIfTemplate.Parameter parameter = toReturn.new Parameter();
				parameter.setName(name);
				parameter.setAlias(alias);
				parameters.add(parameter);
			}
			toReturn.setParameters(parameters);

			// read user profile for profiled data access
			setProfilingUserAttributes(template, toReturn);

			logger.debug("Template parsed succesfully");
		} catch (Exception e) {
			logger.error("Impossible to parse template [" + template.toString() + "]", e);
			throw new WhatIfTemplateParseException(e);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	private static List<TargetClickable> initTargetsClickable(SourceBean mdxSB) {
		List clickableNodes = mdxSB.getAttributeAsList(TAG_MDX_CLICKABLE);
		List<TargetClickable> targetsClickable = new ArrayList<TargetClickable>();
		logger.debug(TAG_MDX_CLICKABLE + ": " + clickableNodes);
		if (clickableNodes != null && !clickableNodes.isEmpty()) {
			for (int i = 0; i < clickableNodes.size(); i++) {
				TargetClickable targetClickable = new TargetClickable((SourceBean) clickableNodes.get(i));
				if (targetClickable != null) {
					targetsClickable.add(targetClickable);
				}
			}
		} else {
			return null;
		}
		return targetsClickable;
	}

	private void setProfilingUserAttributes(SourceBean template, WhatIfTemplate toReturn) {
		SourceBean dataAccessSB = (SourceBean) template.getAttribute(TAG_DATA_ACCESS);
		logger.debug(TAG_DATA_ACCESS + ": " + dataAccessSB);
		List<String> attributes = new ArrayList<String>();
		if (dataAccessSB != null) {
			List attributesSB = dataAccessSB.getAttributeAsList(TAG_USER_ATTRIBUTE);
			Iterator it = attributesSB.iterator();
			while (it.hasNext()) {
				SourceBean attributeSB = (SourceBean) it.next();
				logger.debug("Found " + TAG_USER_ATTRIBUTE + " definition :" + attributeSB);
				String name = (String) attributeSB.getAttribute(PROP_USER_ATTRIBUTE_NAME);
				Assert.assertNotNull(name, "Missing [" + PROP_PARAMETER_NAME + "] attribute in user profile attribute");
				attributes.add(name);
			}
		}
		toReturn.setProfilingUserAttributes(attributes);
	}

	public static boolean initXMLADataSource(SourceBean template, WhatIfTemplate toReturn) {
		List<SourceBeanAttribute> xmlaConnectionProperties;
		Map<String, String> xmlaConnectionPropertiesMap = new HashMap<String, String>();
		SourceBeanAttribute aXmlaConnectionProperty;
		String name;
		String value;

		logger.debug("IN. loading the xmla datasource config");
		SourceBean xmlaSB = (SourceBean) template.getAttribute(TAG_XMLA_DATASOURCE);
		if (xmlaSB != null) {

			logger.debug(TAG_XMLA_DATASOURCE + ": " + xmlaSB);
			xmlaConnectionProperties = xmlaSB.getContainedAttributes();
			if (xmlaConnectionProperties != null) {
				for (int i = 0; i < xmlaConnectionProperties.size(); i++) {
					aXmlaConnectionProperty = xmlaConnectionProperties.get(i);
					name = aXmlaConnectionProperty.getKey();
					value = ((SourceBean) aXmlaConnectionProperty.getValue()).getCharacters();
					xmlaConnectionPropertiesMap.put(name, value);
				}

				logger.debug("Updating the xmla datasource in the template");
				toReturn.setXmlaServerProperties(xmlaConnectionPropertiesMap);
				return true;
			}
		} else {
			logger.debug(TAG_XMLA_DATASOURCE + ": no xmla data source defined in the template");
		}
		return false;
	}

	public static void initToolbar(SourceBean template, WhatIfTemplate toReturn) {
		List<SourceBeanAttribute> toolbarButtons;
		SourceBeanAttribute aToolbarButton;
		String name;
		String visible;
		String menu;
		SourceBean value;

		logger.debug("IN. loading the toolbar config");
		SourceBean toolbarSB = (SourceBean) template.getAttribute(TAG_TOOLBAR);
		if (toolbarSB != null) {

			List<String> toolbarVisibleButtons = new ArrayList<String>();
			List<String> toolbarMenuButtons = new ArrayList<String>();

			logger.debug(TAG_TOOLBAR + ": " + toolbarSB);
			toolbarButtons = toolbarSB.getContainedAttributes();
			if (toolbarButtons != null) {
				for (int i = 0; i < toolbarButtons.size(); i++) {
					aToolbarButton = toolbarButtons.get(i);
					name = aToolbarButton.getKey();
					if (aToolbarButton.getValue() != null) {
						value = (SourceBean) aToolbarButton.getValue();
						visible = (String) value.getAttribute(TAG_VISIBLE);
						menu = (String) value.getAttribute(TAG_MENU);
						if (visible != null && visible.equalsIgnoreCase(TRUE)) {
							if (menu != null && menu.equalsIgnoreCase(TRUE)) {
								toolbarMenuButtons.add(name);
							} else {
								toolbarVisibleButtons.add(name);
							}
						}
					}
				}

				logger.debug("Updating the toolbar in the template");
				toReturn.setToolbarMenuButtons(toolbarMenuButtons);
				toReturn.setToolbarVisibleButtons(toolbarVisibleButtons);
			}
		} else {
			logger.debug(TAG_TOOLBAR + ": no toolbar buttons defined in the template");
		}

	}

	public static SbiScenario initScenario(SourceBean template) {
		logger.debug("IN. loading the scenario");
		SourceBean scenarioSB = (SourceBean) template.getAttribute(TAG_SCENARIO);
		if (scenarioSB != null) {
			logger.debug(TAG_SCENARIO + ": " + scenarioSB);
			String scenarioName = (String) scenarioSB.getAttribute(PROP_NAME);
			SbiScenario scenario = new SbiScenario(scenarioName);

			initWriteBackConf(scenarioSB, scenario);
			initScenarioVariables(scenarioSB, scenario);

			logger.debug("Scenario with name " + scenarioName + " successfully loaded");
			return scenario;

		} else {
			logger.debug(TAG_SCENARIO + ": no write back configuration found in the template");
		}
		return null;
	}

	public static SbiAliases initAliases(SourceBean template) {
		logger.debug("IN. loading the aliases for the what-if model");
		SourceBean aliasesSB = (SourceBean) template.getAttribute(TAG_ALIASES);
		if (aliasesSB != null) {
			logger.debug(TAG_ALIASES + ": " + aliasesSB);
			SbiAliases aliases = new SbiAliases();

			initModelAliases(aliasesSB, aliases);

			logger.debug("Aliases successfully loaded");
			return aliases;

		} else {
			logger.debug(TAG_SCENARIO + ": no write back configuration found in the template");
		}
		return null;
	}

	private static void initWriteBackConf(SourceBean scenarioSB, SbiScenario scenario) {
		logger.debug("IN. loading the writeback config");
		WriteBackEditConfig writeBackConfig = new WriteBackEditConfig();
		String editCube = (String) scenarioSB.getAttribute(WhatIfXMLTemplateParser.EDIT_CUBE_ATTRIBUTE);
		if (editCube == null || editCube.length() == 0) {
			logger.error("In the writeback is enabled you must specify a cube to edit. Remove the " + WRITEBACK_TAG
					+ " tag or specify a value for the attribute " + EDIT_CUBE_ATTRIBUTE);
			throw new SpagoBIEngineRuntimeException("In the writeback is enabled you must specify a cube to edit. Remove the " + WRITEBACK_TAG
					+ " tag or specify a value for the attribute " + EDIT_CUBE_ATTRIBUTE);
		}

		List<SourceBean> editableMeasuresBeans = scenarioSB.getAttributeAsList(WhatIfXMLTemplateParser.MEASURE_TAG);
		if (editableMeasuresBeans != null && editableMeasuresBeans.size() > 0) {
			List<String> editableMeasures = new ArrayList<String>();
			for (int i = 0; i < editableMeasuresBeans.size(); i++) {
				editableMeasures.add(editableMeasuresBeans.get(i).getCharacters());
			}
			writeBackConfig.setEditableMeasures(editableMeasures);
			logger.debug(TAG_SCENARIO + ":the editable measures are " + editableMeasures);
		}

		String initialVersion = (String) scenarioSB.getAttribute(WhatIfXMLTemplateParser.INITIAL_VERSION_ATTRIBUTE);
		if (initialVersion != null && initialVersion.length() > 0) {
			try {
				writeBackConfig.setInitialVersion(new Integer(initialVersion));
				logger.debug("The inital version is " + initialVersion);
			} catch (Exception e) {
				logger.error("Error loading the inital version from the template " + initialVersion);
			}
		}

		writeBackConfig.setEditCubeName(editCube);
		logger.debug(TAG_SCENARIO + ":the edit cube is " + editCube);
		scenario.setWritebackEditConfig(writeBackConfig);
		logger.debug("OUT. Writeback config loaded");
	}

	private static void initScenarioVariables(SourceBean scenarioSB, SbiScenario scenario) {
		logger.debug("IN. loading the scenario variables");
		List<SbiScenarioVariable> variables = new ArrayList<SbiScenarioVariable>();

		List<SourceBean> variablesBeans = scenarioSB.getAttributeAsList(VARIABLE_TAG);
		if (variablesBeans != null && variablesBeans.size() > 0) {
			for (int i = 0; i < variablesBeans.size(); i++) {
				String name = (String) variablesBeans.get(i).getAttribute(PROP_NAME);
				String value = (String) variablesBeans.get(i).getAttribute(PROP_VALUE);
				String type = (String) variablesBeans.get(i).getAttribute(PROP_TYPE);
				variables.add(new SbiScenarioVariable(name, value, type));
			}
		}
		scenario.setVariables(variables);
		logger.debug("OUT. loaded " + variables.size() + " scenario variables");
	}

	private static void initModelAliases(SourceBean aliasesSB, SbiAliases aliases) {
		logger.debug("IN. loading the aliases");
		List<SbiAlias> aliasesFound = new ArrayList<SbiAlias>();

		List<SourceBean> aliasesBeans = aliasesSB.getAttributeAsList(DIMENSION_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = DIMENSION_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		aliasesBeans = aliasesSB.getAttributeAsList(HIERARCHY_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = HIERARCHY_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		// TODO add generic aliases
		aliasesBeans = aliasesSB.getAttributeAsList(ALIAS_TAG);
		if (aliasesBeans != null && aliasesBeans.size() > 0) {
			for (int i = 0; i < aliasesBeans.size(); i++) {
				String name = (String) aliasesBeans.get(i).getAttribute(PROP_NAME);
				String alias = (String) aliasesBeans.get(i).getAttribute(PROP_ALIAS);
				String type = ALIAS_TAG;
				aliasesFound.add(new SbiAlias(name, alias, type));
			}
		}

		aliases.setAliases(aliasesFound);
		logger.debug("OUT. loaded " + aliasesFound.size() + " aliases");
	}

	private static String getBeanValue(String tag, SourceBean bean) {
		String field = null;
		SourceBean fieldBean = null;
		fieldBean = (SourceBean) bean.getAttribute(tag);
		if (fieldBean != null) {
			field = fieldBean.getCharacters();
			if (field == null) {
				field = "";
			}
		}
		return field;
	}

	private static void initStandAlone(SourceBean template, WhatIfTemplate toReturn) {
		logger.debug("IN. loading the configuration for a stand alone execution");
		SourceBean standAloneSB = (SourceBean) template.getAttribute(TAG_STAND_ALONE);
		if (standAloneSB != null) {
			logger.debug("This is a stand alone execution");
			logger.debug(TAG_STAND_ALONE + ": " + standAloneSB);
			IDataSource ds = new DataSource();
			ds.setLabel(STAD_ALONE_DS_LABEL);
			SourceBean connectionProperties = (SourceBean) standAloneSB.getAttribute(TAG_CONNECTION);
			String jndiName = getBeanValue(TAG_JNDI_NAME, connectionProperties);
			if (StringUtilities.isNotEmpty(jndiName)) {
				ds.setJndi(getBeanValue(TAG_JNDI_NAME, connectionProperties));
			} else {
				ds.setPwd(getBeanValue(TAG_PWD, connectionProperties));
				ds.setHibDialectClass(getBeanValue(TAG_DIALECT, connectionProperties));
				ds.setUser(getBeanValue(TAG_USR, connectionProperties));
				ds.setUrlConnection(getBeanValue(TAG_CONNECTIONSTRING, connectionProperties));
				ds.setDriver(getBeanValue(TAG_DRIVER, connectionProperties));
			}
			String catalog = getBeanValue(TAG_CATALOG, connectionProperties);
			toReturn.setStandAloneConnection(ds);
			toReturn.setMondrianSchema(catalog);
		} else {
			logger.debug("This is not a stand alone execution");
		}
	}
}
