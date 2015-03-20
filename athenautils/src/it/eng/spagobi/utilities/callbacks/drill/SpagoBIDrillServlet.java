/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.callbacks.drill;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class SpagoBIDrillServlet extends HttpServlet {

    private static transient Logger logger = Logger
	    .getLogger(SpagoBIDrillServlet.class);

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void service(HttpServletRequest request, HttpServletResponse response) {
	HttpSession session = request.getSession();

	IEngUserProfile profile = (IEngUserProfile) session
		.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	if (profile == null) {
	    logger.error("IEngUserProfile is not in session!!!");
	} else {
	  //  String username = (String) profile.getUserUniqueIdentifier();
	    String username = (String) ((UserProfile)profile).getUserId();

	    String spagobiContextUrl = (String) session
		    .getAttribute("spagobicontext");
	    String url = spagobiContextUrl + "/servlet/AdapterHTTP?";
	    url += "USERNAME=" + username;
	    url += "&NEW_SESSION=TRUE";
	    url += "&PAGE=DirectExecutionPage";
	    url += "&DOCUMENT_LABEL=" + request.getParameter("DOCUMENT_LABEL");
	    String documentParameters = "";
	    Enumeration parameterNames = request.getParameterNames();
	    while (parameterNames.hasMoreElements()) {
		String parurlname = (String) parameterNames.nextElement();
		if (parurlname.equalsIgnoreCase("DOCUMENT_LABEL"))
		    continue;
		String parvalue = request.getParameter(parurlname);
		documentParameters += "%26" + parurlname + "%3D" + parvalue;
	    }
	    url += "&DOCUMENT_PARAMETERS=" + documentParameters;

	    try {
		response.sendRedirect(url);
	    } catch (IOException e1) {
		logger.error("IOException during sendRedirect",e1);
	    }
	}

    }

}
