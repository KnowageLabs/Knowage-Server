/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.engines.datamining.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Giorgio Federici (giorgio.federici@eng.it)
 *
 */
public class ExternalResourceServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -3195688127503072043L;

	public static transient Logger logger = Logger.getLogger(ExternalResourceServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		logger.debug("ExternalResourceServlet: Processing forward for external restful service..");

		try {
			String fileR = request.getParameter("fileR");
			// String searchId = request.getParameter("searchId");
			// String libs = request.getParameter("libraries");

			// fileR = "Sentiment_Analysis_Twitter_DB_unificato.r";

			// String serviceToForward = "/restful-services/1.0/execute/" + fileR + "?search_id=" + searchId + "&libraries=" + libs;
			String serviceToForward = "/restful-services/1.0/execute/" + fileR;

			// logger.debug("ExternalResourceServlet: Forward to service: " + serviceToForward);

			// response.sendRedirect(serviceToForward);

			RequestDispatcher requetsDispatcherObj = request.getRequestDispatcher(serviceToForward);
			requetsDispatcherObj.forward(request, response);

		} catch (Throwable t) {
			logger.error(t);
		}

	}
}
