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
	
	@Override
	public boolean isNull(String key) {
		Object o = session.getAttribute(key);
		return o==null;
	}

	@Override
	public boolean isBlankOrNull(String key) {
		Object o = session.getAttribute(key);
		return o==null || (o instanceof String && ((String)o).trim().equals(""));
	}

	@Override
	public Object get(String key) {
		return session.getAttribute(key);
	}

	@Override
	public String getString(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return o.toString();
		}
		return null;
	}

	@Override
	public Boolean getBoolean(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toBoolean(o);
		}
		return null;
	}

	@Override
	public Integer getInteger(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toInteger(o);
		}
		return null;
	}

	@Override
	public List getList(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toList(o);
		}
		return null;
	}

	@Override
	public List toCsvList(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toCsvList(o);
		}
		return null;
	}

	@Override
	public JSONObject toJSONObject(String key) {
		Object o = session.getAttribute(key);
		if(o!=null){
			return ObjectUtils.toJSONObject(o);
		}
		return null;
	}

	@Override
	public List getKeys() {
		if(session.getAttributeNames()!=null){
			return Arrays.asList(session.getAttributeNames());
		}
		return new ArrayList<>();
	}

	@Override
	public void set(String key, Object value) {
		session.setAttribute(key,value);
	}

	@Override
	public void remove(String key) {
		session.removeAttribute(key);
	}
}