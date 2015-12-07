
/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
