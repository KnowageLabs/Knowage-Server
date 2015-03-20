/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.services.common.SsoServiceInterface;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class AbstractDriver {

	private static final String DESCRIPTION_SUFFIX ="_description";
	
    static Logger logger = Logger.getLogger(AbstractDriver.class);
    /**
     * 
     */
    public AbstractDriver() {
	super();
    }

    /**
     * Applys changes for security reason if necessary
     * 
     * @param pars
     *                The map of parameters
     * @return The map of parameters to send to the engine
     */
    protected Map applySecurity(Map pars, IEngUserProfile profile) {
        logger.debug("IN");
        String active =SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.ACTIVE");
		String userId=(String)profile.getUserUniqueIdentifier();
		if (active != null && active.equalsIgnoreCase("true") && !((UserProfile)profile).isSchedulerUser(userId)){
		    logger.debug("I don't put the UserId information in the URL");
		}else {
		    if (((UserProfile) profile).getUserUniqueIdentifier() != null) {
			pars.put(SsoServiceInterface.USER_ID, ((UserProfile) profile).getUserUniqueIdentifier()); 
		    }
		}
	    
        
        logger.debug("Add parameter: "+SsoServiceInterface.USER_ID+" / " + ((UserProfile) profile).getUserUniqueIdentifier());
        logger.debug("OUT");
        return pars;
    }
    
    /**
     * get the description of the parameter and create a new biparameter to
     * pass at the engine with url parameter_name+DESCRIPTION_SUFFIX
     * @param biobj
     * @param pars
     * @return
     */
    protected Map addBIParameterDescriptions(BIObject biobj, Map pars) {
    	logger.debug("IN");
    	if (biobj == null) {
    	    logger.warn("BIObject parameter null");
    	    logger.debug("OUT");
    	    return pars;
    	}

    	ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
    	if (biobj.getBiObjectParameters() != null) {
    	    BIObjectParameter biobjPar = null;
    	    String description = null;
    	    for (Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();) {
    		try {
    		    biobjPar = (BIObjectParameter) it.next();
    		    logger.debug("Manage parameter: "+biobjPar.getParameterUrlName());
    		    /*
    		     * value = (String) biobjPar.getParameterValues().get(0);
    		     * pars.put(biobjPar.getParameterUrlName(), value);
    		     */    		    
    		    description = parValuesEncoder.encodeDescription(biobjPar);
    		    if (description != null){
	    		    pars.put(biobjPar.getParameterUrlName()+DESCRIPTION_SUFFIX, description);
	    		    logger.debug("Add description:"+biobjPar.getParameterUrlName()+DESCRIPTION_SUFFIX+"/"+description);
    		    }
    		} catch (Exception e) {
    		    logger.debug("OUT");
    		    logger.warn("Error while processing the BIParameter " +biobjPar.getParameterUrlName()+ ".. getting the description", e);
    		}
    	    }
    	}
    	logger.debug("OUT");
    	return pars;
        }
    
    /**
     * Returns the template elaborated.
     * 
     * @param byte[] the template
     * @param profile the profile
     * 
     * @return the byte[] with the modification of the document template
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public byte[] ElaborateTemplate(byte[] template) throws InvalidOperationRequest{
    	logger.warn("Default call. No elaborations are applied to the template.");
    	return template;
    }
    
    public void applyLocale(Locale locale){
    	logger.warn("Method not implemented.");
    }


}