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
