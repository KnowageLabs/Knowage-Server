/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.notifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class NotifierManager {

	private final static Logger log = Logger.getLogger(NotifierManager.class);

	private final ConcurrentMap<Object, INotifierOperator> operators = new ConcurrentHashMap<Object, INotifierOperator>();


	public void addOperatorIfAbsent(Object id, INotifierOperator op) {
		operators.putIfAbsent(id, op);
	}

	public boolean removeOperator(Object id) {
		return operators.remove(id) != null;
	}

	public boolean containsOperator(Object operatorId) {
		return operators.containsKey(operatorId);
	}

	public void manage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body=getBody(req);
		for (INotifierOperator l : operators.values()) {
			try {
				l.notify(req, resp,body);
			} catch (Exception e) {
				log.error("Error on notification", e);
			}

		}
		
	}
	
	private static String getBody(HttpServletRequest request) throws IOException {
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		String data = buffer.toString();
		return data;
	}

}
