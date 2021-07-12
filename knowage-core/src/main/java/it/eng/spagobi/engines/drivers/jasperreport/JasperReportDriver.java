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

package it.eng.spagobi.engines.drivers.jasperreport;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.drivers.AbstractEngineDriver;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

/**
 * Driver Implementation (IEngineDriver Interface) for Jasper Report Engine.
 */
public class JasperReportDriver extends AbstractEngineDriver implements IEngineDriver {

	static Logger logger = Logger.getLogger(JasperReportDriver.class);

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
		} catch (ClassCastException cce) {
			logger.error("The parameter is not a BIObject type", cce);
		}
		map = applySecurity(map, profile);
		logger.debug("OUT");
		return map;
	}

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
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

		pars = addBISubreports(biobj, pars);
		pars = addBIParameters(biobj, pars);
		pars = addBIParameterDescriptions(biobj, pars);

		logger.debug("OUT");
		return pars;
	}

	/**
	 * Add subreport informations
	 */
	private Map addBISubreports(BIObject reportBObject, Map pars) {
		ISubreportDAO subreportDAO;
		IBIObjectDAO bobjectDAO;
		IObjTemplateDAO templateDAO;

		List<Subreport> subreports;
		Subreport subreport;
		BIObject subreportBObject;
		ObjTemplate subreportTemplate;

		String prefixName;
		String tempName;
		String flgTemplateStandard;
		Integer id;

		try {

			subreportDAO = DAOFactory.getSubreportDAO();
			bobjectDAO = DAOFactory.getBIObjectDAO();
			templateDAO = DAOFactory.getObjTemplateDAO();

			subreports = subreportDAO.loadSubreportsByMasterRptId(reportBObject.getId());
			for (int i = 0; i < subreports.size(); i++) {
				subreport = subreports.get(i);
				subreportBObject = bobjectDAO.loadBIObjectForDetail(subreport.getSub_rpt_id());
				subreportTemplate = templateDAO.getBIObjectActiveTemplate(subreportBObject.getId());

				prefixName = subreportBObject.getId() + "_" + subreportTemplate.getBinId();
				pars.put("sr." + (i + 1) + ".ids", prefixName);
				logger.debug("ids: " + prefixName);

				/*
				 * tempName = subreportTemplate.getName().substring(0,subreportTemplate.getName().indexOf(".")); pars.put("subrpt." + (i + 1) + ".tempName",
				 * tempName); logger.debug("tempName: " + tempName);
				 */
				/*
				 * flgTemplateStandard = "true"; if (subreportTemplate.getName().indexOf(".zip") > -1) { flgTemplateStandard = "false"; } pars.put("subrpt." +
				 * (i + 1) + ".flgTempStd", flgTemplateStandard); logger.debug("flgTemplateStandard: " + flgTemplateStandard);
				 */

				/*
				 * id = subreportBObject.getId(); pars.put("subrpt." + (i + 1) + ".id", id); logger.debug("id: " + id);
				 */

			}
			// pars.put("srptnum", "" + subreports.size());

		} catch (EMFUserError e) {
			logger.error("Error while reading subreports:", e);
		} catch (EMFInternalError ex) {
			logger.error("Error while reading subreports:", ex);
		}

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

		try {
			if (biobj == null) {
				logger.warn("BIObject is null");
				return pars;
			}
			// add prefix (objId__templateId) of the master template for manage subreport cache
			IObjTemplateDAO tempdao = DAOFactory.getObjTemplateDAO();
			ObjTemplate objtemp = tempdao.getBIObjectActiveTemplate(biobj.getId());
			String prefixName = biobj.getId() + "__" + objtemp.getBinId();
			pars.put("prefixName", prefixName);
			logger.debug(" prefixName: " + prefixName);

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
		} catch (EMFInternalError ex) {
			logger.error("Error while reading subreports:", ex);
		}

		logger.debug("OUT");
		return pars;
	}

	/**
	 * Function not implemented. This method should not be called
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
