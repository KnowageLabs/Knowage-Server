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
package it.eng.spagobi.engines.drivers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.services.common.SsoServiceInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Angelo Bernabei angelo.bernabei@eng.it
 */
public class AbstractDriver {

	private static final String DESCRIPTION_SUFFIX = "_description";

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
	 *            The map of parameters
	 * @return The map of parameters to send to the engine
	 */
	protected Map applySecurity(Map pars, IEngUserProfile profile) {
		logger.debug("IN");
		/*
		 * String active =SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.ACTIVE"); String userId=(String)profile.getUserUniqueIdentifier(); if
		 * (active != null && active.equalsIgnoreCase("true") && !((UserProfile)profile).isSchedulerUser(userId)){
		 * logger.debug("I don't put the UserId information in the URL"); }else { if (((UserProfile) profile).getUserUniqueIdentifier() != null) {
		 */
		pars.put(SsoServiceInterface.USER_ID, ((UserProfile) profile).getUserUniqueIdentifier());
		// }
		// }

		logger.debug("Add parameter: " + SsoServiceInterface.USER_ID + " / " + ((UserProfile) profile).getUserUniqueIdentifier());
		logger.debug("OUT");
		return pars;
	}

	/**
	 * get the description of the parameter and create a new biparameter to pass at the engine with url parameter_name+DESCRIPTION_SUFFIX
	 * 
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
					logger.debug("Manage parameter: " + biobjPar.getParameterUrlName());
					/*
					 * value = (String) biobjPar.getParameterValues().get(0); pars.put(biobjPar.getParameterUrlName(), value);
					 */
					description = parValuesEncoder.encodeDescription(biobjPar);
					if (description != null) {
						pars.put(biobjPar.getParameterUrlName() + DESCRIPTION_SUFFIX, description);
						logger.debug("Add description:" + biobjPar.getParameterUrlName() + DESCRIPTION_SUFFIX + "/" + description);
					}
				} catch (Exception e) {
					logger.debug("OUT");
					logger.warn("Error while processing the BIParameter " + biobjPar.getParameterUrlName() + ".. getting the description", e);
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
	 * @param profile
	 *            the profile
	 * 
	 * @return the byte[] with the modification of the document template
	 * 
	 * @throws InvalidOperationRequest
	 *             the invalid operation request
	 */
	public byte[] ElaborateTemplate(byte[] template) throws InvalidOperationRequest {
		logger.warn("Default call. No elaborations are applied to the template.");
		return template;
	}

	public void applyLocale(Locale locale) {
		logger.warn("Method not implemented.");
	}

	/**
	 * get template and return it in Source Bean form
	 * 
	 * @param biObject
	 * @return
	 */

	public SourceBean getTemplateAsSourceBean(BIObject biObject) {
		logger.debug("IN");

		SourceBean content = null;

		try {
			ObjTemplate objtemplate = null;
			try {
				objtemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biObject.getId());
			} catch (Exception e) {
				logger.error("Error in getting template: ", e);
			}
			if (objtemplate == null) {
				throw new Exception("Active template is null");
			}

			byte[] contentBytes = null;
			try {
				contentBytes = DAOFactory.getBinContentDAO().getBinContent(objtemplate.getBinId());
			} catch (Exception e) {
				logger.error("Error in getting bin content from template: ", e);
			}

			if (contentBytes == null) {
				throw new Exception("Content of the Active template is null");
			}

			try {
				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object: ", e);
				throw new Exception("Error while converting the Template bytes into a SourceBean object");
			}

		} catch (Exception e) {
			logger.error("Error while recovering document template: \n" + e);
		}

		logger.debug("OUT");
		return content;
	}

	public SourceBean getTemplateAsSourceBean(byte[] contentBytes) {
		logger.debug("IN");

		SourceBean content = null;

		try {

			try {
				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object: ", e);
				throw new Exception("Error while converting the Template bytes into a SourceBean object");
			}

		} catch (Exception e) {
			logger.error("Error while recovering document template: \n" + e);
		}

		logger.debug("OUT");
		return content;
	}

	public JSONObject getTemplateAsJsonObject(byte[] contentTemplate) {
		logger.debug("IN");

		JSONObject content = null;

		try {

			if (contentTemplate == null) {
				throw new Exception("Content of the Active template is null");
			}

			try {
				String contentStr = new String(contentTemplate);
				content = new JSONObject(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a JSON object: ", e);
				throw new Exception("Error while converting the Template bytes into a JSON object");
			}

		} catch (Exception e) {
			logger.error("Error while recovering document template: \n" + e);
		}

		logger.debug("OUT");
		return content;
	}

	public List<DefaultOutputParameter> getDefaultOutputParameters() {
		return new ArrayList<>();
	}

}