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
package it.eng.spagobi.engines.chart.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class is used in DatasetMap.java it creates a Map with all parameters from request o response
 *
 * @author gavardi
 *
 */

public class AttributesContainer {

	HashMap<String, Object> parameters;

	// these are attributes to query in request and response
	String[] attributesToQuery = new String[] { "n_visualization", "categoryAll" };
	String[] attributesToQueryList = new String[] { "category", "serie", "cat_group" };

	static protected Logger logger = Logger.getLogger(AttributesContainer.class);

	public HashMap<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * insert all parameters from the request
	 * 
	 * @param requestContainer
	 */

	public AttributesContainer(javax.servlet.http.HttpServletRequest request) {
		super();
		parameters = new HashMap<String, Object>();

		for (int i = 0; i < attributesToQuery.length; i++) {
			String att = attributesToQuery[i];
			Object value = request.getParameter(att);
			if (value != null) {
				logger.debug(value);
				parameters.put(att, value);
			}
		}

		for (int i = 0; i < attributesToQueryList.length; i++) {
			String att = attributesToQueryList[i];
			Object[] value = request.getParameterValues(att);
			if (value != null) {
				// logger.debug(value);
				parameters.put(att, value);
			}
		}
	}

	public AttributesContainer(SourceBean serviceResponse) {
		super();

		if (serviceResponse == null)
			return;

		parameters = new HashMap<String, Object>();
		List parameterNames = serviceResponse.getContainedAttributes();

		for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
			SourceBeanAttribute sbA = (SourceBeanAttribute) iterator.next();
			String name = sbA.getKey();
			String value = sbA.getValue().toString();
			// if already contained create a List
			if (parameters.keySet().contains(name)) {
				Object prevValue = parameters.get(name);
				if (prevValue instanceof Object[]) {
					Object[] ooo = ((Object[]) prevValue);
					// ooo[ooo.length]=value;
					// add a position
					Object[] newOoo = new Object[ooo.length + 1];
					for (int i = 0; i < ooo.length; i++) {
						newOoo[i] = ooo[i];
					}
					newOoo[ooo.length] = value;
					parameters.put(name, newOoo);
				} else {
					Object[] toInsert = new Object[2];
					toInsert[0] = (prevValue);
					toInsert[1] = (value);
					parameters.put(name, toInsert);
				}
			} else { // single value
				parameters.put(name, value);
			}

		}
	}

	// build it from a MAP, it can contain list that must be translated into array

	public AttributesContainer(Map previousMap) {
		super();
		parameters = new HashMap<String, Object>();
		for (Iterator iterator = previousMap.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Object value = previousMap.get(name);

			if (value instanceof List) {
				Object[] toInsert = new Object[((List) value).size()];
				int i = 0;
				for (Iterator iterator2 = ((List) value).iterator(); iterator2.hasNext();) {
					String n = (String) iterator2.next();
					toInsert[i] = n;
					i++;
				}

			} else {
				parameters.put(name, value);
			}

		}
	}

	// Enumeration parameterNamesEn=request.getAttributeNames();
	// Vector parameterNames=new Vector();
	// while(parameterNamesEn.hasMoreElements()){
	// String name=parameterNamesEn.nextElement().toString();
	// parameterNames.add(name);
	// }
	// for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
	// String name = (String) iterator.next();
	// String value=request.getAttribute(name).toString();
	// // if already contained create a List
	// if(parameters.keySet().contains(name)){
	// Object prevValue=parameters.get(name);
	// if(prevValue instanceof List){
	// ((List)prevValue).add(value);
	// parameters.put(name, prevValue);
	// }
	// else{
	// List newList=new ArrayList();
	// newList.add(prevValue);
	// newList.add(value);
	// parameters.put(name, newList);
	// }
	// }
	// else{ // single value
	// parameters.put(name, value);
	// }
	// }
	// }

	// public AttributesContainer(SourceBean serviceResponse) {
	// super();

	// if(serviceResponse==null) return;

	// parameters=new HashMap<String, Object>();
	// List parameterNames=serviceResponse.getContainedAttributes();

	// for (Iterator iterator = parameterNames.iterator(); iterator.hasNext();) {
	// SourceBeanAttribute sbA = (SourceBeanAttribute) iterator.next();
	// String name = sbA.getKey();
	// String value=sbA.getValue().toString();
	// // if already contained create a List
	// if(parameters.keySet().contains(name)){
	// Object prevValue=parameters.get(name);
	// if(prevValue instanceof List){
	// ((List)prevValue).add(value);
	// parameters.put(name, prevValue);
	// }
	// else{
	// List newList=new ArrayList();
	// newList.add(prevValue);
	// newList.add(value);
	// parameters.put(name, newList);
	// }
	// }
	// else{ // single value
	// parameters.put(name, value);
	// }

	// }
	// }

	public Object getAttribute(String key) {
		if (parameters.containsKey(key)) {
			Object value = parameters.get(key);
			return value;
		} else
			return null;

	}

	public Object getParameter(String key) {
		if (parameters.containsKey(key)) {
			Object value = parameters.get(key);
			return value;
		} else
			return null;

	}

	public Object[] getParameterValues(String key) {

		if (parameters.containsKey(key)) {
			Object val = parameters.get(key);
			if (val instanceof Object[]) {
				return ((Object[]) val);
			} else {
				Object[] toRet = new Object[1];
				toRet[0] = val;
				return toRet;
			}
		} else
			return null;
	}

	public boolean isNull() {
		return (parameters == null);
	}

}
