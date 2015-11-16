/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.servlet.session;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This servlet just extends the current session, if any
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class ExtendSessionServlet extends HttpServlet {
	
    public void service(HttpServletRequest request, HttpServletResponse response)
    	throws IOException, ServletException {
    	request.getSession(false);
    	response.setContentType("text/plain");
		ServletOutputStream ouputStream = response.getOutputStream();
		ouputStream.write("".getBytes());
		ouputStream.flush();
		ouputStream.close();
    }

}
