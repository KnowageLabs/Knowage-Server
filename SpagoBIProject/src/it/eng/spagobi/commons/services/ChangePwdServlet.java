/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import javax.servlet.http.HttpSession;

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
    private static final String URL = "start_url";
    private static final String OLD_PWD = "oldPassword";
    private static final String NEW_PWD = "NewPassword";
    private static final String NEW_PWD2= "NewPassword2";
    private static final String MESSAGE = "MESSAGE";
    private static final String targetJsp = "/WEB-INF/jsp/wapp/changePwd.jsp";
    
    private static final String PROP_NODE = "changepwdmodule.";
    public static final int PWD_OK = 0;
    public static final int PWD_WRONG = 1;
    
    SbiUser tmpUser = null;
    private String userId = null;
    private String url = null;
    private String oldPwd = null;
    private String newPwd = null;
    private String newPwd2 = null;
    
	
   

    /**
     * Initialize the engine.
     * 
     * @param config the config
     * 
     * @throws ServletException the servlet exception
     */
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("Initializing SpagoBI ChangePwd servlet...");
    }

    /**
     * process jasper report execution requests.
     * 
     * @param request the request
     * @param response the response
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	logger.debug("IN");
	
    	EMFErrorHandler errorHandler = new EMFErrorHandler();
    	
    	//getting values from request:
    	String message = (String) request.getParameter(MESSAGE);
    	logger.debug( "Message: " + message);
    	
    	userId = (String) request.getParameter(USER_ID);
    	if (userId == null || userId.equals("")) 
    		userId = (String) request.getParameter(USERNAME);
    	logger.debug("Check syntax pwd for the user: " + userId);
    	
    	url = (String) request.getParameter(URL);
    	logger.debug("Start url for final redirect: " + url);
    	
    	oldPwd = (String) request.getParameter(OLD_PWD);
    	newPwd = (String) request.getParameter(NEW_PWD);
    	newPwd2 = (String) request.getParameter(NEW_PWD2);
    	
    	try {
    		request.setAttribute(USER_ID, userId);
    		request.setAttribute(URL, url);
			if (message == null) {
				getServletContext().getRequestDispatcher(targetJsp).forward(request, response);
				return;
			}

			//gets the user bo from db
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			tmpUser = userDao.loadSbiUserByUserId(userId);
			
			if (message.trim().equalsIgnoreCase("CHANGE_PWD")){	
				if (PWD_OK == CheckPwd(tmpUser)){					
					//getting days number for calculate new expiration date
					IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
					List lstConfigChecks = configDao.loadConfigParametersByProperties("changepwd.expired_time");
					Date beginDate = new Date();
					if (lstConfigChecks.size() > 0){
						Config check = (Config)lstConfigChecks.get(0);						
						if (check.isActive()){
							//define the new expired date							
							Date endDate = null;
							SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
							Calendar cal = Calendar.getInstance();
							cal.set(beginDate.getYear()+1900, beginDate.getMonth(), beginDate.getDate());
							//adds n days (getted from db)
							cal.add(Calendar.DATE, Integer.parseInt(check.getValueCheck()));
							try{
								endDate = StringUtilities.stringToDate(sdf.format(cal.getTime()), DATE_FORMAT);
								logger.debug ("End Date for expiration calculeted: " + endDate);
								tmpUser.setDtPwdBegin(beginDate);
								tmpUser.setDtPwdEnd(endDate);
							}catch(Exception e){
								logger.error("The control pwd goes on error: "+e);
								throw new EMFUserError(EMFErrorSeverity.ERROR, 14008, new Vector(), new HashMap());
							}
						}						
					}
					tmpUser.setDtLastAccess(beginDate); //reset last access date
					tmpUser.setPassword(Password.encriptPassword(newPwd));//SHA encrypt
					tmpUser.setFlgPwdBlocked(false); //reset blocking flag
					userDao.updateSbiUser(tmpUser, tmpUser.getId());
					logger.debug("Updated properties for user with id " + tmpUser.getId() + " - DtLastAccess: " + tmpUser.getDtLastAccess().toString());
					//if it's all ok, redirect on login page 
					response.sendRedirect(url);
				}										
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			request.setAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE, eex.getDescription());
			getServletContext().getRequestDispatcher(targetJsp).forward(request, response);
			return; 
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			request.setAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE, ex.getMessage());
			getServletContext().getRequestDispatcher(targetJsp).forward(request, response);
			return;
		}
    	
		logger.debug("OUT");
    }

   
    /**
     * This method checks the syntax of new pwd.
     * 
     * @return true if it's correct, else otherwise
     */
    private int CheckPwd (SbiUser tmpUser) throws EMFUserError, Exception {
    	//gets the active controls to apply:
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List lstConfigChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
		logger.debug("checks found on db: " + lstConfigChecks.size());
		
		/* initial checks: - all values must be valorized, 
		 * 				   - oldPwd must be correct, 
		 *                 - newPwd and newPwd2 must be the same */
		 
		if (("").equals(oldPwd) || ("").equals(newPwd) ||  ("").equals(newPwd2)) {
			logger.debug("Some fields are empty." );	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14011);  
		}	
		//Controllo la pass vecchia, sia clear che criptata
		if (!Password.encriptPassword(oldPwd).equals(tmpUser.getPassword())){
			logger.debug("The old pwd is uncorrect." );	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14010);  
		}
		if (!newPwd.equals(newPwd2)){
			logger.debug("The two passwords are not the same." );	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14000);  
		}
		
		//getting and applying all check configureted for this module:
		for (int i=0; i < lstConfigChecks.size(); i++ ){
			Config check = (Config)lstConfigChecks.get(i);
			//checks that the configuration is correct (when getValueTypeId is valorized valueCheck must be valorized too)
			if (check.getValueTypeId() != null && check.getValueCheck() == null){
				logger.debug("The value configuration on db isn't valorized." );								
				Vector v = new Vector();
				v.add(check.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, 14009,v,  new HashMap());
			}
			
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_LEN_MIN)){
				//checks the minmum length of password
				int pwdLen = newPwd.length();
				if (pwdLen < Integer.parseInt(check.getValueCheck())){
					logger.debug("The password's length isn't correct." );									
					Vector v = new Vector();
					v.add(check.getValueCheck());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14001,v,  new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_ALPHA)){
				//checks that the new pwd contains alphabetical chars
				char PwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), PwdChars, 0); 
				boolean containsChar = false;
				for (int j = 0; j < PwdChars.length; j++) { 
					if (check.getValueCheck().contains(String.valueOf(PwdChars[j]))){
						containsChar = true;
						break;
					}
				}
				if (!containsChar){
					logger.debug("The password's doesn't contain alphabetical char." );					
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14002,new Vector(),  new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_LOWER_CHAR)){
				//checks that the new pwd contains LOWER chars
				char PwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), PwdChars, 0); 
				boolean containsChar = false;
				for (int j = 0; j < PwdChars.length; j++) { 
					if (check.getValueCheck().contains(String.valueOf(PwdChars[j]))){
						containsChar = true;
						break;
					}
				}
				if (!containsChar){
					logger.debug("The password's doesn't contain lower char." );					
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14004,new Vector(),  new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_UPPER_CHAR)){
				//checks that the new pwd contains UPPER chars
				char PwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), PwdChars, 0); 
				boolean containsChar = false;
				for (int j = 0; j < PwdChars.length; j++) { 
					if (check.getValueCheck().contains(String.valueOf(PwdChars[j]))){
						containsChar = true;
						break;
					}
				}
				if (!containsChar){
					logger.debug("The password's doesn't contain upper char." );					
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14005,new Vector(),  new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_SPECIAL_CHAR)){
				//checks that the new pwd contains special chars
				char PwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), PwdChars, 0); 
				boolean containsChar = false;
				for (int j = 0; j < PwdChars.length; j++) { 
					if (check.getValueCheck().contains(String.valueOf(PwdChars[j]))){
						containsChar = true;
						break;
					}
				}
				if (!containsChar){
					logger.debug("The password's doesn't contain special char." );									
					Vector v = new Vector();
					v.add(check.getValueCheck());
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14003,v, new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_NUMBER)){
				//checks that the new pwd contains numeric chars
				char PwdChars[] = new char[newPwd.length()];
				newPwd.getChars(0, newPwd.length(), PwdChars, 0); 
				boolean containsChar = false;
				for (int j = 0; j < PwdChars.length; j++) { 
					if (check.getValueCheck().contains(String.valueOf(PwdChars[j]))){
						containsChar = true;
						break;
					}
				}
				if (!containsChar){
					logger.debug("The password's doesn't contain numeric char." );					
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14006,new Vector(),  new HashMap());
				}
			}
			if (check.getLabel().equals(SpagoBIConstants.CHANGEPWDMOD_CHANGE)){
				//the password must be different from the lastest
				if (oldPwd.equalsIgnoreCase(newPwd)){
					logger.debug("The password's doesn't be equal the lastest." );									
					throw new EMFUserError(EMFErrorSeverity.ERROR, 14007,new Vector(),  new HashMap());
				}
			}
		}

		return PWD_OK;
	}

}
