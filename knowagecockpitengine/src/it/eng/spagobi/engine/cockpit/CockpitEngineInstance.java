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
package it.eng.spagobi.engine.cockpit;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class CockpitEngineInstance extends AbstractEngineInstance {
	// logger component
	public static Logger logger = Logger.getLogger(CockpitEngineInstance.class);

	JSONObject template;
	AssociationManager associationManager;

	// ENVIRONMENT VARIABLES
	private final String[] lstEnvVariables = { "SBI_EXECUTION_ID", "SBICONTEXT", "SBI_COUNTRY", "SBI_LANGUAGE", "SBI_SPAGO_CONTROLLER", "SBI_EXECUTION_ROLE",
			"SBI_HOST", "country", "language", "user_id", "DOCUMENT_ID", "DOCUMENT_LABEL", "DOCUMENT_NAME", "DOCUMENT_IS_PUBLIC", "DOCUMENT_COMMUNITIES",
			"DOCUMENT_DESCRIPTION", "SPAGOBI_AUDIT_ID", "DOCUMENT_USER", "DOCUMENT_IS_VISIBLE", "DOCUMENT_AUTHOR", "DOCUMENT_FUNCTIONALITIES",
			"DOCUMENT_VERSION", };

	public CockpitEngineInstance(String template, Map env) {
		super(env);
		try {
			this.template = new JSONObject(template);
			// Multisheet management. For retrocompatibility add new sheet section if isn't yet presents.
			JSONObject widgetsConfJSON = this.template.optJSONObject("widgetsConf");
			if (widgetsConfJSON != null && widgetsConfJSON.get("widgets") != null) {
				logger.debug("Cockpit template is in old version, converting it to multisheet format...");
				JSONObject sheetTemplate = new JSONObject();
				JSONArray sheets = new JSONArray();
				JSONObject sheet = new JSONObject();
				sheet.put("sheetId", "Sheet1");
				sheet.put("sheetTitle", "Sheet1");
				sheet.put("sheetConf", widgetsConfJSON);
				sheets.put(sheet);
				sheetTemplate.put("widgetsConf", sheets);
				// completes new template with ohter generic settings (about stores, layout,..)
				JSONObject storeConfJSON = (JSONObject) this.template.get("storesConf");
				sheetTemplate.put("storesConf", storeConfJSON);
				// update internal template variable with the new one
				this.template = sheetTemplate;
			}
			this.associationManager = new AssociationManager();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to parse template", e);
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
		return (Integer) this.getEnv().get(EngineConstants.ENV_DOCUMENT_ID);
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
		for (int i = 0; i < lstEnvVariables.length; i++) {
			if (lstEnvVariables[i].equalsIgnoreCase(parName)) {
				return false;
			}
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
