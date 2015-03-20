/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;



/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */
 

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Defines method to manage dataset parametes
 */
public class DataSetParametersList {
    
	/**
	 * items of the list
	 */
	List items = new ArrayList();
	private static transient Logger logger=Logger.getLogger(DataSetParametersList.class);	
	
	/**
	 * constructor.
	 */
	public DataSetParametersList() {
	} 
	
	/**
	 * constructor.
	 * 
	 * @param dataDefinition the data definition
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public DataSetParametersList(String dataDefinition) throws SourceBeanException {
		loadFromXML(dataDefinition);
	}
	
	/**
	 * loads the lov from an xml string.
	 * 
	 * @param dataDefinition the xml definition of the lov
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public void  loadFromXML (String dataDefinition) throws SourceBeanException {
		logger.debug("IN");
		dataDefinition.trim();
		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		if(!source.getName().equals("PARAMETERSLIST")) {
			SourceBean wrapper = new SourceBean("PARAMETERSLIST");
			wrapper.setAttribute(source);	
			source = wrapper;
		}
		// load data from xml
		List listRows = source.getAttributeAsList("ROWS.ROW");
		Iterator iterRows = listRows.iterator();
		ArrayList parsList = new ArrayList();
		while(iterRows.hasNext()){
			DataSetParameterItem par = new DataSetParameterItem();
			SourceBean element = (SourceBean)iterRows.next();
			String name = (String)element.getAttribute("NAME");
			par.setName(name);
			String type = (String)element.getAttribute("TYPE");
			par.setType(type);
			parsList.add(par);
		}
 		setPars(parsList);
 		logger.debug("OUT");
	}	
	
	/**
	 * serialize the lov to an xml string.
	 * 
	 * @return the serialized xml string
	 */
	public String toXML() {
		logger.debug("IN");
		String lovXML = "";
		lovXML += "<PARAMETERSLIST>";
		lovXML += "<ROWS>";
		DataSetParameterItem lov = null;
		Iterator iter = items.iterator();
		while(iter.hasNext()){
			lov = (DataSetParameterItem)iter.next();
			String name = lov.getName();
			String type = lov.getType();
			lovXML += "<ROW" +
					  " NAME=\"" + name + "\"" +
					  " TYPE=\"" + type + "\"" +
					  "/>";
		}
		lovXML += "</ROWS></PARAMETERSLIST>";
		logger.debug("OUT");
		return lovXML;
	}
	
	/**
	 * Returns the result of the Dataset using a user profile to fill the Datase profile attribute.
	 * 
	 * @param profile the profile of the user
	 * 
	 * @return the string result of the lov
	 * 
	 * @throws Exception the exception
	 */
	public String getDataSetResult(IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		String lovResult = "<ROWS>";
		DataSetParameterItem lov = null;
		Iterator iter = items.iterator();
		while(iter.hasNext()){
			lov = (DataSetParameterItem)iter.next();
			String name = lov.getName();
			String type = lov.getType();
			lovResult += "<ROW" +
					  " NAME=\"" + name + "\"" +
					  " TYPE=\"" + type + "\"" +
					  "/>";
		}
		lovResult += "</ROWS>";
		lovResult = StringUtilities.substituteProfileAttributesInString(lovResult, profile);
		logger.debug("OUT");
		return lovResult;
	}
		

	/**
	 * Gets the list of names of the profile attributes required.
	 * 
	 * @return list of profile attribute names
	 * 
	 * @throws Exception the exception
	 */
	public List getProfileAttributeNames() throws Exception {
		logger.debug("IN");
		List names = new ArrayList();
		String lovResult = this.toXML();
		while(lovResult.indexOf("$P{")!=-1) {
			int startind = lovResult.indexOf("$P{");
			int endind = lovResult.indexOf("}", startind);
			String attributeDef = lovResult.substring(startind + 2, endind);
			if(attributeDef.indexOf("(")!=-1) {
				int indroundBrack = lovResult.indexOf("(", startind);
				String nameAttr = lovResult.substring(startind+2, indroundBrack);
				names.add(nameAttr);
			} else {
				names.add(attributeDef);
			}
			lovResult = lovResult.substring(endind);
		}
		logger.debug("OUT");
		return names;
	}

	/**
	 * Checks if the dataset requires one or more profile attributes.
	 * 
	 * @return true if the dataset require one or more profile attributes, false otherwise
	 * 
	 * @throws Exception the exception
	 */
	public boolean requireProfileAttributes() throws Exception {
		boolean contains = false;
		String lovResult = this.toXML();
		if(lovResult.indexOf("${")!=-1) {
			contains = true;
		}
		return contains;
	}	
	
	
	/**
	 * Adds a dataset to the dataset Detail List.
	 * 
	 * @param name The dataset lov name
	 * @param type the type
	 */
	public void add(String name, String type) {
		// if name or description are empty don't add
		if((name==null) || (name.trim().equals("")))
				return;
		if((type==null) || (type.trim().equals("")))
			return;
		// if the element already exists don't add
		Iterator iter = items.iterator();
		while(iter.hasNext()) {
			DataSetParameterItem lovDet = (DataSetParameterItem)iter.next();
			if(name.equals(lovDet.getName()) && type.equals(lovDet.getType())) {
				return;
			}
		}
		// add the item
		DataSetParameterItem item = new DataSetParameterItem();
		item.setName(name);
		item.setType(type);
		items.add(item);
	}
	
	
	/**
	 * Deletes a dataset from the dataset Detail List.
	 * 
	 * @param name the name
	 * @param type the type
	 */
	public void remove(String name, String type) {
		Iterator iter = items.iterator();
		while(iter.hasNext()) {
			DataSetParameterItem lovDet = (DataSetParameterItem)iter.next();
			if(name.equals(lovDet.getName()) && type.equals(lovDet.getType())) {
				items.remove(lovDet);
				break;
			}
		}
	}
	
	
	/**
	 * Splits an XML string by using some <code>SourceBean</code> object methods
	 * in order to obtain the source <code>LovDetail</code> objects whom XML has been
	 * built.
	 * 
	 * @param dataDefinition The XML input String
	 * 
	 * @return The corrispondent <code>DatasetParameterList</code> object
	 * 
	 * @throws SourceBeanException If a SourceBean Exception occurred
	 */
	public static DataSetParametersList  fromXML (String dataDefinition) throws SourceBeanException {
		return new DataSetParametersList(dataDefinition);	
	}
	
	
	/**
	 * Gets item of the fixed list.
	 * 
	 * @return items of the fixed list
	 */
	public List getItems() {
		return items;
	}
	
	/**
	 * Sets items of the fixed list.
	 * 
	 * @param items the items to set
	 */
	public void setPars(List items) {
		this.items = items;
	}
	

	
}
