/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
  
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package it.eng.spagobi.authentication.flow;

  
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.authentication.utility.AuthenticationUtility;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Class that fetches an account status from a database.
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class ExtendedAccountStatusGetter extends AbstractAccountStatusGetter {
	protected static Logger logger = Logger.getLogger(ExtendedAccountStatusGetter.class);
	/**  The format date to manage the data validation. */
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	/** the properties to load configuration*/
	private static final String PROP_NODE = "changepwd.";

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 10;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The role for the administrator */
    private static final String ADMIN_ROLE = "ADMIN";
    
	public int getStatus(String userID) {
        logger.debug("lookup for userID='" + userID + "'");

        boolean flgPwdBlocked = false;
        Date dtBegin = null;
        Date dtEnd = null;
        Date dtLastAccess = null;
        
        int status = STATUS_ACTIVE;
        List lstInfoPwd = getPwdInfo(userID);
        
        //if user is administrator the time  validation controls aren't apply!
        if (isAdminUser(userID)) return status;
        
        //gets the pwd presents in db 
		Iterator iter_sb_pwd = lstInfoPwd.iterator();
		String strDate = null;
		while(iter_sb_pwd.hasNext()) {
			SourceBeanAttribute tmp_attributeSB = (SourceBeanAttribute)iter_sb_pwd.next();
			SourceBean attributeSB = (SourceBean)tmp_attributeSB.getValue();
			
			strDate = (attributeSB.getAttribute("DT_PWD_BEGIN").toString().equals("")) ? null : attributeSB.getAttribute("DT_PWD_BEGIN").toString();
			dtBegin = (strDate == null) ? null : new Date(((Timestamp)attributeSB.getAttribute("DT_PWD_BEGIN")).getTime());
			logger.debug("DT_PWD_BEGIN : " + dtBegin);
			
			strDate = (attributeSB.getAttribute("DT_PWD_END").toString().equals("")) ? null : attributeSB.getAttribute("DT_PWD_END").toString();
			dtEnd = (strDate == null) ? null: new Date(((Timestamp)attributeSB.getAttribute("DT_PWD_END")).getTime());
			logger.debug("DT_PWD_END : " + dtEnd);
			
			strDate = (attributeSB.getAttribute("DT_LAST_ACCESS").toString().equals("")) ? null : attributeSB.getAttribute("DT_LAST_ACCESS").toString();
			dtLastAccess = (strDate == null)? null :  new Date(((Timestamp)attributeSB.getAttribute("DT_LAST_ACCESS")).getTime());
			logger.debug("DT_LAST_ACCESS : " + dtLastAccess);
			
			if ("true".equalsIgnoreCase(attributeSB.getAttribute("FLG_PWD_BLOCKED").toString()))
				flgPwdBlocked = true;
			logger.debug("FLG_PWD_BLOCKED : " + flgPwdBlocked);	
		}
		
		Date currentDate = new Date();
		logger.debug ("currentDate: " + currentDate);
		List lstChecks = getChecksInfo();
		
		//filter with the 'prop' parameter
		Iterator it = lstChecks.iterator();			    		
		while (it.hasNext()) {	
			SourceBeanAttribute tmp_attributeSB = (SourceBeanAttribute)it.next();
			SourceBean attributeSB = (SourceBean)tmp_attributeSB.getValue();
			
			String strLabel = (attributeSB.getAttribute("LABEL").toString().equals("")) ? "" : attributeSB.getAttribute("LABEL").toString();
			logger.debug("LABEL : " + strLabel);
					
			String strValueCheck = (attributeSB.getAttribute("VALUE_CHECK").toString().equals("")) ? null : attributeSB.getAttribute("VALUE_CHECK").toString();
			logger.debug("VALUE_CHECK : " + strValueCheck);
			
			boolean isActive = false;
			if ("true".equalsIgnoreCase(attributeSB.getAttribute("IS_ACTIVE").toString()))
				isActive = true;
			logger.debug("IS_ACTIVE : " + isActive);
			
			if (isActive && strLabel.startsWith(PROP_NODE)){
				if ((SpagoBIConstants.CHANGEPWD_CHANGE_FIRST).equals(strLabel) && dtLastAccess == null){
					//if dtLastAccess isn't enhanced it represents the first login, so is necessary change the pwd
					logger.info("The pwd needs to activate!");
					return STATUS_ACTIVATE;
				}
				
				
				if ((SpagoBIConstants.CHANGEPWD_EXPIRED_TIME).equals(strLabel) &&
						dtEnd != null && currentDate.compareTo(dtEnd) >= 0){
					//check if the pwd is expiring, in this case it's locked.
					logger.info("The pwd is expiring... it should be changed");
					return STATUS_CHANGEPWD;
				}
				if ((SpagoBIConstants.CHANGEPWD_DISACTIVE_TIME).equals(strLabel)){
					//defines the end date for uselessness
					Date tmpEndForUnused = null;
					if (dtLastAccess != null){
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
						Calendar cal = Calendar.getInstance();
						cal.set(dtLastAccess.getYear()+1900, dtLastAccess.getMonth(), dtLastAccess.getDate());
						cal.add(Calendar.MONTH, 6);
						try{
							tmpEndForUnused = StringUtilities.stringToDate(sdf.format(cal.getTime()), DATE_FORMAT);
							logger.debug ("End Date For Unused: " + tmpEndForUnused);
						}catch(Exception e){
							logger.error("The control pwd goes on error: "+e);
							return STATUS_ERROR;
						}	
					}
					if (tmpEndForUnused != null && currentDate.compareTo(tmpEndForUnused) >= 0){
						//check if the pwd is unused by 6 months, in this case it's locked.
						logger.info("The pwd is unused more than 6 months! It's locked!!");
						return STATUS_LOCKED;
					}
				}
				
			}
			
		}//while on props
		
		
		//general controls: check if the account is already blocked, otherwise update dtLastAccess field
		if (flgPwdBlocked){
			//if flgPwdBlocked is true the user cannot goes on
			logger.info("The pwd needs to activate!");
			return STATUS_ERROR; //STATUS_LOCKED;
		}
		else{
			logger.info("The pwd is active!");
			//update lastAccessDate on db with current date
			try{
				AuthenticationUtility utility = new AuthenticationUtility();
				List pars = new LinkedList();
				Date tmp = new Date();
				pars.add(new Date());
				// CASE INSENSITVE SEARCH ON USER ID
	        	pars.add(userID.toUpperCase());
	        	int res = utility.executeUpdate("UPDATE SBI_USER SET DT_LAST_ACCESS = ? WHERE UPPER(USER_ID) = ?", pars);
			}catch(Exception e){
	        	logger.error("Error while check pwd: " + e);
	        	e.printStackTrace();
	        }
			return STATUS_ACTIVE;
		}
        
    }
	
	public void afterPropertiesSet() throws Exception {
		
	}

    private List getPwdInfo(String userID) {
    	logger.debug("IN");
    	
    	List lstResult = null;
        //define query to get pwd info from database
        try{
        	AuthenticationUtility utility = new AuthenticationUtility();
        	List pars = new LinkedList();
        	// CASE INSENSITVE SEARCH ON USER ID
        	pars.add(userID.toUpperCase());
        	lstResult = utility.executeQuery("SELECT DT_PWD_BEGIN, DT_PWD_END, FLG_PWD_BLOCKED, DT_LAST_ACCESS FROM SBI_USER WHERE UPPER(USER_ID) = ?", pars);
        }catch(Exception e){
        	logger.error("Error while check pwd: " + e);
        	e.printStackTrace();
        }

    	logger.debug("OUT");
    	return lstResult;
    }
    
    private List getChecksInfo() {
    	logger.debug("IN");
    	
    	List lstResult = null;
        //define query to get roles to check pwd from database
        try{
        	AuthenticationUtility utility = new AuthenticationUtility();
        	lstResult = utility.executeQuery("SELECT LABEL, VALUE_CHECK, VALUE_TYPE_ID, IS_ACTIVE FROM SBI_CONFIG ", null);
        }catch(Exception e){
        	logger.error("Error while getting configuration check pwd: " + e);
        	e.printStackTrace();
        }

    	logger.debug("OUT");
    	return lstResult;
    }
   
    private boolean isAdminUser(String userID) {
    	logger.debug("IN");
    	
    	 boolean toReturn = true;	      
    
        //define query to get roles to check pwd from database
        try{
        	AuthenticationUtility utility = new AuthenticationUtility();
        	// CASE INSENSITVE SEARCH ON USER ID
        	String query = "SELECT U.ID, NAME FROM  SBI_EXT_USER_ROLES UR, SBI_EXT_ROLES R, SBI_USER U " +
        					"WHERE UPPER(U.USER_ID) = ? AND UR.ID = U.ID  AND R.EXT_ROLE_ID = UR.EXT_ROLE_ID AND R.ROLE_TYPE_CD =? ";
        	List pars = new LinkedList();
        	pars.add(userID.toUpperCase());
        	pars.add(ADMIN_ROLE);
        	
        	List lstResult = utility.executeQuery(query, pars);
        	if (lstResult.size() == 0){
        		toReturn = false;
        		logger.debug("User isn't administrator. Checks on the password must be apply !");
        	}
        }catch(Exception e){
        	logger.error("Error while getting configuration check pwd: " + e);
        	e.printStackTrace();
        }

    	logger.debug("OUT");
    	return toReturn;
    }
}
