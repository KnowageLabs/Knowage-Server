/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.ScriptDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.XmlDataReader;
import it.eng.spagobi.utilities.json.JSONUtils;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class ScriptDataSet extends ConfigurableDataSet {
	
	public static String DS_TYPE = "SbiScriptDataSet";
	public static final String SCRIPT = "Script";
	public static final String SCRIPT_LANGUAGE = "scriptLanguage";
	
	private static transient Logger logger = Logger.getLogger(ScriptDataSet.class);
	
	public ScriptDataSet() {
		super();
		setDataProxy( new ScriptDataProxy() );
		setDataReader( new XmlDataReader() );
		addBehaviour( new QuerableBehaviour(this) );
	}
	
	public ScriptDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		
		setDataProxy( new ScriptDataProxy() );
		setDataReader( new XmlDataReader() );
		addBehaviour( new QuerableBehaviour(this) );
		try{
    		//JSONObject jsonConf  = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
    		String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());		
    		JSONObject jsonConf  = ObjectUtils.toJSONObject(config);
    		this.setScript((jsonConf.get(SCRIPT) != null)?jsonConf.get(SCRIPT).toString():"");        	
    		this.setScriptLanguage((jsonConf.get(SCRIPT_LANGUAGE) != null)?jsonConf.get(SCRIPT_LANGUAGE).toString():"");
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		//setScript( dataSetConfig.getScript() );
		//setScriptLanguage(dataSetConfig.getLanguageScript());
		
	}
	
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;
		
		sbd = super.toSpagoBiDataSet();	
		
		sbd.setType( DS_TYPE );		
		/*next informations are already loaded in method super.toSpagoBiDataSet() through the table field configuration
		try{
			JSONObject jsonConf  = new JSONObject();
			jsonConf.put(SCRIPT,  getScript() );	
			jsonConf.put(SCRIPT_LANGUAGE,  getScriptLanguage() );	
			sbd.setConfiguration(jsonConf.toString());
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		//sbd.setScript( getScript() );
		//sbd.setLanguageScript(getScriptLanguage());
		*/
		return sbd;
	}

	public ScriptDataProxy getDataProxy() {
		IDataProxy dataProxy;
		
		dataProxy = super.getDataProxy();
		
		if(dataProxy == null) {
			setDataProxy( new ScriptDataProxy() );
			dataProxy = getDataProxy();
		}
		
		if(!(dataProxy instanceof  ScriptDataProxy)) throw new RuntimeException("DataProxy cannot be of type [" + 
				dataProxy.getClass().getName() + "] in FileDataSet");
		
		return (ScriptDataProxy)dataProxy;
	}
	
	public void setScript(String script) {
		getDataProxy().setStatement(script);
	}
	
	public String getScript() {
		return getDataProxy().getStatement();
	}
	
	public void setScriptLanguage(String language){
		getDataProxy().setLanguage(language);
	}
	
	public String getScriptLanguage(){
		return getDataProxy().getLanguage();
	}
	
	public String getSignature() {
		return this.getDataProxy().getStatement();
	}
	
	

}
