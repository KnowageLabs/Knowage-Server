package it.eng.knowage.engines.svgviewer.dataset.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Link.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Link {

	/** The parameters. */
	private Map parameters;

	/** The Constant DEFAULT_BASE_URL. */
	public static final String DEFAULT_BASE_URL = "javascript:void(0)";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(Link.class);

	/**
	 * Instantiates a new link.
	 */
	public Link() {
		parameters = new HashMap();
	}

	/**
	 * Adds the parameter.
	 *
	 * @param type
	 *            the type
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void addParameter(String type, String scope, String name, String value) {
		Parameter parameter = new Parameter(type, scope, name, value);
		parameters.put(parameter.getName(), parameter);
	}

	/**
	 * The Class Parameter.
	 */
	public static class Parameter {

		// types
		public static final String RELATIVE = "relative";
		public static final String ABSOLUTE = "absolute";

		// scopes
		public static final String DATASET = "dataset";
		public static final String ENVIRONMENT = "environment";

		/** The type. */
		String type;

		/** The name. */
		String name;

		/** The value. */
		String value;

		/** The value. */
		String scope;

		/**
		 * Instantiates a new parameter.
		 *
		 * @param type
		 *            the type
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 */
		public Parameter(String type, String scope, String name, String value) {
			setType(type);
			setScope(scope);
			setName(name);
			setValue(value);
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Sets the type.
		 *
		 * @param type
		 *            the new type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name.
		 *
		 * @param name
		 *            the new name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Sets the value.
		 *
		 * @param value
		 *            the new value
		 */
		public void setValue(String value) {
			this.value = value;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

		public boolean isRelative() {
			return RELATIVE.equalsIgnoreCase(getType());
		}

		public boolean isRealtiveToEnvironment() {
			return isRelative() && ENVIRONMENT.equalsIgnoreCase(getScope());
		}

		public boolean isRealtiveToDataset() {
			return isRelative() && DATASET.equalsIgnoreCase(getScope());
		}

		public String getActualValue(ResultSet resultSet, Map env) throws SQLException {
			String actualValue = null;
			if (isRelative()) {
				if (isRealtiveToDataset()) {
					actualValue = resultSet.getString(resultSet.findColumn(getValue()));
				} else if (isRealtiveToEnvironment()) {
					actualValue = "" + env.get(getValue());
				}
			} else {
				actualValue = getValue();
			}
			return actualValue;
		}

		public String getXActualValue(IRecord record, Map env) {
			IField field;

			String actualValue = null;
			if (isRelative()) {
				if (isRealtiveToDataset()) {
					int recordIndex = record.getDataStore().getMetaData().getFieldIndex(getValue());
					if (recordIndex < 0) {
						logger.warn("Impossible to find column [" + getValue() + "] in datstore");
						actualValue = "undefined";
					} else {
						field = record.getFieldAt(recordIndex);
						actualValue = "" + field.getValue();
					}

				} else if (isRealtiveToEnvironment()) {
					actualValue = "" + env.get(getValue());
				}
			} else {
				actualValue = getValue();
			}
			return actualValue;
		}
	}

	/**
	 * To string.
	 *
	 * @param resultSet
	 *            the result set
	 *
	 * @return the string
	 */
	public String toString(ResultSet resultSet, Map env) {
		logger.debug("IN");
		String link = null;
		String execIframeId = null;
		String targetDocLabel = "";
		String parametersStr = "";
		String drillDocTitle = null;
		String target = "self";

		execIframeId = (String) env.get(SvgViewerEngineConstants.ENV_EXEC_IFRAME_ID);

		try {
			Iterator it = parameters.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				Parameter param = (Parameter) parameters.get(key);
				if (param.getName().equalsIgnoreCase("DOCUMENT_LABEL")) {
					targetDocLabel = param.getActualValue(resultSet, env);
				} else if (param.getName().equalsIgnoreCase("target")) {
					target = param.getActualValue(resultSet, env);
					logger.debug("Target:" + target != null ? target : "null");
				} else if (param.getName().equalsIgnoreCase("title")) {
					drillDocTitle = param.getActualValue(resultSet, env);
					logger.debug("Drill Title:" + drillDocTitle != null ? drillDocTitle : "null");
				} else {
					parametersStr += param.getName() + "=" + param.getActualValue(resultSet, env) + "&";
				}
			}
			if (parametersStr.endsWith("&")) {
				parametersStr = parametersStr.substring(0, parametersStr.length() - 1);
			}

			// link = "javascript:parent.parent.execCrossNavigation('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";
			link = "callCrossNavFunc('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";
			// link = "javascript:ping('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";

			if (drillDocTitle != null && target != null && target.equalsIgnoreCase("tab")) {
				link += ",'','" + drillDocTitle + "','tab'";
			} else if (drillDocTitle != null) {
				link += ",'','" + drillDocTitle + "'";
			}
			link += ");";
			logger.debug("Link URL:" + link);

		} catch (Exception e) {
			link = "void(0)";
		}
		logger.debug("OUT");
		return link;
	}

	/**
	 * To string.
	 *
	 * @param record
	 *            the result set
	 *
	 * @return the string
	 */
	public String toXString(IRecord record, Map env) {
		logger.debug("IN");
		String link = null;
		String execIframeId = null;
		String targetDocLabel = "";
		String parametersStr = "";
		String drillDocTitle = null;
		String target = "self";

		execIframeId = (String) env.get(SvgViewerEngineConstants.ENV_EXEC_IFRAME_ID);

		try {
			Iterator it = parameters.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				Parameter param = (Parameter) parameters.get(key);
				if (param.getName().equalsIgnoreCase("DOCUMENT_LABEL")) {
					targetDocLabel = param.getXActualValue(record, env);
				} else if (param.getName().equalsIgnoreCase("target")) {
					target = param.getXActualValue(record, env);
					logger.debug("Target:" + target != null ? target : "null");
				} else if (param.getName().equalsIgnoreCase("title")) {
					drillDocTitle = param.getXActualValue(record, env);
					logger.debug("Drill Title:" + drillDocTitle != null ? drillDocTitle : "null");
				} else {
					parametersStr += param.getName() + "=" + param.getXActualValue(record, env) + "&";
				}
			}
			if (parametersStr.endsWith("&")) {
				parametersStr = parametersStr.substring(0, parametersStr.length() - 1);
			}

			// link = "javascript:parent.execCrossNavigation('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";
			link = "callCrossNavFunc('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";
			// link = "javascript:ping('" + execIframeId + "', '" + targetDocLabel + "' , '" + parametersStr + "'";

			if (drillDocTitle != null && target != null && target.equalsIgnoreCase("tab")) {
				link += ",'','" + drillDocTitle + "','tab'";
			} else if (drillDocTitle != null) {
				link += ",'','" + drillDocTitle + "'";
			}
			link += ");";
			logger.debug("Link URL:" + link);

		} catch (Exception e) {
			logger.error("Impossible to stringify link: ", e);
			// link = "javascript:void(0)";
			link = "";

		}
		logger.debug("OUT");
		return link;
	}
}
