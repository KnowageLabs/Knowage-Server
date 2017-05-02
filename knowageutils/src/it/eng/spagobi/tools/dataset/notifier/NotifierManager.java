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
