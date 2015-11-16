/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @class
 * Container of the HTTPSession..
 *  
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;

public class HttpSessionContainer implements IBeanContainer{

	private HttpSession session;
	
	public HttpSessionContainer(HttpSession session){
		this.session = session;
	}
	
	public boolean isNull(String key) {
		Object o = session.getAttribute(key);
		return o==null;
	}

	public boolean isBlankOrNull(String key) {
		Object o = session.getAttribute(key);
		return o==null || (o instanceof String && ((String)o).trim().equals(""));
	}

	public Object get(String key) {
		return session.getAttribute(key);
	}

	public String getString(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return o.toString();
		}
		return null;
	}

	public Boolean getBoolean(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toBoolean(o);
		}
		return null;
	}

	public Integer getInteger(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toInteger(o);
		}
		return null;
	}

	public List getList(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toList(o);
		}
		return null;
	}

	public List toCsvList(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toCsvList(o);
		}
		return null;
	}

	public JSONObject toJSONObject(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toJSONObject(o);
		}
		return null;
	}

	public List getKeys() {
		if(session.getAttributeNames()!=null){
			return Arrays.asList(session.getAttributeNames());
		}
		return new ArrayList<Object>();
	}

	public void set(String key, Object value) {
		session.setAttribute(key,value);
	}

	public void remove(String key) {
		session.removeAttribute(key);
	}
}