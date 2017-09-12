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
package it.eng.spagobi.tools.dataset.common.behaviour;

import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.ScriptDataSet;
import it.eng.spagobi.tools.dataset.common.query.IQueryTransformer;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.exceptions.ProfileAttributeDsException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.scripting.SpagoBIScriptManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class QuerableBehaviour extends AbstractDataSetBehaviour {

	IQueryTransformer queryTransformer;

	private static transient Logger logger = Logger.getLogger(QuerableBehaviour.class);

	public QuerableBehaviour(IDataSet targetDataSet) {
		super(QuerableBehaviour.class.getName(), targetDataSet);
	}

	public String getStatement() {
		String statement;

		logger.debug("IN");
		try {
			Assert.assertNotNull(getTargetDataSet(), "Target dataset of a QuerableBehaviour cannot be null");
			logger.debug("Querable dataset [" + getTargetDataSet().getName() + "] is of type [" + getTargetDataSet().getClass().getName() + "]");
			statement = getBaseStatement();
			logger.debug("Base dataset statement is equal to [" + statement + "]");
			Assert.assertNotNull(statement, "Querable dataset statment cannot be null");

			statement = resolveProfileAttributes(statement);
			logger.debug("Dataset statement after profile attributes substitution [" + statement + "]");

			statement = resolveParameters(statement);
			logger.debug("Dataset statement after  attributes substitution [" + statement + "]");

			if (queryTransformer != null) {
				statement = (String) queryTransformer.transformQuery(statement);
			}
		} finally {
			logger.debug("OUT");
		}

		return statement;
	}

	private String getBaseStatement() {
		String statement = null;
		if (getTargetDataSet() instanceof ScriptDataSet) {
			statement = ((ScriptDataSet) getTargetDataSet()).getScript();
		} else if (getTargetDataSet() instanceof ConfigurableDataSet) {
			ConfigurableDataSet jdbcDataSet = (ConfigurableDataSet) getTargetDataSet();
			if (StringUtilities.isNotEmpty(jdbcDataSet.getQueryScript())) {
				statement = (String) jdbcDataSet.getQuery();
				statement = applyScript(statement, jdbcDataSet.getQueryScript(), jdbcDataSet.getQueryScriptLanguage());
			} else {
				statement = (String) jdbcDataSet.getQuery();
			}
		} else {
			logger.error("Type [" + getTargetDataSet().getClass().getName() + "] of the dataset [" + getTargetDataSet().getName() + "] is not managed! \n"
					+ "Is impossible to define the dataset's statemant! ");
			Assert.assertNotNull(statement, "Querable dataset statment cannot be null");
		}
		return statement;
	}

	private String applyScript(String statement, String script, String language) {
		List<Object> imports = null;
		if ("groovy".equals(language)) {
			imports = new ArrayList<Object>();
			URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedGroovyScript.groovy");
			File scriptFile;
			try {
				logger.debug("predefinedGroovyScript.groovy file URL is equal to [" + url + "]");
				// URI fileURI = url.toURI();
				// logger.debug("predefinedGroovyScript.groovy file URL is equal to ["
				// + fileURI + "]");
				// scriptFile = new File( fileURI );
				// imports.add(scriptFile);
				imports.add(url);
			} catch (Throwable t) {
				logger.warn("Impossible to load predefinedGroovyScript.groovy", t);
			}

		} else if ("ECMAScript".equals(language)) {
			imports = new ArrayList<Object>();
			URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedJavascriptScript.js");
			File scriptFile;
			try {
				logger.debug("predefinedJavascriptScript.js file URL is equal to [" + url + "]");
				// URI fileURI = url.toURI();
				// logger.debug("predefinedJavascriptScript.js file URL is equal to ["
				// + fileURI + "]");
				// scriptFile = new File( fileURI );
				// imports.add(scriptFile);
				imports.add(url);
			} catch (Throwable t) {
				logger.warn("Impossible to load predefinedJavascriptScript.js", t);
			}
		} else {
			logger.debug("There is no predefined script file to import for scripting language [" + language + "]");
		}

		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("attributes", getTargetDataSet().getUserProfileAttributes());
		bindings.put("parameters", getTargetDataSet().getParamsMap());
		bindings.put("query", statement);
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();
		Object o = scriptManager.runScript(script, language, bindings, imports);
		return o == null ? statement : o.toString();
	}

	private String resolveProfileAttributes(String statement) {

		String newStatement = statement;

		Map<String, Object> userProfileAttributes = getTargetDataSet().getUserProfileAttributes();

		if (getTargetDataSet() instanceof ScriptDataSet) {
			try {
				newStatement = substituteProfileAttributes(newStatement, userProfileAttributes);
			} catch (Throwable e) {
				throw new ProfileAttributeDsException("An error occurred while excuting query [" + newStatement + "]", e);
			}
		} else if (getTargetDataSet() instanceof JDBCDataSet) {
			try {
				newStatement = StringUtilities.substituteParametersInString(newStatement, userProfileAttributes);
			} catch (Exception e) {
				List list = checkProfileAttributesUnfilled(newStatement);
				String atts = "";
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					atts += string;
					if (iterator.hasNext()) {
						atts += ", ";
					}
				}
				throw new ProfileAttributeDsException("The following profile attributes have no value[" + atts + "]", e);

			}
		}

		return newStatement;
	}

	private String resolveParameters(String statement) {

		String newStatement = statement;

		logger.debug("Dataset paramMap [" + getTargetDataSet().getParamsMap() + "]");

		if (getTargetDataSet().getParamsMap() != null) {
			logger.debug("Dataset paramMap contains [" + getTargetDataSet().getParamsMap().size() + "] parameters");

			// if a parameter has value '' put null!
			Map parameterValues = getTargetDataSet().getParamsMap();
			Vector<String> parsToChange = new Vector<String>();

			for (Iterator iterator = parameterValues.keySet().iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				Object val = parameterValues.get(parName);
				if (val != null && val.equals("")) {
					val = null;
					parsToChange.add(parName);
				}
				// parameterValues.remove(parName);
				// parameterValues.put(parName, val);
			}
			for (Iterator iterator = parsToChange.iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				parameterValues.remove(parName);
				parameterValues.put(parName, null);
			}

			try {
				Map parTypeMap = getParTypeMap(getTargetDataSet());
				newStatement = StringUtilities.substituteDatasetParametersInString(newStatement, getTargetDataSet().getParamsMap(), parTypeMap, false);
			} catch (Throwable e) {
				throw new SpagoBIRuntimeException("An error occurred while settin up parameters", e);
			}
		}

		// after having substituted all parameters check there are not other
		// parameters unfilled otherwise throw an exception;
		List<String> parsUnfilled = checkParametersUnfilled(newStatement);
		if (parsUnfilled != null) {
			// means there are parameters not valorized, throw exception
			logger.error("there are parameters without values");
			String pars = "";
			for (Iterator iterator = parsUnfilled.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				pars += string;
				if (iterator.hasNext()) {
					pars += ", ";
				}
			}
			pars += " have no value specified";
			throw new ParametersNotValorizedException("The folowing parameters have no value [" + pars + "]");

		}

		return newStatement;
	}

	private String substituteProfileAttributes(String script, Map attributes) {
		logger.debug("IN");
		String cleanScript = new String(script);
		int indexSubstitution = 0;
		int profileAttributeStartIndex = script.indexOf("${", indexSubstitution);

		while (profileAttributeStartIndex != -1) {
			int profileAttributeEndIndex = script.indexOf("}", profileAttributeStartIndex);
			String attributeName = script.substring(profileAttributeStartIndex + 2, profileAttributeEndIndex).trim();
			Object attributeValueObj = attributes.get(attributeName);
			if (attributeValueObj == null) {
				logger.error("Profile attribute " + attributeName + " not found");
				attributeValueObj = "undefined";
			}
			cleanScript = cleanScript.replaceAll("\\$\\{" + attributeName + "\\}", attributeValueObj.toString());
			indexSubstitution = profileAttributeEndIndex;
			profileAttributeStartIndex = script.indexOf("${", indexSubstitution);
		}
		logger.debug("OUT");
		return cleanScript;
	}

	public Map getParTypeMap(IDataSet dataSet) throws SourceBeanException {

		Map parTypeMap;
		String parametersXML;
		List parameters;

		logger.debug("IN");

		try {
			parTypeMap = new HashMap();
			parametersXML = dataSet.getParameters();

			logger.debug("Dataset parameters string is equals to [" + parametersXML + "]");

			if (!StringUtilities.isEmpty(parametersXML)) {
				parameters = DataSetParametersList.fromXML(parametersXML).getItems();
				logger.debug("Dataset have  [" + parameters.size() + "] parameters");

				for (int i = 0; i < parameters.size(); i++) {
					DataSetParameterItem dsDet = (DataSetParameterItem) parameters.get(i);
					String name = dsDet.getName();
					String type = dsDet.getType();
					logger.debug("Paremeter [" + (i + 1) + "] name is equals to  [" + name + "]");
					logger.debug("Paremeter [" + (i + 1) + "] type is equals to  [" + type + "]");
					parTypeMap.put(name, type);
				}
			}

		} finally {
			logger.debug("OUT");
		}

		return parTypeMap;
	}

	/**
	 * search if there are parameters unfilled and return their names
	 * 
	 * @param statement
	 * @return
	 */

	public static List checkParametersUnfilled(String statement) {
		List toReturn = null;
		int index = statement.indexOf("$P{");
		while (index != -1) {
			int endIndex = statement.indexOf('}', index);
			if (endIndex != -1) {
				String nameAttr = statement.substring(index, endIndex + 1);
				if (toReturn == null) {
					toReturn = new ArrayList<String>();
				}
				toReturn.add(nameAttr);
				index = statement.indexOf("$P{", endIndex);
			}
		}
		return toReturn;
	}

	public static List checkProfileAttributesUnfilled(String statement) {
		List toReturn = null;
		int index = statement.indexOf("${");
		while (index != -1) {

			int endIndex = statement.indexOf('}', index);
			if (endIndex != -1) {
				String nameAttr = statement.substring(index, endIndex + 1);
				if (toReturn == null) {
					toReturn = new ArrayList<String>();
				}
				toReturn.add(nameAttr);
				index = statement.indexOf("${", endIndex);
			}
		}
		return toReturn;
	}

	public IQueryTransformer getQueryTransformer() {
		return queryTransformer;
	}

	public void setQueryTransformer(IQueryTransformer queryTransformer) {
		this.queryTransformer = queryTransformer;
	}
}
