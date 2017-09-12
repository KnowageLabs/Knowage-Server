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
package it.eng.spagobi.analiticalmodel.document.bo;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * This object can execute a data
 */
public class DataSetExecutorForBIObject {
	
	private static Logger logger = Logger.getLogger(DataSetExecutorForBIObject.class);
	
	IDataSet dataSet;
	BIObject biObject;
	IEngUserProfile profile;
	
	/**
	 * @param dataSet: IDataSet the data set to execute
	 * @param biObject: BIObject the object
	 * @param profile: IEngUserProfile the user profile
	 */
	public DataSetExecutorForBIObject(IDataSet dataSet, BIObject biObject, IEngUserProfile profile) {
		super();
		this.dataSet = dataSet;
		this.biObject = biObject;
		this.profile = profile;
	}
	
	/**
	 * Executes the dataset with the parameters of the BIObject
	 * @return IDataStore
	 */
	public IDataStore executeDataSet(){
		Assert.assertNotNull(dataSet, "The IDataSet must be specified before the execution of this method");
		Assert.assertNotNull(biObject, "The BIObject must be specified before the execution of this method");
		Assert.assertNotNull(biObject, "The user profile  must be specified before the execution of this method");
		
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes( (UserProfile) profile));
		dataSet.setParamsMap(getParameters());
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		return dataStore;
	}
	
	private Map getParameters(){
		HashMap parametersMap=null;

		//Search if the chart has parameters
		List parametersList=biObject.getBiObjectParameters();
		logger.debug("Check for BIparameters and relative values");
		if(parametersList!=null){
			parametersMap=new HashMap();
			for (Iterator iterator = parametersList.iterator(); iterator.hasNext();) {
				BIObjectParameter par= (BIObjectParameter) iterator.next();
				String url=par.getParameterUrlName();
				List values=par.getParameterValues();
				if(values!=null){
					if(values.size()==1){
						String value=(String)values.get(0);
						Parameter parameter = par.getParameter();
						if(parameter != null){
							String parType = parameter.getType();
							if(parType.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || parType.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)){
								value=value;
							}
						}
						parametersMap.put(url, value);
					}else if(values.size() >=1){
						String type = (par.getParameter() != null) ? par.getParameter().getType() : SpagoBIConstants.STRING_TYPE_FILTER;
						// if par is a string or a date close with '', else not
						String value = "";
						if(type.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER) || type.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER)){
							value = "'" + (String)values.get(0) + "'";
							for(int k = 1; k< values.size() ; k++){
								value = value + ",'" + (String)values.get(k) + "'";
							}
						}
						else{
							value = (String)values.get(0);
							for(int k = 1; k< values.size() ; k++){
								value = value + "," + (String)values.get(k)+"";
							}							
						}


						parametersMap.put(url, value);
					}
				}
			}	

		} // end looking for parameters
		return parametersMap;
	}
	

}
