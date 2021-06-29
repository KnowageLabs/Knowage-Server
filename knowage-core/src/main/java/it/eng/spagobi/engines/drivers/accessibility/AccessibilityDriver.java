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
package it.eng.spagobi.engines.drivers.accessibility;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.AbstractEngineDriver;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

public class AccessibilityDriver extends AbstractEngineDriver implements IEngineDriver {

	static Logger logger = Logger.getLogger(AccessibilityDriver.class);

	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	@Override
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
		logger.debug("OUT");
		return map;
	}

	@Override
	public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName) {
		return getParameterMap(object, profile, roleName);
	}

	private Map getMap(BIObject biobj) {
		logger.debug("IN");
		Map pars = new Hashtable();

		String documentId = biobj.getId().toString();
		pars.put("document", documentId);
		logger.debug("Add document parameter:" + documentId);
		pars.put("documentLabel", biobj.getLabel());
		logger.debug("Add document parameter:" + biobj.getLabel());

		// adding date format parameter
		SingletonConfig config = SingletonConfig.getInstance();
		String formatSB = config.getConfigValue("SPAGOBI.DATE-FORMAT.format");
		String format = (formatSB == null) ? "DD-MM-YYYY" : formatSB;
		pars.put("dateformat", format);

		pars = addBIParameters(biobj, pars);
		pars = addBIParameterDescriptions(biobj, pars);

		logger.debug("OUT");
		return pars;
	}

	private Map addBIParameters(BIObject biobj, Map pars) {
		logger.debug("IN");
		if (biobj == null) {
			logger.warn("BIObject is null");
			return pars;
		}

		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if (biobj.getDrivers() != null) {
			BIObjectParameter biobjPar = null;
			for (Iterator it = biobj.getDrivers().iterator(); it.hasNext();) {
				try {
					biobjPar = (BIObjectParameter) it.next();
					String value = parValuesEncoder.encode(biobjPar);
					if (value != null)
						pars.put(biobjPar.getParameterUrlName(), value);
					else
						logger.warn("value encoded IS null");
					logger.debug("Add parameter:" + biobjPar.getParameterUrlName() + "/" + value);
				} catch (Exception e) {
					logger.error("Error while processing a BIParameter", e);
				}
			}
		}
		logger.debug("OUT");
		return pars;
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
