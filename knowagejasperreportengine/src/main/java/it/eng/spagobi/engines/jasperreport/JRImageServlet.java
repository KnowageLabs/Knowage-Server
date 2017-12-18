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
package it.eng.spagobi.engines.jasperreport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * 
 * @deprecated use JRImageServlet instead (for any questions contact andrea gioia)
 */
public class JRImageServlet extends HttpServlet {

	private static transient Logger logger = Logger.getLogger(JRImageServlet.class);

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.debug("IN");
		HttpSession session = request.getSession(true);

		java.text.SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");	    
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 10);	//Adding 10 minute to current date time
		Date date=cal.getTime(); 
		String dateString=dateFormat.format( date )+" GMT";
		logger.debug(dateString);	
		response.setDateHeader("Expires", date.getTime());
		//response.setHeader("Expires", "Sat, 6 May 2010 12:00:00 GMT");	
		response.setHeader("Cache-Control: max-age", "600");
		
		response.setContentType("image/png");
		response.setHeader("Content-Type", "image/png");

		String mapName = request.getParameter("mapname");
		Map imagesMap = (Map)session.getAttribute(mapName);
		if(imagesMap != null){
			String imageName = request.getParameter("image");
			if (imageName != null) {
				byte[] imageData = (byte[])imagesMap.get(imageName);
				imagesMap.remove(imageName);
				if(imagesMap.isEmpty()){
					session.removeAttribute(mapName);
				}
				response.setContentLength(imageData.length);
				ServletOutputStream ouputStream = response.getOutputStream();
				ouputStream.write(imageData, 0, imageData.length);
				ouputStream.flush();
				ouputStream.close();
			}
		}
		logger.debug("OUT");
	}

}
