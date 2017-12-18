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
