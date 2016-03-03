
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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * 
 */
@SuppressWarnings("serial")
public class NotifierServlet extends HttpServlet {

	private static final Logger log=Logger.getLogger(NotifierServlet.class);
	
	private static String notifyUrl;

	public static String getNotifyUrl() {
		return notifyUrl;
	}

	public static boolean isNotifiable() {
		return notifyUrl != null;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		notifyUrl=config.getInitParameter("notifyUrl");
		if (notifyUrl==null) {
			log.warn("Notify URL not specified");
		}
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		NotifierManager manager = NotifierManagerFactory.getManager();
		manager.manage(req, resp);
	}

}
