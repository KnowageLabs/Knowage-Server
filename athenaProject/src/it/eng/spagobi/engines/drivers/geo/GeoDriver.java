/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.geo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Driver Implementation (IEngineDriver Interface) for Geo External Engine.
 */
public class GeoDriver extends AbstractDriver implements IEngineDriver {

    static private Logger logger = Logger.getLogger(GeoDriver.class);

    public static final String DOCUMENT_ID = "document";
    public static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";

    public static final String COUNTRY = "country";
    public static final String LANGUAGE = "language";

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

	Map map;
	BIObject biobj;

	map = new Hashtable();
	try {
	    biobj = (BIObject) biobject;
	    map = getMap(biobj);
	} catch (ClassCastException cce) {
	    logger.error("The parameter is not a BIObject type", cce);
	}

	map = applySecurity(map, profile);
	map = applyLocale(map);
	map = applyService(map);

	logger.debug("OUT");

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
	logger.debug("IN");

	if (subObject == null) {
	    return getParameterMap(object, profile, roleName);
	}

	Map map = new Hashtable();
	try {
	    BIObject biobj = (BIObject) object;
	    map = getMap(biobj);
	    SubObject subObjectDetail = (SubObject) subObject;

	    Integer id = subObjectDetail.getId();

	    map.put("nameSubObject", subObjectDetail.getName());
	    map.put("descriptionSubObject", subObjectDetail.getDescription());
	    map.put("visibilitySubObject", subObjectDetail.getIsPublic().booleanValue() ? "Public" : "Private");
	    map.put("subobjectId", subObjectDetail.getId());

	} catch (ClassCastException cce) {
	    logger.error("The second parameter is not a SubObjectDetail type", cce);
	}

	map = applySecurity(map, profile);
	map = applyLocale(map);
	map = applyService(map);

	logger.debug("OUT");

	return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.spagobi.engines.drivers.IEngineDriver#getEditDocumentTemplateBuildUrl(java.lang.Object,
     *      it.eng.spago.security.IEngUserProfile)
     */
    public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	    throws InvalidOperationRequest {
	logger.warn("Function not implemented");
	throw new InvalidOperationRequest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.spagobi.engines.drivers.IEngineDriver#getNewDocumentTemplateBuildUrl(java.lang.Object,
     *      it.eng.spago.security.IEngUserProfile)
     */
    public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	    throws InvalidOperationRequest {
	logger.warn("Function not implemented");
	throw new InvalidOperationRequest();
    }

    /**
     * Starting from a BIObject extracts from it the map of the paramaeters for
     * the execution call
     * 
     * @param biobj
     *                BIObject to execute
     * @return Map The map of the execution call parameters
     */
    private Map getMap(BIObject biobj) {
	logger.debug("IN");

	Map pars;
	ObjTemplate objtemplate;
	byte[] template;
	String documentId;
	String documentlabel;

	pars = new Hashtable();
	try {
	    objtemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobj.getId());
	    if (objtemplate == null) {
		throw new Exception("Active Template null");
	    }

	    template = DAOFactory.getBinContentDAO().getBinContent(objtemplate.getBinId());
	    if (template == null) {
		throw new Exception("Content of the Active template null");
	    }

	    documentId = biobj.getId().toString();
	    pars.put(DOCUMENT_ID, documentId);
	    logger.debug("Add " + DOCUMENT_ID + " parameter: " + documentId);

	    documentlabel = biobj.getLabel().toString();
	    pars.put(DOCUMENT_LABEL, documentlabel);
	    logger.debug("Add " + DOCUMENT_LABEL + " parameter: " + documentlabel);

	    pars = addBIParameters(biobj, pars);
	    pars = addBIParameterDescriptions(biobj, pars);
	} catch (Exception e) {
	    logger.error("Error while recovering execution parameter map: \n" + e);
	}

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
	    return pars;
	}

	ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
	if (biobj.getBiObjectParameters() != null) {
	    BIObjectParameter biobjPar = null;
	    for (Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();) {
		try {
		    biobjPar = (BIObjectParameter) it.next();
		    String value = parValuesEncoder.encode(biobjPar);
		    pars.put(biobjPar.getParameterUrlName(), value);
		    logger.debug("Add parameter:" + biobjPar.getParameterUrlName() + "/" + value);
		} catch (Exception e) {
		    logger.error("Error while processing a BIParameter", e);
		}
	    }
	}

	logger.debug("OUT");

	return pars;
    }

    protected Map applyService(Map pars) {
	logger.debug("IN");
	pars.put("ACTION_NAME", "GEO_ACTION");
	pars.put("NEW_SESSION", "TRUE");
	logger.debug("OUT");
	return pars;
    }

    private Map applyLocale(Map map) {
	logger.debug("IN");

	ConfigSingleton config = ConfigSingleton.getInstance();
	Locale portalLocale = null;
	try {
	    portalLocale = PortletUtilities.getPortalLocale();
	    logger.debug("Portal locale: " + portalLocale);
	} catch (Exception e) {
	    logger.error("Error while getting portal locale.");
	    portalLocale = MessageBuilder.getBrowserLocaleFromSpago();
	    logger.debug("Spago locale: " + portalLocale);
	}

	SourceBean languageSB = null;
	if (portalLocale != null && portalLocale.getLanguage() != null) {
	    languageSB = (SourceBean) config.getFilteredSourceBeanAttribute("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE",
		    "language", portalLocale.getLanguage());
	}

	if (languageSB != null) {
	    map.put(COUNTRY, (String) languageSB.getAttribute("country"));
	    map.put(LANGUAGE, (String) languageSB.getAttribute("language"));
	    logger.debug("Added parameter: country/" + (String) languageSB.getAttribute("country"));
	    logger.debug("Added parameter: language/" + (String) languageSB.getAttribute("language"));
	} else {
	    logger.warn("Language " + portalLocale.getLanguage() + " is not supported by SpagoBI");
	    logger.warn("Portal locale will be replaced with the default lacale (country: US; language: en).");
	    map.put(COUNTRY, "US");
	    map.put(LANGUAGE, "en");
	    logger.debug("Added parameter: country/US");
	    logger.debug("Added parameter: language/en");
	}

	logger.debug("OUT");
	return map;
    }
}
