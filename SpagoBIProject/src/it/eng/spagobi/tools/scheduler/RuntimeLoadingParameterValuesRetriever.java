/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * This class retrieves values for the input BIObjectParameter using the modality related to 
 * the roleToBeUsed field and the user profile specified by the userIndentifierToBeUsed field.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class RuntimeLoadingParameterValuesRetriever extends
		ParameterValuesRetriever {
	
	static private Logger logger = Logger.getLogger(RuntimeLoadingParameterValuesRetriever.class);	
	
	private String roleToBeUsed;
	
	private String userIndentifierToBeUsed;

	@Override
	public List<String> retrieveValues(BIObjectParameter parameter) throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		IParameterUseDAO parusedao;
		try {
			parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse paruse = parusedao.loadByParameterIdandRole(parameter.getParID(), roleToBeUsed);
			Integer manInp = paruse.getManualInput();
			if(manInp.intValue()==1) {
				// modality is manual input, no values can be retrieved
				return toReturn;
			} else {
				// modality is not manual input, load list of values from lov
				toReturn = loadList(parameter.getParID());
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	private List<String> loadList(Integer parId) throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		try {
			IParameterDAO pardao = DAOFactory.getParameterDAO();
			Parameter par = pardao.loadForExecutionByParameterIDandRoleName(parId, roleToBeUsed);
			ModalitiesValue modVal = par.getModalityValue();
			// get the lov provider
			String looProvider = modVal.getLovProvider();
			// get from the request the type of lov
			ILovDetail lovDetail = LovDetailFactory.getLovFromXML(looProvider);
			IEngUserProfile profile = GeneralUtilities.createNewUserProfile(userIndentifierToBeUsed);
			String result = lovDetail.getLovResult(profile, null, null, null);
			SourceBean rowsSourceBean = SourceBean.fromXMLString(result);
			List rows = null;
			if(rowsSourceBean != null) {
				rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
				if (rows != null && rows.size() != 0) {
					Iterator it = rows.iterator();
					while(it.hasNext()) {
						SourceBean row = (SourceBean) it.next();
						Object value = row.getAttribute(lovDetail.getValueColumnName());
						if (value != null) {
							toReturn.add(value.toString());
						}
					}
				}
			}
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	public String getRoleToBeUsed() {
		return roleToBeUsed;
	}

	public void setRoleToBeUsed(String roleToBeUsed) {
		this.roleToBeUsed = roleToBeUsed;
	}

	public String getUserIndentifierToBeUsed() {
		return userIndentifierToBeUsed;
	}

	public void setUserIndentifierToBeUsed(String userIndentifierToBeUsed) {
		this.userIndentifierToBeUsed = userIndentifierToBeUsed;
	}

}
