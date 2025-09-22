/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.engines.drivers.dashboard;

import java.util.*;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.drivers.cockpit.CockpitDriver;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.json.JSONException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

/**
 * @author albnale
 *
 *         The purpose of this class is to inherit the behavior of the cockpit UNTIL the transition from the cockpit to the dashboard is completed
 */
public class DashboardDriver extends CockpitDriver {

	private static Logger logger = Logger.getLogger(DashboardDriver.class);

    private static final String PARAM_NEW_SESSION = "NEW_SESSION";


	@Override
	public ArrayList<String> getFunctionsAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<String> functionUuids = new ArrayList<>();
		if (contentTemplate == null) {
			logger.error("Template content non returned. Impossible get associated functions. Check the template!");
			return functionUuids;
		}

		Configuration conf = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
		List<String> catalogFunction = JsonPath.using(conf).parse(new String(contentTemplate)).read("$.widgets[*].columns[*].catalogFunctionId");
		functionUuids.addAll(catalogFunction.stream().filter(Objects::nonNull).toList());

		logger.debug("OUT");
		return functionUuids;
	}

    /**
     * Returns a map of parameters which will be send in the request to the engine application.
     *
     * @param profile  Profile of the user
     * @param roleName the name of the execution role
     * @param biobject the biobject
     */
    @Override
    public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
        logger.debug("IN");
        Map map;
        try {
            BIObject biobj = (BIObject) biobject;
            map = getMap(biobj, profile);
            map.put(PARAM_NEW_SESSION, "TRUE");
            map = applySecurity(map, profile);
            map = applyLocale(map);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException(e);
        }

        logger.debug("OUT");
        return map;
    }



    /**
     * Starting from a BIObject extracts from it the map of the paramaeters for the execution call
     *
     * @param biobj BIObject to execute
     * @return Map The map of the execution call parameters
     */
    private Map getMap(BIObject biobj, IEngUserProfile profile) {
        logger.debug("IN");

        Map pars;
        ObjTemplate objtemplate;
        byte[] template;
        String documentId;
        String documentlabel;

        pars = new Hashtable();
        try {

            if (biobj.getDocVersion() != null) {
                objtemplate = DAOFactory.getObjTemplateDAO().loadBIObjectTemplate(biobj.getDocVersion()); // specific template version (not active version)
                logger.info("Used template version id " + biobj.getDocVersion());
            } else {
                objtemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobj.getId()); // default
                logger.info("Used active template (default) ");
            }

            if (objtemplate == null) {
                throw new SpagoBIRuntimeException("Template is [null] in document with id equals to [" + biobj.getId() + "]");
            }

            template = DAOFactory.getBinContentDAO().getBinContent(objtemplate.getBinId());
            if (template == null) {
                throw new SpagoBIRuntimeException("Content of the active template is [null] in document with id equals to [" + biobj.getId() + "]");
            }

            documentId = biobj.getId().toString();
            pars.put(DOCUMENT_ID, documentId);
            logger.debug("Add " + DOCUMENT_ID + " parameter:" + documentId);

            documentlabel = biobj.getLabel().toString();
            pars.put(DOCUMENT_LABEL, documentlabel);
            logger.debug("Add " + DOCUMENT_LABEL + " parameter: " + documentlabel);
            pars.put(DOCUMENT_VERSION, objtemplate.getId());
            logger.debug("Add " + DOCUMENT_VERSION + " parameter: " + objtemplate.getId());
            pars.put(DOCUMENT_AUTHOR, biobj.getCreationUser());
            logger.debug("Add " + DOCUMENT_AUTHOR + " parameter: " + biobj.getCreationUser());
            pars.put(DOCUMENT_NAME, biobj.getName());
            logger.debug("Add " + DOCUMENT_NAME + " parameter: " + biobj.getName());
            pars.put(DOCUMENT_DESCRIPTION, biobj.getDescription());
            logger.debug("Add " + DOCUMENT_DESCRIPTION + " parameter: " + biobj.getDescription());
            pars.put(DOCUMENT_IS_PUBLIC, biobj.isPublicDoc());
            logger.debug("Add " + DOCUMENT_IS_PUBLIC + " parameter: " + biobj.isPublicDoc());
            pars.put(DOCUMENT_IS_VISIBLE, biobj.isVisible());
            logger.debug("Add " + DOCUMENT_IS_VISIBLE + " parameter: " + biobj.isVisible());
            if (biobj.getPreviewFile() != null) {
                pars.put(DOCUMENT_PREVIEW_FILE, biobj.getPreviewFile());
            }
            logger.debug("Add " + DOCUMENT_PREVIEW_FILE + " parameter: " + biobj.getPreviewFile());
            List funcs = biobj.getFunctionalities();
            if (funcs != null) {
                pars.put(DOCUMENT_FUNCTIONALITIES, funcs);
                logger.debug("Add " + DOCUMENT_FUNCTIONALITIES + " parameter: " + funcs);
            }
            pars.put(IS_TECHNICAL_USER, UserUtilities.isTechnicalUser(profile));
            logger.debug("Add " + IS_TECHNICAL_USER + " parameter: " + UserUtilities.isTechnicalUser(profile));

            pars = addBIParameters(biobj, pars);
            pars = addBIParameterDescriptions(biobj, pars);
            pars = addOutputParameters(biobj, pars);

        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Error while recovering execution parameter map: \n" + e);
        }

        logger.debug("OUT");

        return pars;
    }


    /**
     * Add into the parameters map the BIObject's BIParameter names and values
     *
     * @param biobj BIOBject to execute
     * @param pars  Map of the parameters for the execution call
     * @return Map The map of the execution call parameters
     */
    private Map addBIParameters(BIObject biobj, Map pars) {
        logger.debug("IN");

        if (biobj == null) {
            logger.warn("BIObject parameter null");
            return pars;
        }

        if (biobj.getDrivers() != null) {
            BIObjectParameter biobjPar = null;
            for (Iterator<BIObjectParameter> it = biobj.getDrivers().iterator(); it.hasNext();) {
                try {
                    biobjPar = it.next();
                    String value = encode(biobjPar);
                    if (biobjPar.getParameterUrlName() != null && value != null) {
                        pars.put(biobjPar.getParameterUrlName(), value);
                        logger.debug("Add parameter:" + biobjPar.getParameterUrlName() + "/" + value);
                    } else {
                        logger.warn("NO parameter are added... something is null");
                    }
                } catch (Exception e) {
                    logger.error("Error while processing a BIParameter", e);
                }
            }
        }

        logger.debug("OUT");
        return pars;
    }

    public String encode(BIObjectParameter biobjPar) {
        logger.debug("IN");

        List<String> parameterValues = biobjPar.getParameterValues();
        if (parameterValues == null) {
            logger.debug("biobjPar.getParameterValues() == null");
            return null;
        }

        if (parameterValues.isEmpty()) {
            return "";
        }

        Parameter parameter = biobjPar.getParameter();
        if (parameter == null) {
            Integer parId = biobjPar.getParID();
            if (parId == null) {
                logger.warn(
                        "Parameter object nor parameter id are set into BIObjectParameter with label = " + biobjPar.getLabel() + " of document with id = " +
                        biobjPar.getBiObjectID());
            } else {
                try {
                    parameter = DAOFactory.getParameterDAO().loadForDetailByParameterID(parId);
                } catch (EMFUserError e) {
                    logger.warn("Error loading parameter with id = " + parId);
                }
            }
        }

        if (parameter == null) {
            logger.error("Unable to load parameter from BIObjectParameter with label = " + biobjPar.getLabel() + " of document with id = " + biobjPar.getBiObjectID());
            return null;
        }

        String type = parameter.getType();
        boolean multivalue = biobjPar.isMultivalue();

        ModalitiesValue modValue = parameter.getModalityValue();
        if (modValue != null) {
            String typeCode = modValue.getITypeCd();
            logger.debug("typeCode " + typeCode);

            if (SpagoBIConstants.INPUT_TYPE_MAN_IN_CODE.equalsIgnoreCase(typeCode)) {
                multivalue = false;
            }
        }

        return encodeValuesFromListOfStrings(multivalue, parameterValues, type);
    }


    public String encodeValuesFromListOfStrings(boolean multivalue, List<String> parameterValues, String type) {
        if (!multivalue) {
            return parameterValues.get(0);
        } else {
            StringBuilder valuesToReturn = new StringBuilder();
            for (String value : parameterValues) {
                valuesToReturn.append(value).append(";");
            }
            return valuesToReturn.substring(0, valuesToReturn.length() - 1);
        }
    }


    private Map applyLocale(Map map) {
        logger.debug("IN");

        ConfigSingleton config = ConfigSingleton.getInstance();
        Locale portalLocale;
        try {
            portalLocale = PortletUtilities.getPortalLocale();
            logger.debug("Portal locale: " + portalLocale);
        } catch (Exception e) {
            logger.warn("Error while getting portal locale.");
            portalLocale = MessageBuilder.getBrowserLocaleFromSpago();
            logger.debug("Spago locale: " + portalLocale);
        }

        SourceBean languageSB = null;
        if (portalLocale != null) {
            languageSB = (SourceBean) config.getFilteredSourceBeanAttribute("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE", "language", portalLocale.getLanguage());
        }

        if (languageSB != null) {
            map.put(COUNTRY, languageSB.getAttribute("country"));
            map.put(LANGUAGE, languageSB.getAttribute("language"));
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
