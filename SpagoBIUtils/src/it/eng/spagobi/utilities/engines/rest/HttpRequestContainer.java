/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @class
 * Container of the HTTPServletRequest
 * 
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.container.IContainer;
import it.eng.spagobi.container.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequestContainer implements IContainer{
	private HttpServletRequest request;
	
	public HttpRequestContainer(HttpServletRequest request) {
		this.request =request;  
	}

	public boolean isNull(String key) {
		Object o = request.getParameter(key);
		return o==null;
	}

	public boolean isBlankOrNull(String key) {
		Object o = request.getParameter(key);
		return o==null || (o instanceof String && ((String)o).trim().equals(""));
	}

	public Object get(String key) {
		return request.getParameter(key);
	}

	public String getString(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return o.toString();
		}
		return null;
	}

	public Boolean getBoolean(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return ObjectUtils.toBoolean(o);
		}
		return null;
	}

	public Integer getInteger(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return ObjectUtils.toInteger(o);
		}
		return null;
	}

	public List getList(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return ObjectUtils.toList(o);
		}
		return null;
	}

	public List toCsvList(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return ObjectUtils.toCsvList(o);
		}
		return null;
	}

	public JSONObject toJSONObject(String key) {
		Object o = request.getParameter(key);
		if(o!=null){
			return ObjectUtils.toJSONObject(o);
		}
		return null;
	}

	public List getKeys() {
		if(request.getParameterMap()!=null && request.getParameterMap().keySet()!=null){
			return Arrays.asList(request.getParameterMap().keySet().toArray());
		}
		return new ArrayList<Object>();
	}
	
	public JSONArray toJSONArray(String key) {
		if( isNull(key) ) return null;
		return ObjectUtils.toJSONArray( get(key) );
	}
	public List toList(String key) {
		if( isNull(key) ) return null;
		return ObjectUtils.toList( get(key) );
	}

	public void set(String key, Object value) {}

	public void remove(String key) {}
}
