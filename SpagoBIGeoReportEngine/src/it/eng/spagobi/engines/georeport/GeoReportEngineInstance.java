/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
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

import org.json.JSONObject;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngineInstance extends AbstractEngineInstance {
	//ENVIRONMENT VARIABLES
	private String[] lstEnvVariables = {"SBI_EXECUTION_ID", "SBICONTEXT", "SBI_COUNTRY", "SBI_LANGUAGE", 
										"SBI_SPAGO_CONTROLLER",  "SBI_EXECUTION_ROLE", "SBI_HOST", 
										"DOCUMENT_ID", "country", "language", "user_id" };
	private JSONObject guiSettings;
	private JSONObject docProperties;
	private List<String> includes;

	public GeoReportEngineInstance(String template, Map env) {
		super( env );	
		try {
			this.guiSettings = new JSONObject(template);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to parse template", t);
		}
		
		includes = GeoReportEngine.getConfig().getIncludes();
	}
	

	public JSONObject getGuiSettings() {
		return guiSettings;
	}
	
	public List getIncludes() {
		return includes;
	}
	
	public JSONObject getDocProperties() {
		return docProperties;
	}


	public void setDocProperties(JSONObject docProperties) {
		this.docProperties = docProperties;
	}


	public IDataSource getDataSource() {
		return (IDataSource)this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	}
	
	public IDataSet getDataSet() {
		return (IDataSet)this.getEnv().get(EngineConstants.ENV_DATASET);
	}
	
	public Locale getLocale() {
		return (Locale)this.getEnv().get(EngineConstants.ENV_LOCALE);
	}
	
	public String getDocumentLabel() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_LABEL);
	}
	
	public String getDocumentVersion() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_VERSION);
	}
	
	public String getDocumentAuthor() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_AUTHOR);
	}
	
	public String getDocumentUser() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_USER);
	}
	
	public String getDocumentName() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_NAME);
	}
	
	public String getDocumentDescription() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_DESCRIPTION);
	}
	
	public String getDocumentIsVisible() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_VISIBLE
				);
	}
	
	public String getDocumentPreviewFile() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_PREVIEW_FILE
				);
	}
	
	public IEngUserProfile getUserProfile() {
		return (IEngUserProfile)this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	}
	
	
	
	public String[] getDocumentCommunities() {
		try{
			String strCommunities = (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_COMMUNITIES);
			if (strCommunities == null) 
				return null;
			else
				return JSONUtils.asStringArray(JSONUtils.toJSONArray(strCommunities));
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to get communities list", t);		}
	}

	public List<Integer> getDocumentFunctionalities() {
		try{
			String strFunctionalities = (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_FUNCTIONALITIES);
			if (strFunctionalities == null) 
				return null;
			else
				return JSONUtils.asList(JSONUtils.toJSONArray(strFunctionalities));
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to get functionalities list", t);		}
	}
	
	public String getDocumentIsPublic() {
		return (String)this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_PUBLIC);
	}
	
	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy)this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}
	
	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy)this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}
	
	public Map getAnalyticalDrivers() {
		Map toReturn = new HashMap();
		Iterator it = getEnv().keySet().iterator();
		while(it.hasNext()) {
			String parameterName = (String)it.next();
			Object parameterValue = (Object) getEnv().get(parameterName);

			if (parameterValue != null && 
				parameterValue.getClass().getName().equals("java.lang.String") && //test necessary for don't pass complex objects like proxy,...
				isAnalyticalDriver(parameterName)){
				toReturn.put(parameterName, parameterValue);
			}
		}
		return toReturn;
	}
	
	private boolean isAnalyticalDriver (String parName){
		for (int i=0; i < lstEnvVariables.length; i++){
			if (lstEnvVariables[i].equalsIgnoreCase(parName)){
				return false;
			}
		}
		return true;
	}
	

	
	// -- unimplemented methods ------------------------------------------------------------

	public IEngineAnalysisState getAnalysisState() {
		throw new GeoReportEngineRuntimeException("Unsupported method [getAnalysisState]");
	}


	public void setAnalysisState(IEngineAnalysisState analysisState) {
		throw new GeoReportEngineRuntimeException("Unsupported method [setAnalysisState]");		
	}


	public void validate() throws SpagoBIEngineException {
		throw new GeoReportEngineRuntimeException("Unsupported method [validate]");		
	}
	
	public boolean isVisibleDataSet(){
		IDataSet datSet = getDataSet();
    	if(datSet!=null){
    		IEngUserProfile profile = getUserProfile();
    		return DataSetUtilities.isExecutableByUser(datSet, profile);
    	}
    	return true;
	}
	
	
}
