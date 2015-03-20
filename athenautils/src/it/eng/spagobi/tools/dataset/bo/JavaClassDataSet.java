/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JavaClassDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.XmlDataReader;
import it.eng.spagobi.utilities.json.JSONUtils;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class JavaClassDataSet extends ConfigurableDataSet {
	 
	public static String DS_TYPE = "SbiJClassDataSet";
	public static final String JCLASS_NAME = "jClassName";
	
	private static transient Logger logger = Logger.getLogger(JavaClassDataSet.class);
	 
	
	public JavaClassDataSet() {
		super();
		setDataProxy( new JavaClassDataProxy() );
		setDataReader( new XmlDataReader() );
	}
	
	public JavaClassDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		setDataProxy( new JavaClassDataProxy() );
		setDataReader( new XmlDataReader() );		
		
		try{
    		//JSONObject jsonConf  = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
    		String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());		
    		JSONObject jsonConf  = ObjectUtils.toJSONObject(config);
    		this.setClassName((jsonConf.get(JCLASS_NAME) != null)?jsonConf.get(JCLASS_NAME).toString():"");        	
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
	//	setClassName( dataSetConfig.getJavaClassName() );
	}
	
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;
		
		sbd = super.toSpagoBiDataSet();
		
		sbd.setType( DS_TYPE );
		/*next informations are already loaded in method super.toSpagoBiDataSet() through the table field configuration
		try{
			JSONObject jsonConf  = new JSONObject();		
			jsonConf.put(JCLASS_NAME, getClassName());
			sbd.setConfiguration(jsonConf.toString());
		}catch (Exception e){
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		//sbd.setJavaClassName( getClassName() );
		*/
		return sbd;
	}
	
	public JavaClassDataProxy getDataProxy() {
		IDataProxy dataProxy;
		
		dataProxy = super.getDataProxy();
		
		if(dataProxy == null) {
			setDataProxy( new JavaClassDataProxy() );
			dataProxy = getDataProxy();
		}
		
		if(!(dataProxy instanceof  JavaClassDataProxy)) throw new RuntimeException("DataProxy cannot be of type [" + 
				dataProxy.getClass().getName() + "] in FileDataSet");
		
		return (JavaClassDataProxy)dataProxy;
	}

	public void setClassName(String className) {
		getDataProxy().setClassName(className);
	}
	
	public String getClassName() {
		return getDataProxy().getClassName();
	}
	
	
	 
}
