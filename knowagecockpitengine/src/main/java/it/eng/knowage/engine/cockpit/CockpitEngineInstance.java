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
package it.eng.knowage.engine.cockpit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.datasource.IDataSource;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.association.AssociationManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CockpitEngineInstance extends AbstractEngineInstance {
	// logger component
	public static Logger logger = Logger.getLogger(CockpitEngineInstance.class);

	JSONObject template;
	AssociationManager associationManager;

	// ENVIRONMENT VARIABLES
	private final String[] lstEnvVariables = { "SBI_EXECUTION_ID", "SBI_COUNTRY", "SBI_LANGUAGE", "SBI_SPAGO_CONTROLLER", "SBI_EXECUTION_ROLE", COUNTRY,
			LANGUAGE, "user_id", "DOCUMENT_ID", "DOCUMENT_LABEL", "DOCUMENT_NAME", "DOCUMENT_IS_PUBLIC", "DOCUMENT_COMMUNITIES", "DOCUMENT_DESCRIPTION",
			"SPAGOBI_AUDIT_ID", "DOCUMENT_USER", "DOCUMENT_IS_VISIBLE", "DOCUMENT_AUTHOR", "DOCUMENT_FUNCTIONALITIES", "DOCUMENT_VERSION", "IS_FOR_EXPORT",
			"COCKPIT_SELECTIONS", "EDIT_MODE", "IS_TECHNICAL_USER", "SBI_ENVIRONMENT", "documentMode", "timereloadurl", "DOCUMENT_OUTPUT_PARAMETERS" };

	public CockpitEngineInstance(String template, Map env) {
		super(env);
		try {
			this.template = new JSONObject(template);

			if (this.template.length() > 0 && (!this.template.has("sheets") && !this.template.has("configuration"))) {
				// TODO This is on old cockpit, maybe we need a translator here
				throw new SpagoBIRuntimeException("This is an outdated and not supported cockpit.");
			}

			// loading from old designer
			if (this.template.length() > 0) {
				JSONArray sheets = this.template.optJSONArray("sheets");
				for (int i = 0; i < sheets.length(); i++) {
					JSONObject sheet = sheets.optJSONObject(i);
					JSONArray widgets = sheet.optJSONArray("widgets");
					for (int j = 0; j < widgets.length(); j++) {
						JSONObject widget = widgets.optJSONObject(j);
						String type = widget.getString("type");
						if (type.equals("chart")) {

							JSONObject content = widget.getJSONObject("content");
							JSONObject oldDesigner = content.optJSONObject("chartTemplate").optJSONObject("CHART");
							if (oldDesigner.get("style") instanceof String) {
								this.template = parseTemplate(this.template);
							}
						}
					}
				}
			}

			this.associationManager = new AssociationManager();
		} catch (JSONException e) {
			String msg = String.format("Impossible to parse template: %s", template);
			throw new SpagoBIRuntimeException(msg, e);
		}
	}

	public JSONObject getTemplate() {
		return template;
	}

	public AssociationManager getAssociationManager() {
		return associationManager;
	}

	public IDataSource getDataSource() {
		return (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	}

	public IDataSet getDataSet() {
		return (IDataSet) this.getEnv().get(EngineConstants.ENV_DATASET);
	}

	public Locale getLocale() {
		return (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
	}

	public String getDocumentLabel() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_LABEL);
	}

	public Integer getDocumentId() {
		return (this.getEnv().get(EngineConstants.ENV_DOCUMENT_ID) == null ? null
				: Integer.parseInt((String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_ID)));
	}

	public String getDocumentVersion() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_VERSION);
	}

	public String getDocumentAuthor() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_AUTHOR);
	}

	public String getDocumentUser() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_USER);
	}

	public String getDocumentName() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_NAME);
	}

	public String getDocumentDescription() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_DESCRIPTION);
	}

	public String getDocumentIsVisible() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_VISIBLE);
	}

	public String getDocumentPreviewFile() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_PREVIEW_FILE);
	}

	public IEngUserProfile getUserProfile() {
		return (IEngUserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	}

	public String isTechnicalUser() {
		return (String) this.getEnv().get(EngineConstants.ENV_IS_TECHNICAL_USER);
	}

	public Map<String, String> getOutputParameters() {
		Map<String, String> outParslist = new HashMap<String, String>();

		try {
			String jsonArrayPars = (String) this.getEnv().get(EngineConstants.DOCUMENT_OUTPUT_PARAMETERS);

			// passed now as JSON Array
			if (jsonArrayPars != null && !jsonArrayPars.equals("")) {
				JSONArray array = new JSONArray(jsonArrayPars);
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = (JSONObject) array.get(i);
					String name = (String) obj.get("name");
					String type = (String) obj.get("type");
					outParslist.put(name, type);
				}

			}
		} catch (Exception e) {
			logger.error("Error in parsing output parameters ", e);
			throw new SpagoBIRuntimeException(e);
		}

		// String outPars = (String) this.getEnv().get(EngineConstants.DOCUMENT_OUTPUT_PARAMETERS);
		// if (outPars != null && !outPars.equals("")) {
		// StringTokenizer st = new StringTokenizer(outPars, ",", false);
		// String parameterToken = null;
		// while (st.hasMoreTokens()) {
		// parameterToken = st.nextToken();
		// outParslist.add(parameterToken);
		// }
		// }
		return outParslist;
	}

	public String[] getDocumentCommunities() {
		try {
			String strCommunities = (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_COMMUNITIES);
			if (strCommunities == null)
				return null;
			else
				return JSONUtils.asStringArray(JSONUtils.toJSONArray(strCommunities));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get communities list", e);
		}
	}

	public List<Integer> getDocumentFunctionalities() {
		try {
			String strFunctionalities = (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_FUNCTIONALITIES);
			if (strFunctionalities == null)
				return null;
			else
				return JSONUtils.asList(JSONUtils.toJSONArray(strFunctionalities));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get functionalities list", e);
		}
	}

	public String getDocumentIsPublic() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_PUBLIC);
	}

	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy) this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}

	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy) this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}

	public boolean isVisibleDataSet() {
		IDataSet datSet = getDataSet();
		if (datSet != null) {
			IEngUserProfile profile = getUserProfile();
			return DataSetUtilities.isExecutableByUser(datSet, profile);
		}
		return true;
	}

	public Map getAnalyticalDrivers() {
		Map toReturn = new HashMap();
		Iterator it = getEnv().keySet().iterator();
		while (it.hasNext()) {
			String parameterName = (String) it.next();
			Object parameterValue = getEnv().get(parameterName);
			// test necessary for don't pass complex objects like proxy,...
			if (parameterValue != null && parameterValue.getClass().getName().equals("java.lang.String") && isAnalyticalDriver(parameterName)) {
				toReturn.put(parameterName, parameterValue);
			}
		}
		return toReturn;
	}

	private boolean isAnalyticalDriver(String parName) {
		if (parName.endsWith("_description")) {
			return false;
		}
		for (int i = 0; i < lstEnvVariables.length; i++) {
			if (lstEnvVariables[i].equalsIgnoreCase(parName)) {
				return false;
			}
		}
		return true;
	}

	private static JSONObject parseTemplate(JSONObject jsonObj) throws JSONException {

		Iterator keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object keyValue = jsonObj.get(key);
			if (key.equalsIgnoreCase("style")) {

				String value = keyValue.toString();
				String[] result = value.split(";");
				JSONObject obj = new JSONObject();
				for (int i = 0; i < result.length; i++) {
					String[] temp = result[i].split(":");
					if (temp.length > 1) {

						if (isNumeric(temp[1])) {
							int num = Integer.parseInt(temp[1]);
							obj.put(temp[0], num);
						} else if (temp[1].equals("true") || temp[1].equals("false")) {
							boolean bool = Boolean.parseBoolean(temp[1]);
							obj.put(temp[0], bool);
						} else {
							obj.put(temp[0], temp[1]);
						}

					} else {
						obj.put(temp[0], "");
					}

				}
				jsonObj.put(key, obj);

			}

			if (isNumeric(keyValue.toString())) {
				jsonObj.put(key, Integer.parseInt(keyValue.toString()));
			}

			if (keyValue.toString().equals("true") || keyValue.toString().equals("false")) {
				jsonObj.put(key, Boolean.parseBoolean(keyValue.toString()));
			}

			if (keyValue instanceof JSONArray) {

				JSONArray array = (JSONArray) keyValue;
				for (int i = 0; i < array.length(); i++) {
					if (array.get(i) instanceof JSONObject) {
						JSONObject obj = array.getJSONObject(i);
						parseTemplate(obj);
					}

				}

			}

			if (keyValue instanceof JSONObject) {

				parseTemplate((JSONObject) keyValue);
			}

		}
		return jsonObj;
	}

	private static boolean isNumeric(String str) {
		try {
			int num = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	// -- unimplemented methods ----------------------------------

	@Override
	public IEngineAnalysisState getAnalysisState() {
		throw new CockpitEngineRuntimeException("Unsupported method [getAnalysisState]");
	}

	@Override
	public void setAnalysisState(IEngineAnalysisState analysisState) {
		throw new CockpitEngineRuntimeException("Unsupported method [setAnalysisState]");
	}

	@Override
	public void validate() throws SpagoBIEngineException {
		throw new CockpitEngineRuntimeException("Unsupported method [validate]");
	}
}
