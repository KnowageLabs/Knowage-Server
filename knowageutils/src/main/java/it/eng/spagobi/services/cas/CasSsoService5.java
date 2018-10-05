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
package it.eng.spagobi.services.cas;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import it.eng.spagobi.services.common.JWTSsoService;

public class CasSsoService5 extends JWTSsoService {
	static private Logger logger = Logger.getLogger(CasSsoService5.class);

    public String readUserIdentifier(HttpServletRequest request){
	    HttpSession session=request.getSession();
	    Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
	    if (assertion == null) {
	    	return super.readUserIdentifier(request);
	    }
	    String userInSession=assertion.getPrincipal().getName();

		//String user=(String)request.getRemoteUser();
		//logger.debug("CAS user in HttpServletRequest:"+user);
		logger.debug("CAS user in HttpSession:"+userInSession);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 10);
		Date expiresAt = calendar.getTime();
	
		String jwtToken = JWTSsoService.userId2jwtToken(userInSession, expiresAt);
		logger.debug("JWT-TOKEN " + jwtToken);
	
		return jwtToken;
    }
    

}
