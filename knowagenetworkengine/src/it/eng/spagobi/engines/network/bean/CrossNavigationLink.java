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

package it.eng.spagobi.engines.network.bean;

import it.eng.spagobi.engines.network.NetworkEngineRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class CrossNavigationLink {

	private String document;//name of the linked document
	private String target = "self";//update, self, popup
	private List<CrossNavigationParameter> parameters;//list of parameters

	public static transient Logger logger = Logger.getLogger(CrossNavigationLink.class);
	
	

	public CrossNavigationLink(String document) {
		super();
		this.document=document;
		parameters= new ArrayList<CrossNavigationParameter>();
	}

	public void addParameter(String name, String value, String type, String property){
		parameters.add(new CrossNavigationParameter(name, value, type, property));
	}
	
	/**
	 * This enum represent the types of cross navigation links
	 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
	 *
	 */
	public enum CrossNavigationParameterType {
		EDGE("EDGE"), NODE("NODE"), ABSOLUTE("ABSOLUTE"), RELATIVE("RELATIVE");
		CrossNavigationParameterType(String value){
			this.value = value;
		}
		private String value;

		public String getValue(){
			return this.value;
		}
		
		public static CrossNavigationParameterType getCrossNavigationParameterType(String type){
			CrossNavigationParameterType[] values = CrossNavigationParameterType.values();
			for (int i = 0; i < values.length; i++) {
				if(values[i].getValue().equalsIgnoreCase(type)){
					return values[i];
				}
			}
			logger.error("The cross navigation parameter type" + type+ " is not allowed.");
			throw new NetworkEngineRuntimeException("The cross navigation parameter type" + type+ " is not allowed.");
		}
	}
	
	/**
	 * 
	 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
	 *
	 *A parameter link can have these attributes:
	 *- name (mandatory)
	 *- type (mandatory)
	 *- value (optional). This is the absolute value of the parameter
	 *- property (optional). This is the property of the edge or node that contains the value for the parameter
	 */
	public class CrossNavigationParameter{
		private String name;
		private String value;
		private String property;
		private CrossNavigationParameterType type;

		public CrossNavigationParameter(String name, String value, String type, String property) {
			super();
			this.name = name;
			this.value = value;
			this.property = property;
			this.type = CrossNavigationParameterType.getCrossNavigationParameterType(type);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public CrossNavigationParameterType getType() {
			return type;
		}
		public void setType(String type) {
			this.type = CrossNavigationParameterType.getCrossNavigationParameterType(type);
		}
		public String getProperty() {
			if(property==null){
				return "id";
			}
			return property;
		}
		public void setProperty(String property) {
			this.property = property;
		}
	}
	
	/**
	 * Get the map (name/value) for the fixed parameters.
	 * The fixed parameters are the ones with type RELATIVE and ABSOLUTE
	 * @return
	 */
	public Map<String, String> getFixedParameters(){
		CrossNavigationParameter parameter;
		Map<String, String> fixedParameter = new HashMap<String, String>();
		for(int i=0; i<parameters.size(); i++){
			parameter = parameters.get(i);
			if(parameter.getType().equals(CrossNavigationParameterType.RELATIVE) || parameter.getType().equals(CrossNavigationParameterType.ABSOLUTE) ){
				fixedParameter.put(StringEscapeUtils.escapeHtml(parameter.getName()), StringEscapeUtils.escapeHtml(parameter.getValue()));
			}
		}
		return fixedParameter;
	}
	
	/**
	 * Get the map (type,(parameter,property)) for the dynamic parameters.
	 * The dynamic parameters are the ones with type EDGE and NODE.
	 * We need these three informations to create the cross navigation link.
	 * Type: to understand the event
	 * Parameter: name of the parameter
	 * Property: the property of the object to bind
	 * @return
	 */
	public Map<String, Map<String,String>> getDynamicParameters(){
		CrossNavigationParameter parameter;
		Map<String,String> edgesList = new HashMap<String,String>();
		Map<String,String> nodesList = new HashMap<String,String>();
		Map<String, Map<String,String>> dynamicParameter = new HashMap<String, Map<String,String>>();
		for(int i=0; i<parameters.size(); i++){
			parameter = parameters.get(i);
			if(parameter.getType().equals(CrossNavigationParameterType.EDGE)){
				edgesList.put( StringEscapeUtils.escapeHtml(parameter.getProperty()),StringEscapeUtils.escapeHtml(parameter.getName()));
			}else if(parameter.getType().equals(CrossNavigationParameterType.NODE)){
				nodesList.put( StringEscapeUtils.escapeHtml(parameter.getProperty()),StringEscapeUtils.escapeHtml(parameter.getName()));
			}
		}
		dynamicParameter.put(CrossNavigationParameterType.EDGE.getValue(),edgesList);
		dynamicParameter.put(CrossNavigationParameterType.NODE.getValue(),nodesList);
		return dynamicParameter;
	}

	public String getDocument() {
		return document;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	
}
