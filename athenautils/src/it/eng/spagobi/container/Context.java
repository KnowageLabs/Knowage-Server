/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class has an objects container (a simple Map) and a last usage date.
 * Objects are stored with a key that is a String.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class Context implements Serializable{
	
	private Calendar _lastUsageDate;
	private Map _container;
	
	public Context() {
		_lastUsageDate = new GregorianCalendar();
		_container = new HashMap();
	}
	
	/**
	 * Updates last usage date
	 */
	private void updateLastUsageDate() {
		_lastUsageDate = new GregorianCalendar();
	}
	
	public Calendar getLastUsageDate() {
		return _lastUsageDate;
	}
	
	public Object get(String key) {
		updateLastUsageDate();
		return _container.get(key);
	}
	
	/**
	 * Returns all the string keys of the stored objects as a list
	 * @return all the string keys of the stored objects as a list
	 */
	public List getKeys() {
		updateLastUsageDate();
		List toReturn = new ArrayList();
		Set keys = _container.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			toReturn.add(key);
		}
		return toReturn;
	}
	
	public void set(String key, Object object) {
		updateLastUsageDate();
		_container.put(key, object);
	}
	
	public void remove(String key) {
		updateLastUsageDate();
		_container.remove(key);
	}
	
	/**
	 * Return true if this instance is older than the minutes given as input, i.e. last usage date is before the given minutes number
	 * @param minutes The number of minutes
	 * @return true if this instance is older than the minutes given as input, i.e. last usage date is before the given minutes number
	 */
	public boolean isOlderThan(int minutes) {
		Calendar now = new GregorianCalendar();
    	Calendar someTimeAgo = new GregorianCalendar();
    	someTimeAgo.set(Calendar.MINUTE, now.get(Calendar.MINUTE) - minutes);
    	return _lastUsageDate.before(someTimeAgo);
	}
	
}
