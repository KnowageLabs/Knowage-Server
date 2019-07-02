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
package it.eng.spagobi.commons.services;


import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 * Process jasper report execution requests and returns bytes of the filled
 * reports
 */
public class ChangePwdServlet extends HttpServlet {


	/**
	 * Logger component
	 */
	private static transient Logger logger = Logger.getLogger(ChangePwdServlet.class);
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String USER_ID = "user_id";
	private static final String USERNAME = "username";
	private static final String OLD_PWD = "oldPassword";
	private static final String NEW_PWD = "NewPassword";
	private static final String NEW_PWD2 = "NewPassword2";
	private static final String MESSAGE = "MESSAGE";
	private static final String TARGET_JSP = "/WEB-INF/jsp/wapp/changePwd.jsp";

	/**
	 * Initialize the engine.
	 *
	 * @param config the config
	 * @throws ServletException the servlet exception
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("Initializing SpagoBI ChangePwd servlet...");
	}

	/**
	 * process jasper report execution requests.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.debug("IN");

		EMFErrorHandler errorHandler = new EMFErrorHandler();

		//getting values from request:
		String message = request.getParameter(MESSAGE);
		logger.debug("Message: " + message);

		String userId = request.getParameter(USER_ID);
		if (userId == null || userId.equals(""))
			userId = request.getParameter(USERNAME);
		logger.debug("Check syntax pwd for the user: " + userId);

		String oldPwd = request.getParameter(OLD_PWD);
		String newPwd = request.getParameter(NEW_PWD);
		String newPwd2 = request.getParameter(NEW_PWD2);

		try {
			request.setAttribute(USER_ID, userId);
			if (message == null) {
				getServletContext().getRequestDispatcher(TARGET_JSP).forward(request, response);
				return;
			}

			//gets the user bo from db
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser tmpUser = userDao.loadSbiUserByUserId(userId);
			
			if (message.trim().equalsIgnoreCase("CHANGE_PWD")) {
				if (PasswordChecker.getInstance()
						.isValid(tmpUser, oldPwd, newPwd, newPwd2)) {
					//getting days number for calculate new expiration date
					IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
					List lstConfigChecks = configDao.loadConfigParametersByProperties(SpagoBIConstants.CHANGEPWD_EXPIRED_TIME);
					Date beginDate = new Date();
					if (lstConfigChecks.size() > 0) {
						Config check = (Config) lstConfigChecks.get(0);
						if (check.isActive()) {
							//define the new expired date							
							SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
							Calendar cal = Calendar.getInstance();
							cal.set(beginDate.getYear() + 1900, beginDate.getMonth(), beginDate.getDate());
							//adds n days (getted from db)
							cal.add(Calendar.DATE, Integer.parseInt(check.getValueCheck()));
							try {
								Date endDate = StringUtilities.stringToDate(sdf.format(cal.getTime()), DATE_FORMAT);
								logger.debug("End Date for expiration calculeted: " + endDate);
								tmpUser.setDtPwdBegin(beginDate);
								tmpUser.setDtPwdEnd(endDate);
							} catch (Exception e) {
								logger.error("The control pwd goes on error: " + e);
								throw new EMFUserError(EMFErrorSeverity.ERROR, 14008, new Vector(), new HashMap());
							}
						}
					}
					tmpUser.setDtLastAccess(beginDate); //reset last access date
					tmpUser.setPassword(Password.encriptPassword(newPwd));//SHA encrypt
					tmpUser.setFlgPwdBlocked(false); //reset blocking flag
					userDao.updateSbiUser(tmpUser, tmpUser.getId());
					logger.debug("Updated properties for user with id " + tmpUser.getId() + " - DtLastAccess: " + tmpUser.getDtLastAccess().toString());
				}
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			request.setAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE, eex.getDescription());
			getServletContext().getRequestDispatcher(TARGET_JSP).forward(request, response);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			request.setAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE, ex.getMessage());
			getServletContext().getRequestDispatcher(TARGET_JSP).forward(request, response);
			return;
		}

		logger.debug("OUT");
	}
}
