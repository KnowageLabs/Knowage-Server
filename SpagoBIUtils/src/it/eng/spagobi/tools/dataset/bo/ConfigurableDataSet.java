/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class ConfigurableDataSet extends AbstractDataSet {

	IDataReader dataReader;
	IDataProxy dataProxy;
	IDataStore dataStore;
	
	protected boolean abortOnOverflow;	
	protected Map bindings;
	private boolean calculateResultNumberOnLoad = true;

	Map<String, Object> userProfileParameters;


	private static transient Logger logger = Logger.getLogger(ConfigurableDataSet.class);


	public ConfigurableDataSet(){
		super();
		userProfileParameters = new HashMap<String, Object>();
	}

	public ConfigurableDataSet(SpagoBiDataSet dataSetConfig){
		super(dataSetConfig);
		userProfileParameters = new HashMap<String, Object>();
	}
	
	/**utility method used to clean different parameters values that should be null
	 * @param params parameters map
	 * @return cleaned params map
	 */
	private Map cleanNullParametersValues(Map params){
		if (params == null) {
			return null;
		}
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object val = params.get(key);
			if(val instanceof String){
				if(val != null && (val.equals("") || val.equals("''"))){
					params.put(key, null);
				}
			}
		}
		
		return params;
	}

	public void loadData(int offset, int fetchSize, int maxResults) {
		
		if (this.isPersisted() || this.isFlatDataset()) {
			
			JDBCDataSet dataset = new JDBCDataSet();
			dataset.setDataSource(getDataSourceForReading());
			dataset.setQuery("select * from " + getTableNameForReading());
			dataset.loadData(offset, fetchSize, maxResults);
			dataStore = dataset.getDataStore();
			
		} else {
			
			Map parameters = cleanNullParametersValues(getParamsMap());
	
			dataProxy.setParameters(parameters);
	
			dataProxy.setProfile(getUserProfileAttributes());
			dataProxy.setResPath(resPath);
			
			// check if the proxy is able to manage results pagination
			if(dataProxy.isOffsetSupported()) {
				dataProxy.setOffset(offset);
			} else if(dataReader.isOffsetSupported()){
				dataReader.setOffset(offset);
			} else {
	
			}
	
			if(dataProxy.isFetchSizeSupported()) {
				dataProxy.setFetchSize(fetchSize);
			} else if(dataReader.isOffsetSupported()){
				dataReader.setFetchSize(fetchSize);
			} else {
	
			}
	
			// check if the proxy is able to manage results limit
			if(dataProxy.isMaxResultsSupported()) {
				dataProxy.setMaxResults(maxResults);
			} else if(dataReader.isOffsetSupported()){
				dataReader.setMaxResults(maxResults);
			} else {
	
			}
			
			
			if( hasBehaviour(QuerableBehaviour.class.getName()) ) { 
				QuerableBehaviour querableBehaviour = (QuerableBehaviour)getBehaviour(QuerableBehaviour.class.getName()) ;
				String stm = querableBehaviour.getStatement();
				//stm = stm.replaceAll("''", "'"); why????
				//                                 This line transforms, for example:
				//                                 .... where column = 'rock ''n'' roll'
				//                                 that is correct into 
				//                                  .... where column = 'rock 'n' roll' 
				//                                 THAT IS NOT CORRECT!!!
				//                                 Commenting out this line solves https://spagobi.eng.it/jira/browse/SPAGOBI-1697 Error in Smart Filter: an error occurs when a static open filter contains a single quote
				dataProxy.setStatement(stm);
			}
			
			dataProxy.setCalculateResultNumberOnLoad(this.isCalculateResultNumberOnLoadEnabled());
	
			dataStore = dataProxy.load(dataReader); 
	
	
			if(hasDataStoreTransformer()) {
				getDataStoreTransformer().transform(dataStore);
			}
		
		}
	}

	public IDataStore getDataStore() {    	
		return this.dataStore;
	}    

	

	/**
	 * Gets the list of names of the profile attributes required.
	 * 
	 * @return list of profile attribute names
	 * 
	 * @throws Exception the exception
	 */
	public List getProfileAttributeNames() throws Exception {
		List names = new ArrayList();
		String query = (String)getQuery();
		while(query.indexOf("${")!=-1) {
			int startind = query.indexOf("${");
			int endind = query.indexOf("}", startind);
			String attributeDef = query.substring(startind + 2, endind);
			if(attributeDef.indexOf("(")!=-1) {
				int indroundBrack = query.indexOf("(", startind);
				String nameAttr = query.substring(startind+2, indroundBrack);
				names.add(nameAttr);
			} else {
				names.add(attributeDef);
			}
			query = query.substring(endind);
		}
		return names;
	}

	public IDataReader getDataReader() {
		return dataReader;
	}

	public void setDataReader(IDataReader dataReader) {
		this.dataReader = dataReader;
	}

	public IDataProxy getDataProxy() {
		return dataProxy;
	}

	public void setDataProxy(IDataProxy dataProxy) {
		this.dataProxy = dataProxy;
	}

	public Map<String, Object> getUserProfileAttributes() {
		return userProfileParameters;
	}

	public void setUserProfileAttributes(Map<String, Object> parameters) {
		this.userProfileParameters = parameters;
	}

	public void setAbortOnOverflow(boolean abortOnOverflow) {
		this.abortOnOverflow = abortOnOverflow;
	}

	public void addBinding(String bindingName, Object bindingValue) {
		bindings.put(bindingName, bindingValue);
	}

	
	public IDataStore test() {
		logger.debug("IN");
		loadData();
		logger.debug("OUT");
		return getDataStore();
	}



	public IDataStore test(int offset, int fetchSize, int maxResults) {
		logger.debug("IN");
		loadData(offset, fetchSize, maxResults);
		logger.debug("OUT");
		return getDataStore();

	}

	public IDataStore decode(
			IDataStore datastore) {
		return datastore;
	}

	public boolean isCalculateResultNumberOnLoadEnabled() {
		return calculateResultNumberOnLoad;
	}

	public void setCalculateResultNumberOnLoad(boolean enabled) {
		calculateResultNumberOnLoad = enabled;
	}
	

	public String getSignature() {
		throw new UnreachableCodeException("getSignature method not implemented in class " + this.getClass().getName() + "!!!!");
	}

	public void setDataSource(IDataSource dataSource) {
		throw new UnreachableCodeException("setDataSource method not implemented in class " + this.getClass().getName() + "!!!!");
	}

	public IDataSource getDataSource() {
		throw new UnreachableCodeException("getDataSource method not implemented in class " + this.getClass().getName() + "!!!!");
	}

	
}
