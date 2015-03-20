/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.drivers.birt;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Driver Implementation (IEngineDriver Interface) for Birt Report Engine.
 */
public class BirtReportDriver extends AbstractDriver implements IEngineDriver {
    static private Logger logger = Logger.getLogger(BirtReportDriver.class);

    /**
     * Returns a map of parameters which will be send in the request to the
     * engine application.
     * 
     * @param profile
     *                Profile of the user
     * @param roleName
     *                the name of the execution role
     * @param biobject
     *                the biobject
     * 
     * @return Map The map of the execution call parameters
     */
    public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
	logger.debug("IN");
	Map map = new Hashtable();
	try {
	    BIObject biobj = (BIObject) biobject;
	    map = getMap(biobj);
	} catch (ClassCastException cce) {
	    logger.error("The parameter is not a BIObject type", cce);
	}
	map = applySecurity(map, profile);
	return map;
    }

    /**
     * Returns a map of parameters which will be send in the request to the
     * engine application.
     * 
     * @param subObject
     *                SubObject to execute
     * @param profile
     *                Profile of the user
     * @param roleName
     *                the name of the execution role
     * @param object
     *                the object
     * 
     * @return Map The map of the execution call parameters
     */
    public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName) {
	return getParameterMap(object, profile, roleName);
    }

    private Map getMap(BIObject biobj) {
	logger.debug("IN");
	Map pars = new Hashtable();

	String documentId = biobj.getId().toString();
	pars.put("document", documentId);
	logger.debug("Add document parameter:" + documentId);

	String documentName = biobj.getName();
	pars.put("documentName", documentName);
	
	// retrieving the date format
	ConfigSingleton config = ConfigSingleton.getInstance();
	SourceBean formatSB = (SourceBean) config.getAttribute("DATA-ACCESS.DATE-FORMAT");
	String format = (formatSB == null) ? "DD-MM-YYYY" : (String) formatSB.getAttribute("format");
	pars.put("dateformat", format);
	pars = addBIParameters(biobj, pars);
	pars = addBIParameterDescriptions(biobj, pars);
	logger.debug("OUT");
	return pars;
    }

    /**
     * Add into the parameters map the BIObject's BIParameter names and values
     * 
     * @param biobj
     *                BIOBject to execute
     * @param pars
     *                Map of the parameters for the execution call
     * @return Map The map of the execution call parameters
     */
    private Map addBIParameters(BIObject biobj, Map pars) {
	logger.debug("IN");
	if (biobj == null) {
	    logger.warn("BIObject parameter null");
	    logger.debug("OUT");
	    return pars;
	}

	ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
	if (biobj.getBiObjectParameters() != null) {
	    BIObjectParameter biobjPar = null;
	    String value = null;
	    for (Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();) {
		try {
		    biobjPar = (BIObjectParameter) it.next();
		    /*
		     * value = (String) biobjPar.getParameterValues().get(0);
		     * pars.put(biobjPar.getParameterUrlName(), value);
		     */
		    value = parValuesEncoder.encode(biobjPar);
			logger.debug("Parameter [" + biobjPar.getParameterUrlName() + "value is equal to [" + value + "]");
		    if (biobjPar.getParameterUrlName()!=null && value!=null) {
			pars.put(biobjPar.getParameterUrlName(), value);
			}
			
		} catch (Exception e) {
		    logger.debug("OUT");
		    logger.warn("Error while processing a BIParameter", e);
		}
	    }
	}
	logger.debug("OUT");
	return pars;
    }

    /**
     * Function not implemented. Thid method should not be called
     * 
     * @param biobject
     *                The BIOBject to edit
     * @param profile
     *                the profile
     * 
     * @return the edits the document template build url
     * 
     * @throws InvalidOperationRequest
     *                 the invalid operation request
     */
    public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	    throws InvalidOperationRequest {
	logger.warn("Function not implemented");
	throw new InvalidOperationRequest();
    }

    /**
     * Function not implemented. Thid method should not be called
     * 
     * @param biobject
     *                The BIOBject to edit
     * @param profile
     *                the profile
     * 
     * @return the new document template build url
     * 
     * @throws InvalidOperationRequest
     *                 the invalid operation request
     */
    public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	    throws InvalidOperationRequest {
	logger.warn("Function not implemented");
	throw new InvalidOperationRequest();
    }

}
