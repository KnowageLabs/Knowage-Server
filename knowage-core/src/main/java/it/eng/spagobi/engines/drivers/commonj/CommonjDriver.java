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

package it.eng.spagobi.engines.drivers.commonj;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.AbstractEngineDriver;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

/**
 * Driver Implementation (IEngineDriver Interface) for COmmonJ External Engine.
 */
public class CommonjDriver extends AbstractEngineDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(CommonjDriver.class);

	public static final String DOCUMENT_ID = "document";
	public static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
	 *
	 * @param profile  Profile of the user
	 * @param roleName the name of the execution role
	 * @param biobject the biobject
	 *
	 * @return Map The map of the execution call parameters
	 */
	@Override
	public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
		logger.debug("IN");
		Map map = new Hashtable();
		try {
			BIObject biobj = (BIObject) biobject;
			map = getMap(biobj);
			// map.put("user", profile.getUserUniqueIdentifier());
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
	 * SpagoBITalendEngine does not manage subobejcts, so this method is equivalent to <code>getParameterMap(object, profile, roleName)</code>.
	 *
	 * @param subObject SubObject to execute
	 * @param profile   Profile of the user
	 * @param roleName  the name of the execution role
	 * @param object    the object
	 *
	 * @return Map The map of the execution call parameters
	 */
	@Override
	public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName) {
		return getParameterMap(object, profile, roleName);
	}

	/**
	 * Starting from a BIObject extracts from it the map of the paramaeters for the execution call
	 *
	 * @param biobj BIObject to execute
	 * @return Map The map of the execution call parameters
	 */
	private Map getMap(BIObject biobj) {
		Map pars = new Hashtable();

		String documentId = biobj.getId().toString();
		pars.put("document", documentId);
		logger.debug("Add document parameter:" + documentId);
		pars = addBIParameters(biobj, pars);
		pars = addBIParameterDescriptions(biobj, pars);
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

		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if (biobj.getDrivers() != null) {
			BIObjectParameter biobjPar = null;
			for (Iterator it = biobj.getDrivers().iterator(); it.hasNext();) {
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

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject The BIOBject to edit
	 * @param profile  the profile
	 *
	 * @return the edits the document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject The BIOBject to edit
	 * @param profile  the profile
	 *
	 * @return the new document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	protected Map applyService(Map pars) {
		logger.debug("IN");
		pars.put("ACTION_NAME", "COMMONJ_ACTION");
		pars.put("NEW_SESSION", "TRUE");
		logger.debug("OUT");
		return pars;
	}

	private Map applyLocale(Map map) {
		logger.debug("IN");

		Locale locale = getLocale();
		map.put(COUNTRY, locale.getCountry());
		map.put(LANGUAGE, locale.getLanguage());

		logger.debug("OUT");
		return map;
	}

	private Locale getLocale() {
		logger.debug("IN");
		try {
			Locale locale = null;
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String currLanguage = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String currCountry = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String currScript = (String) permanentSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
			if (currLanguage != null && currCountry != null) {
				Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

				if (StringUtils.isNotBlank(currScript)) {
					tmpLocale.setScript(currScript);
				}

				locale = tmpLocale.build();
			} else
				locale = GeneralUtilities.getDefaultLocale();

			return locale;
		} catch (Exception e) {
			logger.error("Error while getting locale; using default one", e);
			return GeneralUtilities.getDefaultLocale();
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	@Override
	public List<DefaultOutputParameter> getSpecificOutputParameters(List categories) {
		// TODO Auto-generated method stub
		return null;
	}

	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	@Override
	public List<DefaultOutputParameter> getSpecificOutputParameters(String specificChartType) {
		// TODO Auto-generated method stub
		return null;
	}

}
