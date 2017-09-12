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
