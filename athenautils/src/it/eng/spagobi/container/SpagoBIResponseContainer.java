/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIResponseContainer 
extends AbstractContainer 
implements IWriteOnlyContainer {
   
	private Logger logger = Logger.getLogger(SpagoBIResponseContainer.class);
	
	private SourceBean response;
	
	public SpagoBIResponseContainer(SourceBean response) {
		if (response == null) {
			logger.error("SourceBean request is null. " +
					"Cannot initialize " + this.getClass().getName() + "  instance");
			throw new ExceptionInInitializerError("SourceBean request in input is null");
		}
		setResponse( response );
	}
	
	private void setResponse(SourceBean r) {
		response = r;
	}
	
	private SourceBean getResponse() {
		return response;
	}
	
	public Object get(String key) {
		return getResponse().getAttribute(key);
	}

	public List getKeys() {
		logger.debug("IN");
		List toReturn = new ArrayList();
		List list = getResponse().getContainedAttributes();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			SourceBeanAttribute sba = (SourceBeanAttribute) it.next();
			String key = sba.getKey();
			toReturn.add(key);
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void remove(String key) {
		try {
			getResponse().delAttribute(key);
		} catch (SourceBeanException e) {
			logger.error(e);
		}
	}

	public void set(String key, Object value) {
		try {
			getResponse().setAttribute(key, value);
		} catch (SourceBeanException e) {
			logger.error(e);
		}
		
	}
}
