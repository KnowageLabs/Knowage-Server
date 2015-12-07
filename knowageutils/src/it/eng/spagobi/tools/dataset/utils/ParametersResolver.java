package it.eng.spagobi.tools.dataset.utils;

import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.exceptions.ProfileAttributeDsException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 *  Resolve profile attributes and parameters
 *  
 * @author fabrizio
 *
 */
public class ParametersResolver {

	private static Logger logger = Logger.getLogger(ParametersResolver.class);

	/**
	 * Resolve profile attributes and parameters
	 * 
	 * @param statement
	 * @param dataSet
	 * @return
	 */
	public String resolveAll(String statement, IDataSet dataSet) {
		String res = resolveProfileAttributes(statement, dataSet);
		res = resolveParameters(res, dataSet);
		return res;
	}

	@SuppressWarnings({ "unchecked" })
	public String resolveProfileAttributes(String statement, IDataSet targetDataSet) {

		String newStatement = statement;

		Map<String, Object> userProfileAttributes = targetDataSet.getUserProfileAttributes();

		if (!(targetDataSet instanceof JDBCDataSet)) {
			try {
				return substituteProfileAttributes(newStatement, userProfileAttributes);
			} catch (Exception e) {
				throw new ProfileAttributeDsException("Error during substitution of profile attributes", e);
			}
		}
		
		Assert.assertTrue(targetDataSet instanceof JDBCDataSet,"targetDataSet instanceof JDBCDataSet");
		try {
			return StringUtilities.substituteParametersInString(newStatement, userProfileAttributes);
		} catch (Exception e) {
			String atts = getAttributes(newStatement);
			throw new ProfileAttributeDsException("The following profile attributes have no value[" + atts + "]", e);

		}

	}

	@SuppressWarnings("rawtypes")
	private static String getAttributes(String newStatement) {
		List list = checkProfileAttributesUnfilled(newStatement);
		String atts = "";
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			atts += string;
			if (iterator.hasNext()) {
				atts += ", ";
			}
		}
		return atts;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String resolveParameters(String statement, IDataSet targetDataSet) {

		String newStatement = statement;

		logger.debug("Dataset paramMap [" + targetDataSet.getParamsMap() + "]");

		if (targetDataSet.getParamsMap() != null) {
			logger.debug("Dataset paramMap contains [" + targetDataSet.getParamsMap().size() + "] parameters");

			// if a parameter has value '' put null!
			Map parameterValues = targetDataSet.getParamsMap();
			Vector<String> parsToChange = new Vector<String>();

			for (Iterator iterator = parameterValues.keySet().iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				Object val = parameterValues.get(parName);
				if (val != null && val.equals("")) {
					val = null;
					parsToChange.add(parName);
				}
			}
			for (Iterator iterator = parsToChange.iterator(); iterator.hasNext();) {
				String parName = (String) iterator.next();
				parameterValues.remove(parName);
				parameterValues.put(parName, null);
			}

			try {
				Map parTypeMap = getParTypeMap(targetDataSet);
				newStatement = StringUtilities.substituteDatasetParametersInString(newStatement, targetDataSet.getParamsMap(), parTypeMap, false);
			} catch (Exception e) {
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

	@SuppressWarnings("rawtypes")
	private static String substituteProfileAttributes(String script, Map attributes) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List checkProfileAttributesUnfilled(String statement) {
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

	/**
	 * search if there are parameters unfilled and return their names
	 * 
	 * @param statement
	 * @return
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List checkParametersUnfilled(String statement) {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getParTypeMap(IDataSet dataSet) throws SourceBeanException {

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

}
