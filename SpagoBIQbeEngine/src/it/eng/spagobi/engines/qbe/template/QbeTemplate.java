/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.template;

import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.spagobi.engines.qbe.externalservices.ExternalServiceConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The Class QbeTemplate.
 * 
 * @author Andrea Gioia
 */
public class QbeTemplate {
	private boolean composite;	
	private Map dblinkMap;	
	private List datamartNames;	
	private IModelAccessModality datamartModelAccessModality;
	private String dialect;
	
	private Map properties;
	
	private Object rawData;	
	
	private List<ExternalServiceConfiguration> externalServicesConfiguration;
		
	public QbeTemplate() {
		datamartNames = new ArrayList();
		dblinkMap = new HashMap();
		properties = new HashMap();
		externalServicesConfiguration = new ArrayList();
	}
	
	public void addDatamartName(String name) {
		datamartNames.add(name);
	}
	
	public List getDatamartNames() {
		return datamartNames;
	}
	
	public void addExternalServiceConfiguration(ExternalServiceConfiguration c) {
		externalServicesConfiguration.add(c);
	}
	
	public List<ExternalServiceConfiguration> getExternalServiceConfigurations() {
		return externalServicesConfiguration;
	}
	
	public JSONArray getExternalServiceConfigurationsAsJSONArray() throws JSONException {
		JSONArray toReturn = new JSONArray();
		Iterator<ExternalServiceConfiguration> it = externalServicesConfiguration.iterator();
		while (it.hasNext()) {
			ExternalServiceConfiguration aServiceConfig = it.next();
			JSONObject obj = new JSONObject();
			obj.put("id", aServiceConfig.getId());
			obj.put("description", aServiceConfig.getDescription());
			toReturn.put(obj);
		}
		return toReturn;
	}
	
	public void setDbLink(String datamartName, String dblink) {
		dblinkMap.put(datamartName, dblink);
	}
	
	public Map getDbLinkMap() {
		return dblinkMap;
	}
	
	public void setDatamartModelAccessModality(QbeXMLModelAccessModality datamartModelAccessModality) {
		this.datamartModelAccessModality = datamartModelAccessModality;
	}
	
	public IModelAccessModality getDatamartModelAccessModality() {
		return datamartModelAccessModality;
	}
	
	public boolean isComposite() {
		return composite;
	}

	public void setComposite(boolean composite) {
		this.composite = composite;
	}
	
	public void setProperty(String pName, Object pValue) {
		properties.put(pName, pValue);
	}
	
	public Object getProperty(String pName) {
		return properties.get(pName);
	}

}
